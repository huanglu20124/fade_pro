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

	String concern(Integer fans_id, Integer star_id);

	String cancelConcern(Integer fans_id, Integer star_id);

	String getPersonPage(Integer user_id, Integer my_id);

	String getHeadImageUrl(User user);

	String getAddMessage(Integer user_id);

	String getAddContribute(Integer user_id,Integer start,String point);

	String getAddFans(Integer user_id,Integer start,String point);

	String getAddComment(Integer user_id,Integer start,String point);

	String searchUser(String keyword, Integer page);

	String getTenRecommendUser(Integer user_id, Integer page);

	String getMessageToken(Integer user_id)throws FadeException;

	String getOldContribute(Integer user_id, Integer start);

	String getOldFans(Integer user_id, Integer start);

	String getOldComment(Integer user_id, Integer start);

	void downloadPic(String url, String localPath);

	String getFans(Integer user_id, Integer start);

	String getConcerns(Integer user_id, Integer start);

}
