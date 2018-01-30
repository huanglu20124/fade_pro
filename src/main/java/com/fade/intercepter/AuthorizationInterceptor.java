package com.fade.intercepter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.fade.domain.SimpleResponse;
import com.fade.domain.TokenModel;
import com.fade.exception.FadeException;
import com.fade.util.TokenUtil;

public class AuthorizationInterceptor extends HandlerInterceptorAdapter{
	
	@Resource(name = "tokenUtil")
	private TokenUtil tokenUtil;
	
	public static Logger logger = Logger.getLogger(AuthorizationInterceptor.class);
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		//String tokenModel_str = request.getParameter("tokenModel");
		//改用从header头部获取token信息
		String tokenModel_str = request.getHeader("tokenModel");
		if(tokenModel_str == null){
			logger.info(request.getRequestURI() +"请求失败,"+
			getIpAddr(request) + "输入的tokenModel格式不合法,tokenModel为空");
			return false;
		}
		else {
			TokenModel model = null;
			try {
				model = JSON.parseObject(tokenModel_str,TokenModel.class);
			} catch (JSONException e) {
				
				logger.info(request.getRequestURI() +"请求失败,"+
				getIpAddr(request) + "输入的tokenModel格式不合法,tokenModel为" + model);
				return false;
			}
			System.out.println("开始token验证,token=" + model.getToken() + ",user_id="+model.getUser_id());
			if(tokenUtil.checkToken(model) == true){
				System.out.println("通过认证");
				return true;
			}else {
				logger.info(request.getRequestURI() +"请求失败,"+
				getIpAddr(request) + "未通过认证,tokenModel为" + model);
				return false;
			}
		}
	}
	
	private  String getIpAddr(HttpServletRequest request) {     
		      String ip = request.getHeader("x-forwarded-for");     
		      if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {     
		         ip = request.getHeader("Proxy-Client-IP");     
		     }     
		      if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {     
		         ip = request.getHeader("WL-Proxy-Client-IP");     
		      }     
		     if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {     
		          ip = request.getRemoteAddr();     
		     }     
		     return ip;     
		} 
}
