package com.jeremiz.monitor;

import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SessionManager {
    private static final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    public static void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    public static void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public static Set<WebSocketSession> getSessions() {
        return sessions;
    }
}