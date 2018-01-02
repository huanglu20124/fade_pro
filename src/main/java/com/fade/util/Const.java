package com.fade.util;

public class Const {
	//微信小程序相关：
	public static final String APP_ID       = "wx3e4e1ddf8d7f6773";
	public static final String AppSecret    = "0918c2522745b1f0d0066021cb124374";
	//图片文件路径的存储规则为，IP加路径即为网络url，DATA_PATH加路径即为本地url
	public static final String BASE_IP      = "http://172.18.92.209:8080/fade_pro/";
	//public static final String DATA_PATH    = "E:/study_and_work/project_data/fade/";//文件根目录
	public static final String DATA_PATH    = "/usr/java/project/fade/";//文件根目录
	//在线用户set集合
	public static final String ONLINE_USERS = "online_users";
	
	//公有热门推送排行榜
	public static final String HOT_NOTES    = "hot_notes"; 
	
	//一个帖子的默认生存时间(单位：分钟)
	public static final long DEFAULT_LIFE   = 60l;
	
	//die_list，收集死亡的帖子，每隔一分钟清理一次
	public static final String DIE_LIST     = "die_list";
	
}
