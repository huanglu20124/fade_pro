package com.fade.controller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.fade.domain.TokenModel;
import com.fade.domain.User;
import com.fade.exception.FadeException;
import com.fade.service.UserService;

@Controller
public class UserController {
	
	@Resource(name = "userService")
	private UserService userService;
	 
	@RequestMapping(value = "/getUserById/{user_id}", method = RequestMethod.GET)
	@ResponseBody
	public String getUserById(@PathVariable("user_id")Integer user_id) throws FadeException{
		//获取某人全部信息的请求
		return userService.getUserById(user_id);
	}
	
	@RequestMapping(value = "/loginWechat/{wechat_id}", method = RequestMethod.GET)
	@ResponseBody
	public String loginWechat(@PathVariable("wechat_id")String wechat_id) throws FadeException{
		//微信登录，返回最新用户信息，同时进行token处理（token先不设置过期）
		return userService.loginWechat(wechat_id);
	}
	
	@RequestMapping(value = "/registerWechat", method = RequestMethod.POST)
	@ResponseBody
	public String registerWechat(String js_code,String user) throws FadeException{
		return userService.registerWechat(js_code,JSON.parseObject(user, User.class));
	}
	
	@RequestMapping(value = "/registerQueryTel/{telephone}", method = RequestMethod.GET)
	@ResponseBody
	public String registerQueryTel(@PathVariable("telephone")String telephone){
		//查询手机号是否被注册
		return userService.registerQueryTel(telephone);
	}
	
	@RequestMapping(value = "/registerByName", method =  RequestMethod.POST)
	@ResponseBody
	public String registerByName(String user,@RequestParam(name="file",required=false)MultipartFile file) throws FadeException{
		//安卓端昵称密码的注册方式
		return userService.registerByName(JSON.parseObject(user, User.class),file);
	}
	
	@RequestMapping(value = "/loginUserByName/{fade_name}/{password}",method =  RequestMethod.GET)
	@ResponseBody
	public String loginUserByName(@PathVariable("fade_name")String fade_name, 
			@PathVariable("password")String password) throws FadeException{
		//安卓端通过fade_name登录
		User user = new User();
		user.setFade_name(fade_name);
		user.setPassword(password);
		//安卓端昵称密码的注册方式
		return userService.loginUser(user);
	}
	
	@RequestMapping(value = "/loginUserByTel/{telephone}/{password}",method =  RequestMethod.GET)
	@ResponseBody
	public String loginUserByTel(@PathVariable("telephone")String telephone,
			@PathVariable("password")String password) throws FadeException{
		//安卓端通过telephone登录，使用post请求，安全一点
		User user = new User();
		user.setTelephone(telephone);
		user.setPassword(password);
		//安卓端昵称密码的注册方式
		return userService.loginUser(user);
	}
	
	@RequestMapping(value = "/updateUserById", method =  RequestMethod.POST)
	@ResponseBody
	public String updateUserById(String user,@RequestParam(name="file",required=false)MultipartFile file) throws FadeException{
		//包括了上传头像
		//编辑用户信息
		return userService.updateUserById(JSON.parseObject(user, User.class),file);
	}

	@RequestMapping(value = "/updateUserByIdText", method =  RequestMethod.POST)
	@ResponseBody
	public String updateUserByIdText(String user) throws FadeException{
		//包括了上传头像
		//编辑用户信息
		return userService.updateUserById(JSON.parseObject(user, User.class),null);
	}
	
	@RequestMapping(value = "/logoutByToken/{tokenModel}", method =  RequestMethod.DELETE)
	@ResponseBody
	public String logoutUserByToken(@PathVariable("tokenModel")String tokenModel)throws FadeException{
		//注销登录
		TokenModel model = JSON.parseObject(tokenModel,TokenModel.class);
		return userService.logoutUserByToken(model);
	}

	@RequestMapping(value = "/online",method =  RequestMethod.POST)
	@ResponseBody
	public String online(Integer user_id){
		//上线请求
		return userService.online(user_id);
	}
	
	@RequestMapping(value = "/offline/{user_id}",method =  RequestMethod.DELETE)
	@ResponseBody
	public String offline(@PathVariable("user_id")Integer user_id){
		//下线请求
		System.out.println(user_id + "请求下线");
		return userService.offline(user_id);
	}	

	@RequestMapping(value = "/concern",method =  RequestMethod.POST)
	@ResponseBody
	public String concern(Integer fans_id, Integer star_id){
		//关注某人的请求
		return userService.concern(fans_id,star_id);
	}

	@RequestMapping(value = "/cancelConcern",method =  RequestMethod.POST)
	@ResponseBody
	public String cancelConcern(Integer fans_id,Integer star_id){
		return userService.cancelConcern(fans_id,star_id);
	}
	
	@RequestMapping(value = "/getPersonPage/{user_id}/{my_id}",method =  RequestMethod.GET)
	@ResponseBody
	public String getPersonPage(@PathVariable("user_id")Integer user_id,@PathVariable("my_id")Integer my_id){
		//请求自己和他人的个人页,如果是自己的个人页，则user_id和my_id都填自己的
		return userService.getPersonPage(user_id,my_id);
	}

	@RequestMapping(value = "/getHeadImageUrl",method =  RequestMethod.POST)
	@ResponseBody
	public String getHeadImageUrl(String telephone,String fade_name,String wechat_id){
		//根据fade_name或者telephone或者wechat_id获取头像，其他不发的要填null
		User user = new User();
		user.setTelephone(telephone);
		user.setFade_name(fade_name);
		user.setWechat_id(wechat_id);
		return userService.getHeadImageUrl(user);
	}
	
