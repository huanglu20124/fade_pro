package com.fade.domain;

public class DetailPage {
	//帖子详情页
	private NoteQuery noteQuery; //包含10条增减秒列表，以及下一次分页查询的start
	private CommentQuery commentQuery; //10条评论列表，以及下一次分页查询的start
	private Note note;//如果是从首页跳转过来，返回的note只有comment_num,add_num,sub_num,fetchTime;
	                  //如果是从其他页面跳转过来，则返回完整note
	
	public NoteQuery getNoteQuery() {
		return noteQuery;
	}
	public void setNoteQuery(NoteQuery noteQuery) {
		this.noteQuery = noteQuery;
	}
	public CommentQuery getCommentQuery() {
		return commentQuery;
	}
	public void setCommentQuery(CommentQuery commentQuery) {
		this.commentQuery = commentQuery;
	}
	public Note getNote() {
		return note;
	}
	public void setNote(Note note) {
		this.note = note;
	}
	
	
}
