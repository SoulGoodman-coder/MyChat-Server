package com.mychat.redis;

import com.mychat.entity.dto.SysSettingDto;
import com.mychat.entity.dto.TokenUserInfoDto;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * projectName: com.mychat.redis
 * author:  SoulGoodman-coder
 * description: redis操作常用方法
 */

@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    @Value("${contants.REDIS_KEY_USER_HEARTBEAT}")
    private String REDIS_KEY_USER_HEARTBEAT;

    @Value("${contants.REDIS_KEY_USER_TOKEN}")
    private String REDIS_KEY_USER_TOKEN;

    @Value("${contants.TIME_SECOND_1Day}")
    private Integer TIME_SECOND_1Day;

    @Value("${contants.REDIS_KEY_USER_TOKEN_USERID}")
    private String REDIS_KEY_USER_TOKEN_USERID;

    @Value("${contants.REDIS_KEY_SYS_SETTING}")
    private String REDIS_KEY_SYS_SETTING;

    @Value("${contants.REDIS_TIME_HEART_BEAT_EXPIRES}")
    private Integer REDIS_TIME_HEART_BEAT_EXPIRES;

    @Value("${contants.REDIS_KEY_USER_CONTACT}")
    private String REDIS_KEY_USER_CONTACT;

    /**
     * 获取用户心跳（确定客户端是否已登录）
     * @param userId    用户id
     * @return          时间戳
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisUtils.get(REDIS_KEY_USER_HEARTBEAT + userId);
    }

    /**
     * 保存用户心跳（确定客户端是否保持连接）
     * @param userId    用户id
     */
    public void saveUserHeartBeat(String userId) {
        redisUtils.set(REDIS_KEY_USER_HEARTBEAT + userId, System.currentTimeMillis(), REDIS_TIME_HEART_BEAT_EXPIRES);
    }

    /**
     * 删除用户心跳
     * @param userId    用户id
     */
    public void removeUserHeartBeat(String userId) {
        redisUtils.delete(REDIS_KEY_USER_HEARTBEAT + userId);
    }

    /**
     * 保存TokenUserInfoDto
     * @param tokenUserInfoDto  TokenUserInfoDto
     */
    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto) {
        // token: tokenUserInfoDto
        redisUtils.set(REDIS_KEY_USER_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, TIME_SECOND_1Day*3);
        // userid: token
        redisUtils.set(REDIS_KEY_USER_TOKEN_USERID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), TIME_SECOND_1Day*3);
    }

    /**
     * 根据token获取TokenUserInfoDto
     * @param token     用户token
     * @return TokenUserInfoDto
     */
    public TokenUserInfoDto getTokenUserInfoDto(String token) {
        return  (TokenUserInfoDto) redisUtils.get(REDIS_KEY_USER_TOKEN + token);
    }

    /**
     * 根据userId获取TokenUserInfoDto
     * @param userId    用户id
     * @return TokenUserInfoDto
     */
    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String userId) {
        String token = (String) redisUtils.get(REDIS_KEY_USER_TOKEN_USERID + userId);
        return  getTokenUserInfoDto(token);
    }

    /**
     * 获取系统设置
     * @return SysSettingDto
     */
    public SysSettingDto getSysSettingDto() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(REDIS_KEY_SYS_SETTING);
        sysSettingDto = sysSettingDto == null ? new SysSettingDto() : sysSettingDto;
        return sysSettingDto;
    }

    /**
     * 保存系统设置
     * @param sysSettingDto 新的系统设置对象
     */
    public void saveSysSetting(SysSettingDto sysSettingDto){
        redisUtils.set(REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    /**
     * 保存用户的联系人id
     * @param userId            当前用户id
     * @param contactId         用户联系人的id
     */
    public void saveUserContact(String userId, String contactId) {
        List<String> userContactIdList = getUserContactIdList(userId);
        // 若用户联系人id已在redis列表中，则无需再添加
        if (userContactIdList.contains(contactId)){
            return;
        }
        redisUtils.lpush(REDIS_KEY_USER_CONTACT + userId, contactId, TIME_SECOND_1Day*3);
    }

    /**
     * 批量保存用户的联系人id
     * @param userId            当前用户id
     * @param contactIdList     用户联系人的id列表
     */
    public void saveUserContactBatch(String userId, List<String> contactIdList) {
        redisUtils.lpushAll(REDIS_KEY_USER_CONTACT + userId, contactIdList, TIME_SECOND_1Day*3);
    }

    /**
     * 清空用户联系人列表
     * @param userId            当前用户id
     */
    public void clearUserContact(String userId) {
        redisUtils.delete(REDIS_KEY_USER_CONTACT + userId);
    }

    /**
     * 获取用户联系人id列表
     * @param userId            当前用户id
     */
    public List<String> getUserContactIdList(String userId) {
        return redisUtils.getList(REDIS_KEY_USER_CONTACT + userId);
    }

    public void removeUserTokenByUserId(String userId) {
        String token = (String) redisUtils.get(REDIS_KEY_USER_TOKEN_USERID + userId);
        if (null == token){
            return;
        }
        redisUtils.delete(REDIS_KEY_USER_TOKEN_USERID + userId);
        redisUtils.delete(REDIS_KEY_USER_TOKEN + token);
    }

    /**
     * 删除指定用户的指定联系人缓存
     * @param userId        当前用户
     * @param contactId     要删除的联系人
     */
    public void removeUserContact(String userId, String contactId) {
        redisUtils.removeOne(REDIS_KEY_USER_CONTACT + userId, contactId);
    }
}
