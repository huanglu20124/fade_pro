package com.hl.test;



import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.ognl.InappropriateExpressionException;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.fastjson.JSON;
import com.fade.controller.UserController;
import com.fade.domain.Comment;
import com.fade.domain.CommentMessage;
import com.fade.domain.CommentQuery;
import com.fade.domain.Note;
import com.fade.domain.SecondComment;
import com.fade.domain.User;
import com.fade.mapper.CommentDao;
import com.fade.mapper.NoteDao;
import com.fade.mapper.UserDao;
import com.fade.service.CommentService;
import com.fade.service.NoteService;
import com.fade.service.SolrService;
import com.fade.service.UserService;
import com.fade.util.Const;
import com.fade.util.JDBCUtil;
import com.fade.util.MD5Utils;
import com.fade.util.RedisUtil;
import com.fade.util.TimeUtil;
import com.fade.util.TokenUtil;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.AppMessage;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.LinkTemplate;

public class UserTest extends BaseTest {
/*	@Resource(name="userService")
	private UserService userService;*/
	
	@Resource(name="userDao")
	private UserDao userDao;

	@Resource(name = "noteDao")
	private NoteDao noteDao;
	
	@Resource(name = "commentDao")
	private CommentDao commentDao;
	
	@Resource(name = "tokenUtil")
	private TokenUtil tokenUtil;
	
	@Resource(name = "redisUtil")
	private RedisUtil redisUtil;
	
	private MockMvc mockMvc;
	
	@Autowired
	private UserController userController;
	
	@Resource(name = "noteService")
	private NoteService noteService;
	
	@Resource(name = "commentService")
	private CommentService commentService;
	
	@Resource(name = "solrService")
	private SolrService solrService;
	
	@Resource(name = "userService")
	private UserService userService;
	
    @Before
    public void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }
    
	@Test
	public void testAddUser() throws Exception {
		User user = new User();
		user.setFade_name("test3");
		user.setConcern_num(0);
		user.setFade_num(0);
		user.setFans_num(0);
		user.setRegister_time(com.fade.util.TimeUtil.getCurrentTime());
		user.setSchool_id(1);
		user.setSalt("xxxx");
		user.setPassword("xxxxxxxxxx");
		userService.registerByName(user, null);
		//userDao.addUser(user);
		//System.out.println(user.getUser_id());//自增主键自动设置到对象中
	}

	@Test
	public void testEditNickname() throws Exception {
		User user = new User();
		user.setUser_id(1);
		user.setNickname("aaaa");
		System.out.println(userDao.updateNickname(user));	//返回更新的行数
	}

	@Test
	public void testEditHead() throws Exception {
		System.out.println(userDao.updateHeadUrl("11111", 8));
	}

	@Test
	public void testGetStarUser() throws Exception {
		List<User>users = userDao.getStarUser(1);
		for(User user : users){
			System.out.println(user.getNickname() + "--" + user.getUser_id());
		}
	}
	
	@Test
	public void testGetUserByTelephone() throws Exception {
		User user = userDao.getUserByTel("13763359943");
		System.out.println(user);
	}
	
	@Test
	public void testgetUserByTelPwd() throws Exception {
		System.out.println(userDao.getUserByFadeName("fade_eb6e22"));
	}
	
	@Test
	public void testList() throws Exception {
		redisUtil.listRightPush("test", "note_1");
		redisUtil.listRightPush("test", "note_2");
		redisUtil.listRightPush("test", "note_3");
		redisUtil.listLeftPop("test");
		System.out.println(redisUtil.listGetAll("test"));
	}
		
	@Test
	public void testAddNote() throws Exception {
		for(int i = 0; i < 10; i++){
			Note note = new Note();
			note.setUser_id(43);
			note.setNickname("黄路");
			note.setNote_content("诚宜开张圣听，以光先帝遗德，恢弘志士之气，不宜妄自菲薄，引喻失义，以塞忠谏之路也。宫中府中，俱为一体，陟罚臧否，不宜异同。若有作奸犯科及为忠善者，宜付有司论其刑赏，以昭陛下平明之理，不宜偏私，使内外异法也。");
			note.setPost_time(TimeUtil.getCurrentTime());
			note.setHead_image_url("image/head/2018-02/55e5bbce-e.png");
			note.setUuid(UUID.randomUUID().toString());
			noteService.addNote(note, null);	
		}

	}

	@Test
	public void getTenNoteByTime() throws Exception {
		//System.out.println(noteService.getTenNoteByTime(49,0,1));
	}
	
	@Test
	public void testChangeSecond() throws Exception {
		Note note = new Note();
		note.setUser_id(108);
		note.setNickname("test");
		note.setTarget_id(2531);
		note.setType(1);
		System.out.println(noteService.changeSecond(note,108));
	}
	
	@Test
	public void testOnline() throws Exception {
		System.out.println(userService.online(43));
	}

	@Test
	public void findFans() throws Exception {
		System.out.println(userDao.getAllFansId(29));
	}
	
	@Test
	public void testGetMore() throws Exception {
		Note note = new Note();
		note.setNote_id(582);
		note.setTarget_id(0);
		List<Note>list = new ArrayList<>();
		System.out.println(noteService.getMoreNote(46, list));
	}

	@Test
	public void testAddComment() throws Exception {
		for(int i = 0; i < 20; i++){
			Comment comment = new Comment();
			comment.setNote_id(1127);
			comment.setHead_image_url("image/head/2018-01/83a21c6d-d.png");
			comment.setNickname("黄路");
			comment.setComment_content("这是一级评论" + i);
			comment.setUser_id(43);
			comment.setType(0);
			commentService.addComment(comment); 
		}
	}
	
	@Test
	public void testGetTenComment() throws Exception {
		System.out.println(commentService.getTenComment(207, 0));
	}
	
	@Test
	public void testaddSecondComment() throws Exception {
		SecondComment secondComment = new SecondComment();
		secondComment.setComment_id(16);
		secondComment.setUser_id(43);
		secondComment.setTo_user_id(43);
		secondComment.setTo_nickname("黄路");
		secondComment.setComment_content("这是一条二级评论，回复黄路的(第二条)");
		secondComment.setNickname("黄路");
		secondComment.setNote_id(172);
		System.out.println(commentService.addSecondComment(secondComment));
	}

	@Test
	public void testConcern() throws Exception {
		System.out.println(userService.concern(47, 45));
	    //System.out.println(userService.cancelConcern(16, 30));
	}

	@Test
	public void testLogin() throws Exception {
		User user = new User();
		user.setPassword("66e6056677d54c72b04a5aafb252f44a");
		user.setTelephone("13763359943");
		System.out.println(userDao.getUserByTelPwd(user));
	}
	
	@Test
	public void testSolr() throws Exception {
	    System.out.println(solrService.getTenUserKeyword("黄路", 0));
	}
	
    @Test
    public void Ctest() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/getTenNoteByTime/43/0/10"));
		Note note = new Note();
		note.setNote_id(539);
		note.setTarget_id(0);
		List<Note>list = new ArrayList<>();
		list.add(note);
		System.out.println("/getMoreNote/43/" + JSON.toJSONString(list));
    	//ResultActions resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/getMoreNote/43/[{'note_id':582,'target_id':0}]"));
        MvcResult mvcResult = resultActions.andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        System.out.println("=====客户端获得反馈数据:" + result);
        // 也可以从response里面取状态码，header,cookies...
