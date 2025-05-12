package com.mychat.redis;

import com.mychat.entity.dto.SysSettingDto;
import com.mychat.entity.dto.TokenUserInfoDto;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Value("${contants.REDIS_TIME_1Day}")
    private Integer REDIS_TIME_1Day;

    @Value("${contants.REDIS_KEY_USER_TOKEN_USERID}")
    private String REDIS_KEY_USER_TOKEN_USERID;

    @Value("${contants.REDIS_KEY_SYS_SETTING}")
    private String REDIS_KEY_SYS_SETTING;

    /**
     * 获取用户心跳（确定客户端是否保持连接）
     * @param userId    用户id
     * @return          时间戳
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisUtils.get(REDIS_KEY_USER_HEARTBEAT + userId);
    }

    /**
     * 保存TokenUserInfoDto
     * @param tokenUserInfoDto  TokenUserInfoDto
     */
    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto) {
        // token: tokenUserInfoDto
        redisUtils.set(REDIS_KEY_USER_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, REDIS_TIME_1Day*3);
        // userid: token
        redisUtils.set(REDIS_KEY_USER_TOKEN_USERID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), REDIS_TIME_1Day*3);
    }

    public SysSettingDto getSysSettingDto() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(REDIS_KEY_SYS_SETTING);
        sysSettingDto = sysSettingDto == null ? new SysSettingDto() : sysSettingDto;
        return sysSettingDto;
    }

    public void saveSysSetting(SysSettingDto sysSettingDto){
        redisUtils.set(REDIS_KEY_SYS_SETTING, sysSettingDto);
    }
}
