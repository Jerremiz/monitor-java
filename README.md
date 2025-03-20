<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
<h1>🚀 Performance Metrics</h1>
</div>

## 📋 项目简介

一个基于 Spring Boot 的系统性能监控工具，可以实时监控并记录系统的各项性能指标，包括 CPU 使用率、系统负载、内存使用情况、磁盘读写速度以及网络流量等。

## ✨ 特性

- 🔄 **实时监控**: 通过 WebSocket 提供实时系统指标数据
- 📊 **历史数据**: 支持查询过去一小时、一天、一周和一个月的历史数据
- 🌐 **跨平台**：支持 Windows、Linux 和 macOS 等多种操作系统
- 🔌 **轻量级部署**：单一 JAR 包部署，内嵌式SQLite数据库，无需复杂环境配置
- 🗄️ **自动维护**：定期清理过期数据并压缩数据库，优化存储性能

## 🔧 安装

### 环境要求

- [JDK 17 或更高版本](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

### 安装步骤

1. **下载 JAR 包**
   ```bash
   curl -LO https://github.com/Jerremiz/monitor-java/releases/download/v1.0.6/monitor-1.0.6.jar
   ```
   或访问[Releases页面](https://github.com/Jerremiz/monitor-java/releases)下载最新版本

2. **运行应用**
   ```bash
   java -jar monitor-1.0.6.jar
   ```
   默认端口：4173

3. **设置为系统服务** (Linux, 使用systemd)
   ```bash
   # 创建服务文件
   sudo nano /etc/systemd/system/monitor.service
   
   # 文件内容
   [Unit]
   Description=Performance Metrics Monitor
   After=network.target
   
   [Service]
   User=yourusername
   ExecStart=/usr/bin/java -jar /path/to/monitor-1.0.6.jar
   Restart=on-failure
   
   [Install]
   WantedBy=multi-user.target
   
   # 启用并启动服务
   sudo systemctl enable monitor
   sudo systemctl start monitor
   ```

## ⚙️ 配置选项

### 基本配置

可通过 `application.properties` 文件或命令行参数配置：

```bash
# 自定义端口
java -jar monitor-1.0.6.jar --server.port=9090
```

## 📝 使用指南

### 配合 Web 前端

[monitor-vue](https://github.com/Jerremiz/monitor-vue) 提供了完整的可视化界面，支持实时监控和历史数据查看。

### REST API

系统提供以下 REST API 端点来获取历史性能数据:

| 端点                  | 描述            |
|---------------------|---------------|
| `/api/cpu_usage`    | CPU 使用率 (百分比) |
| `/api/mem_usage`    | 内存使用量 (MB)    |
| `/api/sys_load_1`   | 1 分钟系统负载      |
| `/api/sys_load_5`   | 5 分钟系统负载      |
| `/api/sys_load_15`  | 15 分钟系统负载     |
| `/api/disk_read`    | 磁盘读取速度 (KB/s) |
| `/api/disk_write`   | 磁盘写入速度 (KB/s) |
| `/api/net_upload`   | 网络上传速度 (KB/s) |
| `/api/net_download` | 网络下载速度 (KB/s) |

#### 响应格式示例

```json
{
  "lastHour": {
    "labels": ["2023-08-01 10:00:00", "2023-08-01 10:01:00"],
    "data": [996.01953125, 998.43359375]
  },
  "lastDay": {
    "labels": ["2023-08-01 10:00:00", "2023-08-01 10:01:00"],
    "data": [996.01953125, 998.43359375]
  },
  "lastWeek": {
    "labels": ["2023-08-01 10:00:00", "2023-08-01 10:01:00"],
    "data": [996.01953125, 998.43359375]
  },
  "lastMonth": {
    "labels": ["2023-08-01 10:00:00", "2023-08-01 10:01:00"],
    "data": [996.01953125, 998.43359375]
  }
}
```

### WebSocket

实时数据通过 WebSocket 传输，连接端点为 `/ws/data`

## 👨‍💻 开发

1. **克隆仓库**
   ```bash
   git clone https://github.com/Jerremiz/monitor-java.git
   cd monitor-java
   ```

2. **启动开发服务器**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **打包**
   ```bash
   ./mvnw clean package
   ```

## 💾 数据管理

- **数据库文件位置**：应用运行目录下的 `monitor.db`
- **数据采集频率**：系统每分钟采集一次性能指标
- **数据库维护**：每天自动清理数据并压缩数据库，默认保留 3 个月历史数据

## 📄 许可证

- [GNU General Public License v3.0](LICENSE)