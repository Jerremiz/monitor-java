package com.jeremiz.monitor.service;

import com.jeremiz.monitor.utils.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HistoryDataService {
    private static final Logger logger = LoggerFactory.getLogger(HistoryDataService.class);

    // 定义缓存，键为 metricType + timeTamp，值为缓存项
    private static final ConcurrentHashMap<String, CacheItem> cache = new ConcurrentHashMap<>();
    private Map<String, Double> metrics;

    private static final Map<String, Integer> timeInterval = Map.of(
            "-1 hour", 60,
            "-1 day", 900,
            "-7 days", 3600,
            "-1 month", 14400
    );

    private static final String DB_URL = "jdbc:sqlite:monitor.db"; // SQLite 数据库文件路径
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS metrics (" +
            "timestamp DATETIME PRIMARY KEY, " +
            "cpu_usage DOUBLE, " +
            "sys_load_1 DOUBLE, " +
            "sys_load_5 DOUBLE, " +
            "sys_load_15 DOUBLE, " +
            "mem_usage DOUBLE, " +
            "disk_read DOUBLE, " +
            "disk_write DOUBLE, " +
            "net_upload DOUBLE, " +
            "net_download DOUBLE)";

    // 缓存类，包含数据和更新时间
    private static class CacheItem {
        private final Map<String, Object> data;
        private final long updateTime;

        public CacheItem(Map<String, Object> data) {
            this.data = data;
            this.updateTime = System.currentTimeMillis();
        }

        public Map<String, Object> getData() {
            return data;
        }

        public long getUpdateTime() {
            return updateTime;
        }
    }

    // 创建数据库表
    static {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                conn.createStatement().execute(CREATE_TABLE_SQL);
            }
        } catch (SQLException e) {
            logger.error("Error creating table", e);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void setData() {
        if (WebSocketSessionManager.getSessions().isEmpty()) {
            metrics = SystemMetricsService.getSysMetrics();
        } else {
            metrics = RealTimeDataService.dataPoint();
        }

        double[] sys_load = SystemMetricsService.getSysLoadAvg();
        metrics.put("sys_load_1", sys_load[0]);
        metrics.put("sys_load_5", sys_load[1]);
        metrics.put("sys_load_15", sys_load[2]);

        saveToDatabase();
    }

    private void saveToDatabase() {
        String insertSql = "INSERT INTO metrics (timestamp, cpu_usage, sys_load_1, sys_load_5, sys_load_15, mem_usage, disk_read, disk_write, net_upload, net_download) VALUES (datetime('now'), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(insertSql)) {
            preparedStatement.setDouble(1, metrics.get("cpu_usage"));
            preparedStatement.setDouble(2, metrics.get("sys_load_1"));
            preparedStatement.setDouble(3, metrics.get("sys_load_5"));
            preparedStatement.setDouble(4, metrics.get("sys_load_15"));
            preparedStatement.setDouble(5, metrics.get("mem_usage"));
            preparedStatement.setDouble(6, metrics.get("disk_read"));
            preparedStatement.setDouble(7, metrics.get("disk_write"));
            preparedStatement.setDouble(8, metrics.get("net_upload"));
            preparedStatement.setDouble(9, metrics.get("net_download"));
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            logger.error("Error saving system metrics to database", e);
        }
    }

    public static Map<String, Object> getFromDatabase(String metricType, String timesTamp) {
        String cacheKey = metricType + timesTamp;
        CacheItem cacheItem = cache.get(cacheKey);

        if (cacheItem != null && System.currentTimeMillis() - cacheItem.getUpdateTime() < 30000) {
            return cacheItem.getData();
        } else {
            cache.remove(cacheKey);
        }

        Map<String, Object> response = new HashMap<>();
        LinkedList<String> labels = new LinkedList<>();
        LinkedList<Double> data = new LinkedList<>();

        // 定义允许的metricType集合
        Set<String> allowedMetrics = Set.of(
                "cpu_usage", "sys_load_1", "sys_load_5", "sys_load_15",
                "mem_usage", "disk_read", "disk_write", "net_upload", "net_download"
        );
        if (!allowedMetrics.contains(metricType)) {
            logger.error("Invalid metric type: {}", metricType);
            return response;
        }

        String querySql = "SELECT timestamp, " + metricType + " FROM metrics " + " WHERE timestamp >= datetime('now', ?) " + " AND ((strftime('%s', (SELECT MAX(timestamp) FROM metrics)) - strftime('%s', timestamp)) % ?) = 0 " + " ORDER BY timestamp DESC";

        //noinspection SqlSourceToSinkFlow
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(querySql)) {
            preparedStatement.setString(1, timesTamp);
            preparedStatement.setInt(2, timeInterval.get(timesTamp));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                labels.addFirst(resultSet.getString("timestamp"));
                data.addFirst(resultSet.getDouble(metricType));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving data from database", e);
        }

        response.put("labels", labels);
        response.put("data", data);

        // 更新缓存
        cache.put(cacheKey, new CacheItem(response));
        return response;
    }

    // 定时任务，每天凌晨3点执行数据清理和数据库压缩
    @Scheduled(cron = "0 0 3 * * *")
    public void removeOldData() {
        String deleteSql = "DELETE FROM metrics WHERE timestamp < datetime('now', '-3 months')";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            int deletedRows = stmt.executeUpdate(deleteSql);
            stmt.execute("VACUUM"); // 执行 VACUUM 整理压缩数据库
            logger.info("Old data cleanup and vacuum completed, deleted {} rows.", deletedRows);
        } catch (SQLException e) {
            logger.error("Error during old data cleanup or vacuum", e);
        }
    }
}
