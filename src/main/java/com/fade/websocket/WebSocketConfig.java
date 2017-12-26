package com.fade.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer{

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		//分别添加处理器，拦截器
		registry.addHandler(systemWebSocketHandler(),"/webSocketServer")
		        .addInterceptors(new WebSocketHandshakeInterceptor());
		registry.addHandler(systemWebSocketHandler(),"/webSocketServer/sockjs")
		        .addInterceptors(new WebSocketHandshakeInterceptor())
		        .withSockJS();
	}
	
	@Bean
    public WebSocketHandler systemWebSocketHandler(){
        return new SystemWebSocketHandler();
    }
	
}
