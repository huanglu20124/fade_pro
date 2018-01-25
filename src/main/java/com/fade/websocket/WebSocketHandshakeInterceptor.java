package com.fade.websocket;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.fade.domain.TokenModel;
import com.fade.util.TokenUtil;

@Component("interceptor")
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor{
	
	@Resource(name = "tokenUtil")
	private TokenUtil tokenUtil;
	
	private static Logger logger = Logger.getLogger(WebSocketHandshakeInterceptor.class);
	
	@Override
	public void afterHandshake(ServerHttpRequest arg0, ServerHttpResponse arg1, WebSocketHandler arg2, Exception arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler arg2,
			Map<String, Object>attributes ) throws Exception {
		//建立连接前，进行token认证
		ServletServerHttpRequest req = (ServletServerHttpRequest) request;
		Integer user_id = new Integer(req.getServletRequest().getParameter("user_id"));
		String token = req.getServletRequest().getParameter("token");
		if(token == null || user_id == null) return false;
		TokenModel tokenModel = new TokenModel(user_id, token);
		if(tokenUtil.checkToken(tokenModel)){
			attributes.put("user", tokenModel.getUser_id());
			logger.info("用户"+tokenModel.getUser_id() + "通过websocket认证");
			return true;
		}else {
			logger.info("用户"+tokenModel.getUser_id() + "不能通过websocket认证");
			return false;
		}
	}

}
