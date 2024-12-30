package com.jeremiz.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class RealTimeDataService extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RealTimeDataService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Map<String, Double> metrics;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        try {
            SessionManager.addSession(session);
            logger.info("WebSocket session added: {}", session.getId());
        } catch (Exception e) {
            logger.error("Error adding WebSocket session", e);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        try {
            SessionManager.removeSession(session);
            logger.info("WebSocket session removed: {}", session.getId());
        } catch (Exception e) {
            logger.error("Error removing WebSocket session", e);
        }
    }

    @Scheduled(fixedRate = 1000)
    private void sendRealTimeSysMetrics() {
        Set<WebSocketSession> sessions = SessionManager.getSessions();
        if (!sessions.isEmpty()) {
            getRealTimeSysMetrics();
            sendSysMetrics(sessions);
        }
    }

    private void getRealTimeSysMetrics() {
        metrics = SystemMetricsService.getSysMetrics();
    }

    public static Map<String, Double> dataPoint() {
        return metrics;
    }

    private void sendSysMetrics(Set<WebSocketSession> sessions) {
        Map<String, Object> response = new HashMap<>(metrics);
        response.put("timestamp", System.currentTimeMillis());

        sessions.forEach(session -> {
            try {
                String jsonData = objectMapper.writeValueAsString(response);
                session.sendMessage(new TextMessage(jsonData));
            } catch (IOException e) {
                logger.error("Error sending message to WebSocket session", e);
            }
        });
    }
}
