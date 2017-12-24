package com.fade.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.fade.domain.Note;

public interface NoteDao {

	//添加帖子
	Integer addNote(Note note);
	//批量插入图片
	Integer addNoteImageBatch(Note note);
	//宣布帖子死亡(批处理)
	Integer updateNoteDie(@Param("note_ids")List<Integer>note_ids);
	//一次查找一百条,活的帖子加入到队列里
	List<Integer> getMuchNoteId(@Param("user_id")Integer user_id,@Param("search_id")Integer search_id);
}
