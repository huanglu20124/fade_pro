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
		//同时把tokenModel存储在redis中，token作为key
		redisUtil.addKey(token,user_id.toString());
		//加入到登录队列,"user_"+user_id作为key,value为token
		redisUtil.listLeftPush("user_"+user_id,token);
		return tokenModel;
	}
 		
	public Boolean checkToken(TokenModel tokenModel){
		String token  = tokenModel.getToken();
		String user_id = (String)redisUtil.getValue(token);
		if(user_id == null) return false;
		else {
			if(tokenModel.getUser_id().toString().equals(user_id))
				return true;
		}
		return false;
	}
	
	public void deleteToken(TokenModel tokenModel){
		//退出登录
		//移出登录队列
		redisUtil.listRemoveValue("user_"+tokenModel.getUser_id(), tokenModel.getToken());
		//删除key
		redisUtil.deleteKey(tokenModel.getToken());
	}
}
