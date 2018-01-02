package com.fade.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.fade.domain.Comment;
import com.fade.domain.SecondComment;

public interface CommentDao {

	//获取比search_id大的num个评论
	List<Comment> getTenComment(@Param("note_id")Integer note_id, @Param("search_id")int search_id, @Param("num")int num);

	//获取一个评论的所有二级评论
	List<SecondComment> getSecondComment(Integer comment_id);

	//根据评论id获取一条评论
	Comment getCommentById(Integer comment_id);

	//添加一条一级评论
	Integer addComment(Comment comment);

	//添加二级评论
	Integer addSecondComment(SecondComment secondComment);

	//获得一个用户的所有一级评论的id
	List<Integer> getUserAllComment(Integer user_id);

	//用于通知页只返回一级评论,一次10条
	List<Comment> getAddComment(@Param("user_id")Integer user_id, @Param("start")Integer start);

}
