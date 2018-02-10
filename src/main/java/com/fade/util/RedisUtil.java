package com.fade.util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.remoting.RemoteTimeoutException;

public class RedisUtil{

	// 8月26号更新，统一redis连接池，加入spring
	private RedisTemplate<String, Object> redisTemplate;
	public RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void addKey(String key, Object value) {
		//增加key
		redisTemplate.opsForValue().set(key, value);
	}
	
	public void addKey(String key, Object value,Long timeout,TimeUnit unit){
		redisTemplate.opsForValue().set(key, value, timeout, unit);
	}
	
	public void deleteKey(String key) {
		//删除key
		redisTemplate.delete(key);
	}
	
	public void addSelf(String key){
		//自增
		redisTemplate.opsForValue().increment(key, 1);
	}
	
	public Object getValue(String key) {
		//得到对应的value
		return redisTemplate.opsForValue().get(key);
	}
	
	public List<String> listGetAll(String array_name) {
		//得到整个队列，一项为String
		List<Object> origins=  redisTemplate.opsForList().range(array_name, 0l, -1l);
		List<String>keys = new ArrayList<>();
		for(Object object : origins){
			keys.add((String)object);
		}
		return keys;
	}
	
	public List<Object> listGetAllObject(String array_name) {
		//得到整个队列，返回原来的Object
		return redisTemplate.opsForList().range(array_name, 0l, -1l);
	}

	public List<String> listGetRange(String array_name,Long start,Long end) {
		//得到整个队列
		List<Object> origins=  redisTemplate.opsForList().range(array_name, start, end);
		List<String>keys = new ArrayList<>();
		for(Object object : origins){
			keys.add((String)object);
		}
		return keys;
	}
	
	public void listLeftPush(String array_name,Object value){
		//插入到队列左边
		redisTemplate.opsForList().leftPush(array_name, value);
	}

	public void listLeftPushAll(String array_name,Object...values){
		//插入到队列左边
		redisTemplate.opsForList().leftPushAll(array_name, values);
	}

	public void listRightPushAll(String array_name,Object...values){
		//插入到队列右边
		redisTemplate.opsForList().rightPushAll(array_name, values);
	}	
	
	public Object listLeftPop(String array_name){
		return redisTemplate.opsForList().leftPop(array_name);
	}
	
	public Object listRightPop(String array_name){
		return redisTemplate.opsForList().rightPop(array_name);
	}	
	
	public void listRightPush(String array_name,Object value){
		//插入到队列左边
		redisTemplate.opsForList().rightPush(array_name, value);
	}	

	public void listRemoveValue(String array_name,String value){
		//删除队列中的元素
		redisTemplate.opsForList().remove(array_name, 1, value);
	}
	
	public void zsetAddKey(String key, Object value, Double score){
		//加入到zset里面
		redisTemplate.opsForZSet().add(key, value, score);
	}
	
	public void zsetDeleteKey(String key, Object value){
		redisTemplate.opsForZSet().remove(key, value);
	}
	
	public Long listGetSize(String key){
		return redisTemplate.opsForList().size(key);
	}

	public void setAddKey(String key, Object values){
		redisTemplate.opsForSet().add(key, values);
	}
	
	public void setAddKeyMul(String key, Object... values){
		redisTemplate.opsForSet().add(key, values);
	}
	
	public Set<String> setGetAll(String key){
		Set<Object>origin = redisTemplate.opsForSet().members(key);
		Set<String>set = new HashSet<>();
		Iterator<Object>iterator = origin.iterator();
		while(iterator.hasNext()){
			set.add((String)iterator.next());
		}
		return set;
	}
	
	public Boolean setIsMember(String key, String value){
		return redisTemplate.opsForSet().isMember(key, value);
	}
	
	public void setKeyTime(String key,long timeout,TimeUnit unit){
		redisTemplate.expire(key, timeout, unit);
	}

	public long getKeyTime(String key, TimeUnit unit){
		return redisTemplate.getExpire(key, unit);
	}
	
	public boolean setIsContain(String key,Object value){
		return redisTemplate.opsForSet().isMember(key, value);
	}
	
	public void setRemove(String key,Object value){
		redisTemplate.opsForSet().remove(key,value);
	}

/*	public Long setSize(String key){
		return redisTemplate.opsForSet().size(key);
	}*/
	
/*	public Set<Integer> setGetAllInt(String key){
		Set<Object>objects =  redisTemplate.opsForSet().members(key);
		Set<Integer>set = new HashSet<>();
		Iterator<Object>iterator = objects.iterator();
		while (iterator.hasNext()) {
			set.add((Integer)iterator.next());
		}
		return set;
	}*/
	
	
	public List<String> zsetRange(String key ,long start, long end){
		Set<Object>set =  redisTemplate.opsForZSet().reverseRange(key, start, end);
		List<String>list = new ArrayList<>();
		Iterator<Object>iterator = set.iterator();
		while (iterator.hasNext()) {
			String value = (String) iterator.next();
			list.add(value);
		}
		return list;
	}
	
	public List<Integer> zsetRangeInt(String key ,long start, long end){
		//分数从低到高
		Set<Object>set =  redisTemplate.opsForZSet().range(key, start, end);
		List<Integer>list = new ArrayList<>();
		Iterator<Object>iterator = set.iterator();
		while (iterator.hasNext()) {
			list.add((Integer)iterator.next());
		}
		return list;
	}
	
	public Boolean zsetisMember(String key,Object member){
		Object ans = redisTemplate.opsForZSet().rank(key, member);
		return ans == null ? false : true;
	}	
	
	public List<Integer> zsetGetAllIntList(String key){
		//按照倒序排序加入
		Set<Object>set =  redisTemplate.opsForZSet().reverseRange(key, 0, -1);
		List<Integer>list = new ArrayList<>();
		Iterator<Object>iterator = set.iterator();
		while (iterator.hasNext()) {
			list.add((Integer) iterator.next());
		}
		return list;
	}	
	
	public long zsetGetSize(String key){
	    return redisTemplate.opsForZSet().size(key);
	}
	
	public Double zsetScore(String key, Object object){
		return redisTemplate.opsForZSet().score(key, object);
	}

}
