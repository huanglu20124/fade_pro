package com.fade.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

@Component("messageWebSocketHandler")
public class MessageWebSocketHandler implements WebSocketHandler {
	
	public static final Map<String, Object>sessionMap = new HashMap<>();
	
	private static Logger logger = Logger.getLogger(WebSocketHandler.class);
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus arg1) throws Exception {
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// TODO Auto-generated method stub
		//将会话放入map中，id为user_id
		Map<String, Object>attributes = session.getAttributes();
		Integer user_id = (Integer) attributes.get("user");
		sessionMap.put(user_id.toString(), session);
		logger.info("与用户" + user_id + "建立websocket连接");
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> arg1) throws Exception {
		
		// TODO Auto-generated method stub

	}

	@Override
	public void handleTransportError(WebSocketSession arg0, Throwable arg1) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsPartialMessages() {
		// TODO Auto-generated method stub
		return false;
	}

	public void sendMessageToUser(Integer user_id, String message){
		//根据user_id发送消息
		WebSocketSession session = (WebSocketSession) sessionMap.get(user_id.toString());
		if(session != null){
			try {
				if(session.isOpen()){
					session.sendMessage(new TextMessage(message));
					logger.info("向用户" + user_id + "发送websocket消息成功");
				}else {
					logger.info("websocket已关闭");
				}
			} catch (IOException e) {
				logger.info("向用户" + user_id + "发送websocket消息失败");
				e.printStackTrace();
			}
		}
	}
}
