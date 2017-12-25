package com.fade.service;

import org.springframework.web.multipart.MultipartFile;

import com.fade.domain.TokenModel;
import com.fade.domain.User;
import com.fade.exception.FadeException;

public interface UserService {

	String getUserById(Integer user_id) throws FadeException;

	String loginWechat(String wechat_id) throws FadeException;

	String registerWechat(String wechat_id,User user) throws FadeException;

	String registerQueryTel(String telephone);

	String registerByName(User user,MultipartFile file) throws FadeException;

	String loginUser(User user);

	String updateUserById(User user,MultipartFile file) throws FadeException;

	String logoutUserByToken(TokenModel model) throws FadeException;

	String online(Integer user_id);

	String offline(Integer user_id);


}
