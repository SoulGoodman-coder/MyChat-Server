package com.mychat.redis;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * projectName: com.mychat.redis
 * author:  SoulGoodman-coder
 * description: redis操作工具类
 */

@Component("redisUtils")
@Slf4j
public class RedisUtils<T> {
    @Resource
    private RedisTemplate<String, T> redisTemplate;

    /**
     * 删除key
     * @param key   可以传一个或多个
     */
    public void delete(String... key){
        if(key != null && key.length > 0){
            if(key.length == 1){
                redisTemplate.delete(key[0]);
            }else {
                redisTemplate.delete(List.of(key));
            }
        }
    }

    /**
     * 根据key获取字符串
     * @param key   key
     * @return      key对应字符串
     */
    public T get(String key){
        return key == null? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 存入字符串
     * @param key       健
     * @param value     值
     * @return          ture|false
     */
    public boolean set(String key, T value){
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }catch (Exception e){
            log.error("设置redisKey:{},value:{}失败！",key,value);
            return false;
        }
    }


    /**
     * 存入字符串并设置过期时间
     * @param key       健
     * @param value     值
     * @param timeout   过期时间（秒）  当timeout小于等于0时，设置为无限期
     * @return          ture|false
     */
    public boolean set(String key, T value, long timeout){
        try {
            if (timeout > 0) {
                redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            }else {
                set(key, value);
            }
            return true;
        }catch (Exception e){
            log.error("设置redisKey:{},value:{}失败！",key,value);
            return false;
        }
    }

    /**
     * 设置过期时间
     * @param key       健
     * @param timeout   过期时间（秒）
     * @return
     */
    public boolean expire(String key, long timeout){
        try {
            if (timeout > 0){
                redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 获取列表中所有元素
     * @param key       键
     * @return          列表元素
     */
    public List<T> getList(String key){
        return key == null?null:redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 向列表中插入元素（从左边插入）
     * @param key       键
     * @param value     值
     * @param timeout   过期时间
     * @return          ture|false
     */
    public boolean lpush(String key, T value, long timeout){
        try {
            // 从列表左边插入值
            redisTemplate.opsForList().leftPush(key, value);
            if (timeout > 0){
                expire(key, timeout);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除键为key的列表中指定元素
     * @param key       键
     * @param value     值
     * @return          修改的元素数量
     */
    public long removeOne(String key, Object value){
        try {
            // count> 0：删除等于从左到右值为value的第一个元素；count< 0：删除等于从右到左值为value的第一个元素；count = 0：删除等于value的所有元素。
            Long remove = redisTemplate.opsForList().remove(key, 1, value);
            return remove;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除键为key的列表中指定元素
     * @param key       键
     * @param value     值
     * @return          修改的元素数量
     */
    public long removeAll(String key, Object value){
        try {
            // count> 0：删除等于从左到右值为value的第一个元素；count< 0：删除等于从右到左值为value的第一个元素；count = 0：删除等于value的所有元素。
            Long remove = redisTemplate.opsForList().remove(key, -1, value);
            return remove;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 向列表中插入多个元素（从左边插入）
     * @param key       键
     * @param values    List类型的值
     * @param timeout   过期时间
     * @return
     */
    public boolean lpushAll(String key, List<T> values, long timeout){
        try {
            redisTemplate.opsForList().leftPushAll(key, values);
            if (timeout > 0){
                expire(key, timeout);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
