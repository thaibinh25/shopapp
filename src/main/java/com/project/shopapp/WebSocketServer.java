package com.project.shopapp;

import com.project.shopapp.components.CustomSpringConfigurator;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


//@ServerEndpoint(value = "/ws", configurator = CustomSpringConfigurator.class)
@Component // ⚠️ Chỉ nếu bạn muốn lấy bean từ Spring
public class WebSocketServer {
    /*private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Client connected: " + session.getId());
    }

    @OnMessage
    public void onMessage(String msg, Session session) throws IOException {
        System.out.println("Message: " + msg);
        session.getBasicRemote().sendText("Echo: " + msg);
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Client disconnected: " + session.getId());
    }*/

    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();


    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String query = session.getQueryString(); // ví dụ: userId=6
        if (query != null && query.startsWith("userId=")) {
            String userId = query.substring("userId=".length());
            session.getUserProperties().put("userId", userId);
            sessions.add(session);
            sessionMap.put(userId, session); // gửi realTime đến usser
            System.out.println("Client connected: " + session.getId() + " (userId: " + userId + ")");
        } else {
            try {
                session.close(); // từ chối kết nối nếu không có userId
            } catch (IOException ignored) {}
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessionMap.entrySet().removeIf(entry -> entry.getValue().equals(session));
        sessions.remove(session); //dọn dẹp các session chết
        System.out.println("Session " + session.getId() + " disconnected");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Message from client: " + message);
    }

    private String getUserIdFromQuery(Session session) {
        String query = session.getQueryString();
        if (query != null && query.contains("userId=")) {
            return query.split("userId=")[1];
        }
        return null;
    }

    // ✅ API dùng để gửi tin nhắn từ controller/service
    public static void sendMessageToUser(String userId, String message) throws IOException {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(message);
        }
    }

    public static void broadcast(String message) throws IOException {
        for (Session session : sessionMap.values()) {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        }
    }

    public static void sendNotificationToUser(Long userId, String message) {
        for (Session session : sessions) {
            String uid = (String) session.getUserProperties().get("userId");
            if (uid != null && uid.equals(String.valueOf(userId))) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



}
