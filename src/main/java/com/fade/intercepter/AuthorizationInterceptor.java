package com.fade.intercepter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.fade.domain.TokenModel;
import com.fade.exception.FadeException;
import com.fade.util.TokenUtil;

public class AuthorizationInterceptor extends HandlerInterceptorAdapter{
	
	@Resource(name = "tokenUtil")
	private TokenUtil tokenUtil;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		//String tokenModel_str = request.getParameter("tokenModel");
		//改用从header头部获取token信息
		String tokenModel_str = request.getHeader("tokenModel");
		if(tokenModel_str == null) throw new FadeException("token为空，未通过验证");
		else {
			TokenModel model = null;
			try {
				model = JSON.parseObject(tokenModel_str,TokenModel.class);
			} catch (JSONException e) {
				throw new FadeException("输入的tokenModel格式不合法");
			}
			System.out.println("开始token验证,token=" + model.getToken() + ",user_id="+model.getUser_id());
			if(tokenUtil.checkToken(model) == true){
				System.out.println("通过认证");
				return true;
			}else {
				System.out.println("未通过认证");
				throw new FadeException("未经过token认证");
			}
		}
	}
	
	
}
