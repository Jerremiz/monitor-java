package com.jeremiz.monitor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api") // 设置路径前缀
public class HistoryDataController {
    private Map<String, Object> getData(String metricName) {
        return Map.of(
                "lastHour", HistoryDataService.getFromDatabase(metricName, "-1 hour"),
                "lastDay", HistoryDataService.getFromDatabase(metricName, "-1 day"),
                "lastWeek", HistoryDataService.getFromDatabase(metricName, "-7 days"),
                "lastMonth", HistoryDataService.getFromDatabase(metricName, "-1 month")
        );
    }

    @GetMapping("/cpu_usage")
    public Map<String, Object> getCpu() {
        return getData("cpu_usage");
    }

    @GetMapping("/sys_load_1")
    public Map<String, Object> getLoad1() {
        return getData("sys_load_1");
    }

    @GetMapping("/sys_load_5")
    public Map<String, Object> getLoad5() {
        return getData("sys_load_5");
    }

    @GetMapping("/sys_load_15")
    public Map<String, Object> getLoad15() {
        return getData("sys_load_15");
    }

    @GetMapping("/mem_usage")
    public Map<String, Object> getMemory() {
        Map<String, Object> response = new HashMap<>(getData("mem_usage"));
        response.put("totalMem", SystemMetricsService.getTotalMem());
        return response;
    }

    @GetMapping("/disk_read")
    public Map<String, Object> getDiskRead() {
        return getData("disk_read");
    }

    @GetMapping("/disk_write")
    public Map<String, Object> getDiskWrite() {
        return getData("disk_write");
    }

    @GetMapping("/net_upload")
    public Map<String, Object> getNetUpload() {
        return getData("net_upload");
    }

    @GetMapping("/net_download")
    public Map<String, Object> getNetDownload() {
        return getData("net_download");
    }
}