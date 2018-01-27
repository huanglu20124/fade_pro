 package com.fade.controller;

import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSON;
import com.fade.domain.Note;
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

	@RequestMapping(value = "/getTenNoteByTime/{user_id}/{start}/{concern_num}", method = RequestMethod.GET)
	@ResponseBody
	public String getTenNoteByTime(@PathVariable("user_id")Integer user_id,
			@PathVariable("start")Integer start, @PathVariable("concern_num")Integer concern_num){
		//初次加载以及向下翻，  按照时间顺序上拉加载10条，一开始start填0或者不发
		//concern_num是sql语句判断用的
		return noteService.getTenNoteByTime(user_id,start,concern_num);
	}
	
	@RequestMapping(value = "/getMoreNote/{user_id}/{updateList}", method = RequestMethod.GET)
	@ResponseBody
	public String getMoreNote(@PathVariable("user_id")Integer user_id, @PathVariable("updateList")String updateList){
		System.out.println("user_id=" + user_id + " updateList=" + updateList);
		//顶部下拉刷新，同时更新之前发过的帖子的信息,updateList的一项要包含note_id,target_id
		return noteService.getMoreNote(user_id,JSON.parseArray(updateList, Note.class));
	}	

	@RequestMapping(value = "/changeSecond", method = RequestMethod.POST)
	@ResponseBody
	public String changeSecond(String note) throws FadeException{
		System.out.println("收到增减秒的请求");
		//增和减秒的请求，实则是增加帖子,需要发的属性有user_id,nickname,target_id,type,head_image_url 
		return noteService.changeSecond(JSON.parseObject(note,Note.class));
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
		//获取个人帖子信息的请求，10条一次
		System.out.println("获取个人帖子信息的请求");
		return noteService.getMyNote(user_id,start);
	}	

	@RequestMapping(value = "/getOtherPersonNote/{user_id}/{my_id}/{start}", method = RequestMethod.GET)
	@ResponseBody
	public String getOtherPersonNote(@PathVariable("user_id")Integer user_id,@PathVariable("my_id")Integer my_id,
			@PathVariable("start")Integer start){
		//获取个人帖子信息的请求，10条一次
		System.out.println("获取他人帖子信息的请求");
		return noteService.getOtherPersonNote(user_id,my_id,start);
	}	

	@RequestMapping(value = "/getFullNote/{note_id}/{user_id}", method = RequestMethod.GET)
	@ResponseBody
	public String getFullNote(@PathVariable("note_id")Integer note_id,@PathVariable("user_id")Integer user_id){
		//获取一个首页完整帖子的请求
		System.out.println("获取一个首页完整帖子的请求");
		return noteService.getFullNote(note_id,user_id);
	}		
	
	@RequestMapping(value = "/searchAliveNote/{keyword}/{page}/{isAlive}/{user_id}",method =  RequestMethod.GET)
	@ResponseBody	
	public String searchNote(@PathVariable("keyword")String keyword,@PathVariable("page")Integer page,
			@PathVariable("isAlive")Integer isAlive, @PathVariable("user_id")Integer user_id){
		return noteService.searchNote(keyword,page,isAlive,user_id);
	}
	
	
}
