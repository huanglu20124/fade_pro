package com.fade.exception;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;

public class FadeExceptionResolver implements HandlerExceptionResolver {

	public static Logger logger = Logger.getLogger(FadeExceptionResolver.class);
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// 统一异常处理代码
		// 针对系统自定义的FadeException异常，就可以直接从异常类中获取异常信息，将异常处理在错误页面展示
		FadeException fadeException = null;
		// 如果ex是系统 自定义的异常，直接取出异常信息
		if (ex instanceof FadeException) {
			fadeException = (FadeException) ex;
		} else {
			fadeException = new FadeException("服务器未知错误 "+ex.getMessage());
		}
		//日志记录原异常
		logger.error(fadeException.getErrorMessage() + "\r\n" + getException(ex) + "\r\n");
		try {
			Map<String, Object>map = new HashMap<>();
			map.put("error",fadeException.getErrorMessage() );
			response.getWriter().write(JSON.toJSONString(map));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//不返回视图，直接json返回
		return null;
	}
	
	 public static String getException(Exception e){
	        StackTraceElement[] ste = e.getStackTrace();
	        StringBuffer sb = new StringBuffer();
	        sb.append(e.getMessage() + " ");
	        //错误信息限定在前5行
	        if(ste.length <= 10){
		        for (int i = 0; i < ste.length; i++) {
			          sb.append(ste[i].toString() + "\r\n");
			        }
	        }else {
		        for (int i = 0; i < 5; i++) {
			          sb.append(ste[i].toString() + "\r\n");
			        }
			}
	        return sb.toString();
	    }
}
