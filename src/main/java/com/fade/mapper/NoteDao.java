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
	//一次查找一百条,活的帖子加入到队列里,返回的Note仅仅包含note_id,target_id
	List<Note> getMuchNoteId(@Param("user_id")Integer user_id,@Param("search_id")Integer search_id);
	//更新加一秒的数量
	Integer updateNoteAddNum(@Param("user_id")Integer user_id, @Param("add_num")Integer add_num);
	//更新减一秒的数量
	Integer updateNoteSubNum(@Param("user_id")Integer user_id, @Param("sub_num")Integer sub_num);
	//根据帖子id获取一个帖子全部内容，后期这个方法可能要缓存
	Note getNoteById(Integer note_id);
	//获取type，查询是否操作过一个帖子
	Integer getNoteCheckAction(@Param("user_id")Integer user_id,@Param("note_id")Integer note_id);
}