	@RequestMapping(value = "/getAddMessage/{user_id}",method =  RequestMethod.GET)
	@ResponseBody
	public String getAddMessage(@PathVariable("user_id")Integer user_id){
		return userService.getAddMessage(user_id);
	}	
	
	@RequestMapping(value = "/getAddContribute/{user_id}/{start}/{point}",method =  RequestMethod.GET)
	@ResponseBody
	public String getAddContribute(@PathVariable("user_id")Integer user_id,
			@PathVariable("start")Integer start,@PathVariable("point")String point){
		//获取“贡献者”队列
		return userService.getAddContribute(user_id, start,point);
	}	
	
	@RequestMapping(value = "/getAddFans/{user_id}/{start}/{point}",method =  RequestMethod.GET)
	@ResponseBody
	public String getAddFans(@PathVariable("user_id")Integer user_id,
			@PathVariable("start")Integer start,@PathVariable("point")String point){
		//获取新的粉丝队列
		return userService.getAddFans(user_id, start,point);
	}
	
	@RequestMapping(value = "/getAddComment/{user_id}/{start}/{point}",method =  RequestMethod.GET)
	@ResponseBody
	public String getAddComment(@PathVariable("user_id")Integer user_id,
			@PathVariable("start")Integer start,@PathVariable("point")String point){
		//获取新的队列
		return userService.getAddComment(user_id, start,point);
	}
	
	@RequestMapping(value = "/searchUser/{keyword}/{start}",method =  RequestMethod.GET)
	@ResponseBody	
	public String searchUser(@PathVariable("keyword")String keyword, @PathVariable("start")Integer start){
		return userService.searchUser(keyword,start);
	}
	
	@RequestMapping(value = "/getRecommendUser/{user_id}/{start}",method =  RequestMethod.GET)
	@ResponseBody	
	public String getRecommendUser(@PathVariable("user_id")Integer user_id, @PathVariable("start")Integer start){
		//感兴趣用户，分页，要设置上限，目前60个
		return userService.getTenRecommendUser(user_id,start);
	}
	
	//从融云处获取token
	@RequestMapping(value = "/getMessageToken/{user_id}", method = RequestMethod.GET)
	@ResponseBody
	public String getMessageToken(@PathVariable("user_id")Integer user_id)throws FadeException{
		//System.out.println("收到从融云处获取token的请求");
		return userService.getMessageToken(user_id);
	}	
	
	@RequestMapping(value = "/getOldContribute/{user_id}/{start}",method =  RequestMethod.GET)
	@ResponseBody
	public String getOldContribute(@PathVariable("user_id")Integer user_id,@PathVariable("start")Integer start){
		//获取“贡献者”队列
		System.out.println("收到getOldContribute的请求");
		return userService.getOldContribute(user_id, start);
	}	
	
	@RequestMapping(value = "/getOldFans/{user_id}/{start}",method =  RequestMethod.GET)
	@ResponseBody
	public String getOldFans(@PathVariable("user_id")Integer user_id,@PathVariable("start")Integer start){
		//获取新的粉丝队列
		return userService.getOldFans(user_id, start);
	}
	
	@RequestMapping(value = "/getOldComment/{user_id}/{start}",method =  RequestMethod.GET)
	@ResponseBody
	public String getOldComment(@PathVariable("user_id")Integer user_id,@PathVariable("start")Integer start){
		//获取新的队列
		return userService.getOldComment(user_id, start);
	}
	
	@RequestMapping(value = "/getFans/{user_id}/{my_id}/{start}",method =  RequestMethod.GET)
	@ResponseBody
	public String getFans(@PathVariable("user_id")Integer user_id,@PathVariable("my_id")Integer my_id,
			@PathVariable("start")Integer start){
		//个人页，分页查询20条粉丝
		return userService.getFans(user_id, my_id,start);
	}	
	
	@RequestMapping(value = "/getConcerns/{user_id}/{my_id}/{start}",method =  RequestMethod.GET)
	@ResponseBody
	public String getConcerns(@PathVariable("user_id")Integer user_id,@PathVariable("my_id")Integer my_id,
			@PathVariable("start")Integer start){
		//个人页，分页查询20条关注者
		return userService.getConcerns(user_id, my_id, start);
	}		

	@RequestMapping(value = "/getOriginRecommendUsers/{user_id}/{start}",method =  RequestMethod.GET)
	@ResponseBody
	public String getOriginRecommendUsers(@PathVariable("user_id")Integer user_id, @PathVariable("start")Integer start){
		//注册完后推荐的一批用户,start分页用的
		return userService.getOriginRecommendUsers(user_id, start);
	}
	
	@RequestMapping(value = "/getSchoolDepartment/{school_id}",method =  RequestMethod.GET)
	@ResponseBody
	public String getSchoolDepartment(@PathVariable("school_id")Integer school_id){
		//返回一个学校所有院系
		return userService.getSchoolDepartment(school_id);
	}	

	@RequestMapping(value = "/changePasswordTel",method =  RequestMethod.POST)
	@ResponseBody
	public String changePasswordTel(String telephone, String password){
		//客户端手机验证过后，修改密码
		return userService.changePasswordTel(telephone, password);
	}	

	@RequestMapping(value = "/addClientId", method =  RequestMethod.POST)
	@ResponseBody
	public String addClientId(Integer user_id,String clientid){
		//添加个推需要的clientid保存到一个hash结构中
		return userService.addClientId(user_id,clientid);
	}
}
