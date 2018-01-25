package com.fade.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.fade.domain.Image;
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
	//获取十条增减秒，每个note包含有user_id,nickname,type
	List<Note> getTenRelayNote(@Param("note_id")Integer note_id,@Param("page")Integer page);
	//删除帖子
	Integer deleteNote(Integer note_id);
	//评论数量加一
	Integer updateCommentNum(Integer note_id);
	//找到用户所有活帖子的id
	List<Integer> getUserLiveNote(Integer user_id);
	//一次查找一百条,活的帖子加入到队列里,返回的Note仅仅包含note_id,target_id(仅限自己的)
	List<Note> getMuchMyNoteId(@Param("user_id")Integer user_id,@Param("search_id")Integer search_id);

	//得到个人主页的10条fade信息
	List<Note> getMyNote(@Param("user_id")Integer user_id, @Param("start")Integer start);
	//得到一个帖子有的全部图片
	List<Image> getNoteImage(Integer note_id);
	//续秒前检查是否已经续过
	Integer getNoteQueryChangeSecond(@Param("user_id")Integer user_id, @Param("target_id")Integer target_id);
	//添加一张示例图片，用于通知显示
	String getOneImage(Integer note_id);
	
	
}
