package com.jeremiz.monitor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String CHART_DATA_ENDPOINT = "/ws/data";
    private static final String[] ALLOWED_ORIGINS = {"https://monitor.jeremiz.com", "http://localhost:4173"};

    private final RealTimeDataService realTimeDataService;

    public WebSocketConfig(RealTimeDataService realTimeDataService) {
        this.realTimeDataService = realTimeDataService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realTimeDataService, CHART_DATA_ENDPOINT)
                .setAllowedOrigins(ALLOWED_ORIGINS);
    }
}
