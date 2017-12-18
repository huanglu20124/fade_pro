package com.fade.service;

import com.fade.domain.TokenModel;
import com.fade.domain.User;
import com.fade.exception.FadeException;

public interface UserService {

	String getUserById(Integer user_id) throws FadeException;

	String loginWechat(String wechat_id) throws FadeException;

	String registerWechat(String wechat_id,User user) throws FadeException;

	String registerQueryTel(String telephone);

	String registerByName(User user) throws FadeException;

	String loginUser(User user) throws FadeException;

	String updateUserById(User user) throws FadeException;

	String logoutUserByToken(TokenModel model) throws FadeException;


}
