package com.fade.util;

import java.util.UUID;

import javax.annotation.Resource;
import org.springframework.stereotype.Component;

import com.fade.domain.TokenModel;

@Component("tokenUtil")
public class TokenUtil {
	
	@Resource(name = "redisUtil")
	private RedisUtil redisUtil;
	
	public TokenModel createTokenModel(Integer user_id){
		//登录时间作为uuid
		String token = UUID.randomUUID().toString();
		TokenModel tokenModel = new TokenModel(user_id,token);
		//加入到redis,"user_"+user_id作为key,value为token
		redisUtil.addKey("user_"+user_id,token);
		return tokenModel;
	}
 		
	public Boolean checkToken(TokenModel tokenModel){
		if(tokenModel == null) return false;
		//校验token
		String token = (String) redisUtil.getValue("user_" + tokenModel.getUser_id().toString());
		if(token.equals(tokenModel.getToken())){
			return true;
		}else {
			return false;
		}
	}
	
	public void deleteToken(TokenModel tokenModel){
		//退出登录
		redisUtil.deleteKey("user_" + tokenModel.getUser_id().toString());
	}
	
	public String getToken(Integer user_id){
		return (String) redisUtil.getValue("user_" + user_id.toString());
	}
}
