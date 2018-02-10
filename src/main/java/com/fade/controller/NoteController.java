 package com.fade.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HttpServletBean;

import com.alibaba.fastjson.JSON;
import com.fade.domain.Note;
import com.fade.domain.TokenModel;
import com.fade.exception.FadeException;
import com.fade.service.NoteService;

@Controller
public class NoteController {
	
	@Resource(name = "noteService")
	private NoteService noteService;
	
	@RequestMapping(value = "/addNote", method = RequestMethod.POST)
	@ResponseBody
	public String addNote(String note,@RequestParam(name="file",required=false)MultipartFile[]files) throws FadeException{
		//增加的一定是原创帖，需要发的属性有user_id,nickname,note_content,head_image_url
		System.out.println("收到增加帖子的请求");
		return noteService.addNote(JSON.parseObject(note, Note.class),files); 
	}

	@RequestMapping(value = "/getTenNoteByTime", method = RequestMethod.POST)
	@ResponseBody
	public String getTenNoteByTime(Integer user_id,Integer start, Integer concern_num,String updateList){
		//初次加载以及向下翻，  按照时间顺序上拉加载10条，一开始start填0或者不发
		//concern_num是sql语句判断用的
		//updateList用来判断是去除重复转发帖,必须包含note_Id,target_id,type
		return noteService.getTenNoteByTime(user_id,start,concern_num,JSON.parseArray(updateList, Note.class));
	}
	
	@RequestMapping(value = "/getMoreNote", method = RequestMethod.POST)
	@ResponseBody
	public String getMoreNote(Integer user_id,String updateList){
		System.out.println("user_id=" + user_id + " updateList=" + updateList);
		//顶部下拉刷新，同时更新之前发过的帖子的信息,updateList的一项要包含note_id,target_id
		return noteService.getMoreNote(user_id,JSON.parseArray(updateList, Note.class));
	}	

	@RequestMapping(value = "/changeSecond", method = RequestMethod.POST)
	@ResponseBody
	public String changeSecond(HttpServletRequest request,  String note) throws FadeException{
		System.out.println("收到增减秒的请求");
		//特殊处理，通过tokenModel里面的user_id判断是否是本人
	    String tokenModelStr = request.getHeader("tokenModel");
	    TokenModel model = JSON.parseObject(tokenModelStr,TokenModel.class);
		//增和减秒的请求，实则是增加帖子,需要发的属性有user_id,nickname,target_id,type,head_image_url 
		return noteService.changeSecond(JSON.parseObject(note, Note.class),
				model.getUser_id());
	}

	@RequestMapping(value = "/getNotePage/{note_id}/{user_id}/{getFull}", method = RequestMethod.GET)
	@ResponseBody
	public String getNotePage(@PathVariable("note_id")Integer note_id,
			@PathVariable("user_id")Integer user_id,
			@PathVariable("getFull")Integer getFull) throws FadeException{
		System.out.println("收到获取详情页的请求");
		//详情页加载，10条续减一秒，10个评论.getFull为0只返回note的三个数量及获取时间信息，为1的话返回整个note
		return noteService.getNotePage(note_id,user_id,getFull);
	}

	@RequestMapping(value = "/deleteNote/{note_id}/{user_id}", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteNote(@PathVariable("note_id")Integer note_id,@PathVariable("user_id")Integer user_id){
		//删除帖子的请求
		System.out.println("收到删除帖子的请求");
		return noteService.deleteNote(note_id,user_id);
	}
	
	@RequestMapping(value = "/getMyNote/{user_id}/{start}", method = RequestMethod.GET)
	@ResponseBody
	public String getMyNote(@PathVariable("user_id")Integer user_id,@PathVariable("start")Integer start){
		//获取个人原创帖子信息的请求，10条一次
		System.out.println("获取个人帖子信息的请求");
		return noteService.getMyNote(user_id,start);
	}	

	@RequestMapping(value = "/getOtherPersonNote/{user_id}/{my_id}/{start}", method = RequestMethod.GET)
	@ResponseBody
	public String getOtherPersonNote(@PathVariable("user_id")Integer user_id,@PathVariable("my_id")Integer my_id,
			@PathVariable("start")Integer start){
		//获取个人原创帖子信息的请求，10条一次
		System.out.println("获取他人帖子信息的请求");
		return noteService.getOtherPersonNote(user_id,my_id,start);
	}	

	@RequestMapping(value = "/getLiveNote/{user_id}/{my_id}/{start}", method = RequestMethod.GET)
	@ResponseBody
	public String getLiveNote(@PathVariable("user_id")Integer user_id,@PathVariable("my_id")Integer my_id,
			@PathVariable("start")Integer start){
		//获取自己活着他人的动态帖子
		System.out.println("获取自己活着他人的动态帖子的请求");
		return JSON.toJSONString(noteService.getLiveNote(user_id,my_id,start));
	}	
	
	@RequestMapping(value = "/getFullNote/{note_id}/{user_id}", method = RequestMethod.GET)
	@ResponseBody
	public String getFullNote(@PathVariable("note_id")Integer note_id,@PathVariable("user_id")Integer user_id){
		//获取一个首页完整帖子的请求
		System.out.println("获取一个首页完整帖子的请求");
		return noteService.getFullNote(note_id,user_id);
	}		
	
	@RequestMapping(value = "/searchNote/{keyword}/{start}/{isAlive}/{user_id}",method =  RequestMethod.GET)
	@ResponseBody	
	public String searchNote(@PathVariable("keyword")String keyword,@PathVariable("start")Integer start,
			@PathVariable("isAlive")Integer isAlive, @PathVariable("user_id")Integer user_id){
		return noteService.searchNote(keyword,start,isAlive,user_id);
	}
	
	@RequestMapping(value = "/getConcernSecond/{user_id}/{target_id}/{start}/{type}",method =  RequestMethod.GET)
	@ResponseBody
	public String getConcernSecond(@PathVariable("user_id")Integer user_id,@PathVariable("target_id")Integer target_id,
			@PathVariable("start")Integer start,@PathVariable("type")Integer type){
		//点开折叠列表，获取20条记录, type为1和2，分别代表增和减
		return noteService.getConcernSecondNote(user_id, target_id, start, type);
	}

	@RequestMapping(value = "/getAllSecond/{user_id}/{target_id}/{start}/{type}",method =  RequestMethod.GET)
	@ResponseBody
	public String getAllSecond(@PathVariable("user_id")Integer user_id,@PathVariable("target_id")Integer target_id,
			@PathVariable("start")Integer start,@PathVariable("type")Integer type){
		//点开折叠列表，获取20条记录, type为1和2，分别代表增和减
		return noteService.getAllSecond(user_id, target_id, start, type);
	}
	
	
	@RequestMapping(value = "/getTenRelayNote/{note_id}/{user_id}/{start}",method =  RequestMethod.GET)
	@ResponseBody
	public String getTenRelayNote(@PathVariable("note_id")Integer note_id, @PathVariable("user_id")Integer user_id, 
			@PathVariable("start")Integer start){
		return JSON.toJSONString(noteService.getTenRelayNote(note_id, user_id, start));
	}
}
