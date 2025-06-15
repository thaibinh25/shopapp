package com.project.shopapp.components;



import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.websocket.server.ServerEndpointConfig.Configurator;

@Component
public class CustomSpringConfigurator extends Configurator {

    private static ApplicationContext context;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) {
        return context.getBean(endpointClass); // Lấy bean từ Spring context
    }
}
