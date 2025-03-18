package com.jeremiz.monitor.service;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.NetworkIF;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SystemMetricsService {
    private static final SystemInfo systemInfo = new SystemInfo();
    private static final CentralProcessor processor = systemInfo.getHardware().getProcessor();
    private static final GlobalMemory memory = systemInfo.getHardware().getMemory();

    private static long prevTime = 0;
    private static double timeDiff = 0;
    private static long[] prevTicks = processor.getSystemCpuLoadTicks();
    private static long preReadBytes = 0;
    private static long preWriteBytes = 0;
    private static long prevRxBytes = 0;
    private static long prevTxBytes = 0;

    public static Map<String, Double> getSysMetrics() {
        long currentTime = System.currentTimeMillis();
        timeDiff = (currentTime - prevTime) / 1000.0;
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("cpu_usage", getCpuUsage());
        metrics.put("mem_usage", getMemUsage());
        metrics.put("disk_read", getDiskReadSpeed());
        metrics.put("disk_write", getDiskWriteSpeed());
        metrics.put("net_upload", getNetUploadSpeed());
        metrics.put("net_download", getNetDownloadSpeed());
        prevTime = currentTime;
        return metrics;
    }

    // 获取CPU使用率（百分比）
    private static double getCpuUsage() {
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = ticks;
        return cpuUsage;
    }

    public static double getTotalMem() {
        return (double) memory.getTotal() / (1024 * 1024);
    }

    private static double getAvailMem() {
        return (double) memory.getAvailable() / (1024 * 1024);
    }

    private static double getMemUsage() {
        return getTotalMem() - getAvailMem();
    }

    // 获取系统平均负载（1分钟、5分钟、15分钟）
    public static double[] getSysLoadAvg() {
        return processor.getSystemLoadAverage(3);
    }

    private static double getDiskReadSpeed() {
        List<HWDiskStore> diskStores = systemInfo.getHardware().getDiskStores();
        long totalReadBytes = 0;
        for (HWDiskStore disk : diskStores) {
            totalReadBytes += disk.getReadBytes();
        }
        double readSpeed = (double)(totalReadBytes - preReadBytes) / timeDiff / 1024;
        preReadBytes = totalReadBytes;
        return readSpeed;
    }

    private static double getDiskWriteSpeed() {
        List<HWDiskStore> diskStores = systemInfo.getHardware().getDiskStores();
        long totalWriteBytes = 0;
        for (HWDiskStore disk : diskStores) {
            totalWriteBytes += disk.getWriteBytes();
        }
        double writeSpeed = (double)(totalWriteBytes - preWriteBytes) / timeDiff / 1024;
        preWriteBytes = totalWriteBytes;
        return writeSpeed;
    }

    private static double getNetUploadSpeed() {
        List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();
        long totalTxBytes = 0;
        for (NetworkIF net : networkIFs) {
            totalTxBytes += net.getBytesSent();
        }
        double uploadSpeed = (double)(totalTxBytes - prevTxBytes) / timeDiff / 1024;
        prevTxBytes = totalTxBytes;
        return uploadSpeed;
    }

    private static double getNetDownloadSpeed() {
        List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();
        long totalRxBytes = 0;
        for (NetworkIF net : networkIFs) {
            totalRxBytes += net.getBytesRecv();
        }
        double downloadSpeed = (double)(totalRxBytes - prevRxBytes) / timeDiff / 1024;
        prevRxBytes = totalRxBytes;
        return downloadSpeed;
    }
}