//	        System.out.println(mvcResult.getResponse().getStatus());
    }
	
    @Test
	public void testAddCommentMessage() throws Exception {
    	for(int i = 0; i < 20; i++){
    		CommentMessage message = new CommentMessage();
    		message.setComment_content("xxx");
    		message.setFrom_id(45);
    		message.setTo_id(43);
    		message.setComment_id(502);
    		message.setComment_time(TimeUtil.getCurrentTime());
    		message.setNote_id(1205);
    		commentDao.addCommentMessage(message);
    	}
	}
    
    @Test
	public void insertSchool() throws Exception {
		Connection connection1 =   JDBCUtil.getConn("jdbc:mysql://119.23.229.19:3306/school?useUnicode=true&amp;characterEncoding=utf8",
				"student", "137137lu");
		Connection connection2 =   JDBCUtil.getConn("jdbc:mysql://119.23.229.19:3306/fade_pro?useUnicode=true&amp;characterEncoding=utf8",
				"student", "137137lu");	
		String sql1 = "select * from school";
		String sql2 = "insert into school values(?,?,?)";
		Statement statement1 = connection1.createStatement();
		ResultSet resultSet1 =   statement1.executeQuery(sql1);
		PreparedStatement statement2 = connection2.prepareStatement(sql2);
		while(resultSet1.next()){
			String name = resultSet1.getString("name");
			Integer cid = resultSet1.getInt("cid");
			Integer sid = resultSet1.getInt("sid");
			statement2.setInt(1, sid);
			statement2.setString(2, name);
			statement2.setInt(3, cid);
			statement2.execute();
			//System.out.println("插入成功：" + name);
		}
		connection1.close();
		connection2.close();
	}
    
    @Test
	public void insertDepartment() throws Exception {
		Connection connection1 =   JDBCUtil.getConn("jdbc:mysql://119.23.229.19:3306/school?useUnicode=true&amp;characterEncoding=utf8",
				"student", "137137lu");
		Connection connection2 =   JDBCUtil.getConn("jdbc:mysql://119.23.229.19:3306/fade_pro?useUnicode=true&amp;characterEncoding=utf8",
				"student", "137137lu");	
		String sql1 = "select * from department";
		String sql2 = "insert into department values(null,?,?)";
		Statement statement1 = connection1.createStatement();
		ResultSet resultSet1 =   statement1.executeQuery(sql1);
		PreparedStatement statement2 = connection2.prepareStatement(sql2);
		while(resultSet1.next()){
			String name = resultSet1.getString("name");
			Integer sid = resultSet1.getInt("sid");
			statement2.setInt(1, sid);
			statement2.setString(2, name);
			statement2.execute();
			//System.out.println("插入成功：" + name);
		}
		connection1.close();
		connection2.close();
	}
    
    @Test
	public void testPush() throws Exception {
    	  IGtPush push = new IGtPush(Const.GETUI_URL, Const.GETUI_APPKEY, Const.GETUI_MASTERSECRET);

          // 定义"点击链接打开通知模板"，并设置标题、内容、链接
          LinkTemplate template = new LinkTemplate();
          template.setAppId(Const.GETUI_APPID);
          template.setAppkey(Const.GETUI_APPKEY);
          template.setTitle("请填写通知标题");
          template.setText("请填写通知内容");
          template.setUrl("http://getui.com");

          List<String> appIds = new ArrayList<String>();
          appIds.add(Const.GETUI_APPID);

          // 定义"AppMessage"类型消息对象，设置消息内容模板、发送的目标App列表、是否支持离线发送、以及离线消息有效期(单位毫秒)
          AppMessage message = new AppMessage();
          message.setData(template);
          message.setAppIdList(appIds);
          message.setOffline(true);
          message.setOfflineExpireTime(1000 * 600);
          
          IPushResult ret = push.pushMessageToApp(message);
          System.out.println(ret.getResponse().toString());
	}
    
    @Test
	public void testAll() throws Exception {
    	List<Note>updateList =  new ArrayList<>();
/*    	Note note = new Note();
    	note.setNote_id(1490);
    	note.setTarget_id(1486);
    	note.setType(1);
    	updateList.add(note);*/
    	
    	testWrite(noteService.getTenNoteByTime(46, 0, 10, updateList),false);
	}
      
