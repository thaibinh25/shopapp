package com.project.shopapp;


import com.project.shopapp.components.CustomSpringConfigurator;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    public WebSocketConfig(ApplicationContext applicationContext) {
        CustomSpringConfigurator.setApplicationContext(applicationContext);
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter(); // Bắt buộc cho @ServerEndpoint
    }

    @Bean
    public ServerEndpointConfig serverEndpointConfig() {
        return ServerEndpointConfig.Builder
                .create(WebSocketServer.class, "/ws")
                .configurator(new CustomSpringConfigurator())
                .build();
    }
}

