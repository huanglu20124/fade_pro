package com.fade.websocket;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer{
	
	@Resource(name = "interceptor")
	private WebSocketHandshakeInterceptor interceptor;

	@Resource(name = "messageWebSocketHandler")
	private MessageWebSocketHandler handler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		//分别添加处理器，拦截器
		registry.addHandler(handler,"/webSocketServer")
		        .addInterceptors(interceptor);
		registry.addHandler(handler,"/webSocketServer/sockjs")
		        .addInterceptors(interceptor)
		        .withSockJS();
	}
	
}
