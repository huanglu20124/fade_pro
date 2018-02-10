package com.fade.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.fade.domain.Image;
import com.fade.domain.Note;
import com.fade.domain.Preference;

public interface NoteDao {

	//添加帖子,第二个参数为初始流传时间
	Integer addNote(Note note);
	//批量插入图片
	Integer addNoteImageBatch(Note note);
	//宣布帖子死亡(批处理)
	Integer updateNoteDie(@Param("note_ids")List<Integer>note_ids);
	//一次查找一百条,活的帖子加入到队列里,返回的Note仅仅包含note_id,target_id
	List<Note> getMuchNoteId(@Param("user_id")Integer user_id,@Param("search_id")Integer search_id);
	//更新加一秒的数量
	Integer updateNoteAddNum(@Param("note_id")Integer note_id, @Param("add_num")Integer add_num);
	//更新减一秒的数量
	Integer updateNoteSubNum(@Param("note_id")Integer note_id, @Param("sub_num")Integer sub_num);
	//根据帖子id获取一个帖子全部内容，后期这个方法可能要缓存
	Note getNoteById(Integer note_id);
	//获取type，查询是否操作过一个帖子
	Integer getNoteCheckAction(@Param("user_id")Integer user_id,@Param("note_id")Integer note_id);
	//获取十条增减秒，每个note包含有user_id,nickname,type
	List<Note> getTenRelayNote(@Param("note_id")Integer note_id,@Param("page")Integer page);
	//删除帖子
	Integer deleteNote(Integer note_id);
	//评论数量加一
	Integer updateCommentNum(@Param("note_id")Integer note_id, @Param("type")Integer type);
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
	//首页信息流，获取全部新增的帖子
	List<Note> getAddNote(@Param("user_id")Integer user_id, @Param("start")Integer start);
	//10条的获取活着的自己的动态，和首页一样，仅仅返回note_id和start
	List<Note> getLiveNote(@Param("user_id")Integer user_id, @Param("start")int start);
	//设置原贴的流传时间
	void updateLiveTime(@Param("note_id")Integer note_id, @Param("liveTime")long liveTime);
	//更新单个帖子的生死情况
	void updateNoteDieSingle(Integer note_id);
	//找到全部增秒的人
	List<Note> getAddAll(Integer note_id);
	//找到全部减秒的人
	List<Note> getSubAll(Integer note_id);
	//得到某个续秒贴
	Note getRelayNoteById(Integer note_id);
	//将某个原贴的全部抓发帖is_die置为0
	void updateRelayNoteDie(Integer target_id);
	//得到某条偏好
	Preference getPreference(@Param("note_id")Integer note_id, @Param("user_id")Integer user_id);
	//加入某条偏好
	void addPreference(Preference preference);
	//更新偏好分数
	void updatePreference(@Param("note_id")Integer note_id, @Param("user_id")Integer user_id,
			@Param("score")Double score);
	
	
}
