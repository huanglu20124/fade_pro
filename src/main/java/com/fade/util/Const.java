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
	
	//一个帖子的默认生存时间(单位：秒)
	public static final long DEFAULT_LIFE   = 3600l;
	
	//die_list，收集死亡的帖子，每隔一分钟清理一次
	public static final String DIE_LIST     = "die_list";
	
	//收集需要添加到索引数据库的帖子
	public static final String INDEX_LIST   = "index_list";
	
	//需要更新数据的任务
	public static final String PREFERENCE_LIST = "preference_list";
	
	//默认院系及默认学校
	public static final int DEFAULT_SCHOOL_ID = 1;
	public static final int DEFAULT_DEPARTMENT_ID = 100000;
	//热门用户的阈值
	public static final int HOT_USER_THRESHOLD = 0;
	
	//融云的app秘钥
	public static final String RONG_APP_KEY ="0vnjpoad0gn2z";
	public static final String RONG_APP_SECRET ="Sk52dbUr6eg";
	public static final String RONG_URL     ="http://api.cn.ronghub.com";
	
	//以下是推荐算法计算评分：
	public static final double BASE_SCORE = 1;//基础分
	public static final double SCHOOL_SCORE = 1; //同学校加1分
	public static final double SECOND_SCORE = 1; //加减秒加1分
	public static final double DETAIL_SCORE = 0.5; //打开详情页一次加0.5
	public static final double COMMENT_SCORE = 1; //评论一次加1
	public static final double FAV_SCORE = 1; //收藏一次加2
	
	//list1个人帖子加载队列, list1_ + user_id
	//list2个人新帖子加载队列,list2_ + user_id
	//list3帖子评论加载队列, list3_ + note_id
	//list4详情页续秒增秒列表加载队列，其中为0的代表的是帖子本人点赞，需要再查询本人的信息,list4_ + note_id
	//add_  增秒列表  zset member为user， value为note_id
	//sub_  减秒列表，每次增秒或者减秒，缓存都重置为15l zset member为user， value为note_id
	
	//个推配置
	public static final String GETUI_APPID = "TLfCLo6wYu76VgJzJuhy89";
	public static final String GETUI_APPSECRET = "5UVWRUoUJBAqBCNUAocDS";
	public static final String GETUI_APPKEY = "4Oo126YfVO7e3kYu7hlAZA";
	public static final String GETUI_MASTERSECRET = "JKsshW3FGNATVmJ5MkuHv1";
	public static final String GETUI_URL = "http://sdk.open.api.igexin.com/apiex.htm";
	//保存在redis的个推用户cid池hash
	public static final String GETUI_CIDS = "cids";
	
	
}
