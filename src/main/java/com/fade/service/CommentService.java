package com.fade.service;

import com.fade.domain.Comment;
import com.fade.domain.CommentQuery;
import com.fade.domain.SecondComment;
import com.fade.exception.FadeException;

public interface CommentService {
	CommentQuery getTenComment(Integer note_id, Integer start) throws FadeException;

	String addComment(Comment parseObject);

	String addSecondComment(SecondComment parseObject);
}
