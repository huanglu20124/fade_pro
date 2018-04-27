package com.fade.controller;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.fade.domain.Comment;
import com.fade.domain.CommentQuery;
import com.fade.domain.SecondComment;
import com.fade.exception.FadeException;
import com.fade.service.CommentService;

@Controller
public class CommentController {
	
	@Resource(name = "commentService")
	private CommentService commentService;
	
	@RequestMapping(value = "/getTenComment/{note_id}/{start}", method = RequestMethod.GET)
	@ResponseBody
	public String getTenComment(@PathVariable("note_id")Integer note_id,@PathVariable("start")Integer start) throws FadeException{
		//详情页加载，10条续减一秒，10个评论
		CommentQuery query =  commentService.getTenComment(note_id,start);
		return JSON.toJSONString(query);
	}	
	
	@RequestMapping(value = "/addComment", method = RequestMethod.POST)
	@ResponseBody	
	public String addComment(String comment){
		//comment要有type
		System.out.println("comment=" + comment);
		return commentService.addComment(JSON.parseObject(comment, Comment.class));
	}
	
	@RequestMapping(value = "/addSecondComment", method = RequestMethod.POST)
	@ResponseBody	
	public String addSecondComment(String secondComment){
		return commentService.addSecondComment(JSON.parseObject(secondComment, SecondComment.class));
	}
}