/*    @Test
	public void testUserCF() throws Exception {
    	MysqlDataSource dataSource = new MysqlDataSource();
    	dataSource.setURL("jdbc:mysql://119.23.229.19:3306/fade_pro?useUnicode=true&amp;characterEncoding=utf8mb4");
    	dataSource.setUser("student");
    	dataSource.setPassword("137137lu");
    	long start = System.currentTimeMillis();
    	JDBCDataModel dataModel = new MySQLJDBCDataModel(dataSource, "preference", "user_id", "note_id", "score", "time");
		// 基于用户相似度的协同过滤推荐实现
		try {
			//DataModel dataModel = new FileDataModel(new File("E:/study_and_work/project_files/fade/data.csv"));
			UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(dataModel);
			UserNeighborhood neighborhood = new NearestNUserNeighborhood(4, userSimilarity, dataModel);
			// 构建基于用户的推荐系统
			Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, userSimilarity);
			LongPrimitiveIterator iter = dataModel.getUserIDs();
			while (iter.hasNext()) {
				long uid = iter.nextLong();
				System.out.println(uid + "的邻居：" + Arrays.toString(neighborhood.getUserNeighborhood(uid)));
				// 得到指定用户的推荐结果
				List<RecommendedItem> recommendations = recommender.recommend(uid, 10);
				System.out.print("向用户" + uid + "推荐:");
				// 打印推荐结果
				for (RecommendedItem recommendation : recommendations) {
					System.out.print(recommendation.getItemID() + " ");
				}
				System.out.println();
			}
			System.out.println("总耗时" + System.currentTimeMillis() + start);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}*/
    
    @Test
	public void testAddPreference() throws Exception {
		int[] user_ids = new int []{43,45,46,47,48,51,52,53,54,56,57,58,62,63};
		for(int user_id : user_ids){
			int[]note_ids = randomArray(1395, 1574, 30);
			for(int note_id : note_ids){
				double a = new Random().nextInt(3) + 0.5;
				userDao.addPreference(user_id,note_id,a);
			}
		}
	}
    
	public int[] randomArray(int min,int max,int n){  
	    int len = max-min+1;  
	      
	    if(max < min || n > len){  
	        return null;  
	    }  
	      
	    //初始化给定范围的待选数组  
	    int[] source = new int[len];  
	       for (int i = min; i < min+len; i++){  
	        source[i-min] = i;  
	       }  
	         
	       int[] result = new int[n];  
	       Random rd = new Random();  
	       int index = 0;  
	       for (int i = 0; i < result.length; i++) {  
	        //待选数组0到(len-2)随机一个下标  
	           index = Math.abs(rd.nextInt() % len--);  
	           //将随机到的数放入结果集  
	           result[i] = source[index];  
	           //将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换  
	           source[index] = source[len];  
	       }  
	       return result;  
	}  
 
	private void testWrite(Object data, Boolean toJson) throws IOException{
    	File file = new File("C:/Users/road/Desktop/json.txt");
    	FileWriter writer = new FileWriter(file);
    	if(file.exists()){
    		writer.write("");
    	}else {
			file.createNewFile();
		}
    	String str = null;
    	if(toJson){
    		str = JSON.toJSONString(data);
    	}else {
			str = (String) data;
		}
    	System.out.println(str);
    	writer.write(str);
    	writer.flush();
    	writer.close();
	}
}
