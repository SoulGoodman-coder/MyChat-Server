package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.config.AppConfig;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.UserContactApply;
import com.mychat.entity.vo.UserInfoVo;
import com.mychat.exception.BusinessException;
import com.mychat.mapper.UserInfoBeautyMapper;
import com.mychat.entity.po.UserInfo;
import com.mychat.entity.po.UserInfoBeauty;
import com.mychat.redis.RedisComponent;
import com.mychat.service.UserInfoService;
import com.mychat.mapper.UserInfoMapper;
import com.mychat.utils.CopyUtils;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
* @author Administrator
* @description 针对表【user_info(用户信息表)】的数据库操作Service实现
* @createDate 2025-04-01 22:37:22
*/
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService{

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserInfoBeautyMapper userInfoBeautyMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Value("${contants.LENGTH_20}")
    private Integer LENGTH_20;

    @Value("${contants.FILE_FOLDER_FILE}")
    private String FILE_FOLDER_FILE;

    @Value("${contants.FILE_FOLDER_AVATAR_NAME}")
    private String FILE_FOLDER_AVATAR_NAME;

    @Value("${contants.PNG_SUFFIX}")
    private String PNG_SUFFIX;

    @Value("${contants.COVER_PNG_SUFFIX}")
    private String COVER_PNG_SUFFIX;

    /**
     * 用户注册
     *
     * @param email    邮箱
     * @param nickName 昵称
     * @param password 密码
     */
    @Override
    public void register(String email, String nickName, String password) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getEmail, email);
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);

        if (null != userInfo) {
            throw new BusinessException("邮箱账号已存在");
        }
        String userId = StringUtils.getUserId();

        // 根据邮箱查询是否是靓号
        LambdaQueryWrapper<UserInfoBeauty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfoBeauty::getEmail, email);
        UserInfoBeauty beautyAccount = userInfoBeautyMapper.selectOne(wrapper);

        // 标识靓号是否被未使用
        Boolean isUseBeautyAccount = null != beautyAccount && beautyAccount.getStatus().equals(BeautyAccountStatusEnum.NO_USE.getStatus());

        if (isUseBeautyAccount){
            userId = UserContactTypeEnum.USER.getPrefix() + beautyAccount.getUserId();
        }

        // 当前时间
        Date curDate = new Date();

        // 封装账号信息对象
        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setNickName(nickName);
        userInfo.setPassword(StringUtils.encodeMD5(password));
        userInfo.setEmail(email);
        userInfo.setCreateTime(curDate);
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatue());
        userInfo.setLastOffTime(curDate.getTime());
        userInfo.setJoinType(JoinTypeEnum.JOIN.getType());

        // 将账号信息写入数据库
        userInfoMapper.insert(userInfo);

        // 靓号被使用后要修改靓号状态
        if (isUseBeautyAccount){
            LambdaUpdateWrapper<UserInfoBeauty> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper
                    .eq(UserInfoBeauty::getUserId, userId)
                    .set(UserInfoBeauty::getStatus, BeautyAccountStatusEnum.USED.getStatus());
            userInfoBeautyMapper.update(updateWrapper);
        }

        // TODO 创建机器人好友

    }

    /**
     * 用户登录
     *
     * @param email    邮箱
     * @param password 密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVo login(String email, String password) {
        // 根据邮箱查询用户信息
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getEmail, email);
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);

        if (null == userInfo) {
            throw new BusinessException("账号不存在");
        }
        if (!userInfo.getPassword().equals(StringUtils.encodeMD5(password))){
            throw new BusinessException("密码错误");
        }
        if (UserStatusEnum.DISABLE.getStatue().equals(userInfo.getStatus())){
            throw new BusinessException("账号已禁用");
        }

        // TODO 查询我的群组 查询我的联系人

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(userInfo);

        // 通过心跳判断账号是否已登录
        Long lastHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
        if (null != lastHeartBeat){
            throw new BusinessException("此账号已经在别处登录");
        }

        // 生成token
        String token = StringUtils.encodeMD5(tokenUserInfoDto.getUserId() + StringUtils.getRandomString(LENGTH_20));
        tokenUserInfoDto.setToken(token);

        // 保存登录信息到redis
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        // 封装返回登录信息
        UserInfoVo userInfoVo = CopyUtils.copy(userInfo, UserInfoVo.class);
        userInfoVo.setAdmin(tokenUserInfoDto.getAdmin());
        userInfoVo.setToken(tokenUserInfoDto.getToken());
        return userInfoVo;
    }

    /**
     * 修改用户信息
     *
     * @param userInfo    修改后的用户信息对象
     * @param avatarFile  原用户头像
     * @param avatarCover 用户头像缩略图
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        // 判断是否需要修改头像
        if (null != avatarFile){
            // 构建存储路径
            String baseFolder = appConfig.getProjectFolder() + FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder + FILE_FOLDER_AVATAR_NAME);
            if (!targetFileFolder.exists()){
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + userInfo.getUserId() + PNG_SUFFIX;

            avatarFile.transferTo(new File(filePath));
            avatarCover.transferTo(new File(filePath + COVER_PNG_SUFFIX));
        }

        // 查询数据库中原本的用户信息对象
        UserInfo dbUserInfo = userInfoMapper.selectById(userInfo.getUserId());

        // 更新数据库中用户信息
        userInfoMapper.updateById(userInfo);

        // 判断用户昵称是否更新（更新界面中用户昵称）
        String userNickName = null;
        if (!dbUserInfo.getNickName().equals(userInfo.getNickName())){
            userNickName = userInfo.getNickName();
        }

        // TODO 更新会话信息中的昵称信息 （userNickName）

        // TODO 更新token

    }

    /**
     * 获取用户列表
     *
     * @param userId        用户id
     * @param nickNameFuzzy 用户昵称（支持模糊搜索）
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return List<UserInfo>
     */
    @Override
    public List<UserInfo> loadUser(String userId, String nickNameFuzzy, Integer pageNumber, Integer pageSize) {
        // 判断页码参数是否合法
        if (null == pageNumber || pageNumber <= 0) {
            pageNumber = 1;
        }

        // 判断页码参数是否合法
        if (null == pageSize || pageSize <= 0) {
            pageSize = 15;
        }

        // IPage接口的实现对象Page(当前页码, 页容量)
        Page<UserInfo> page = new Page<>(pageNumber, pageSize);
        userInfoMapper.loadUser(page, userId, nickNameFuzzy);
        // 获取当前页数据
        List<UserInfo> records = page.getRecords();
        return records;
    }

    /**
     * 更新用户状态
     *
     * @param status 新的用户状态 0：禁用  1：启用
     * @param userId 目标用户id
     */
    @Override
    public void updateUserStatus(Integer status, String userId) {
        UserStatusEnum statusEnum = UserStatusEnum.getByStatue(status);
        if (null == statusEnum){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setStatus(status);
        userInfoMapper.updateById(userInfo);
    }

    /**
     * 强制下线
     *
     * @param userId 被强制下线的用户id
     */
    @Override
    public void forceOffLine(String userId) {
        // TODO 强制下线
    }

    /**
     * 封装返回给前端的用户信息
     * @param userInfo  UserInfo
     * @return          TokenUserInfoDto
     */
    public TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo) {
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setNickName(userInfo.getNickName());
        tokenUserInfoDto.setUserId(userInfo.getUserId());

        // 判断是否是管理员
        List<String> adminEmails = appConfig.getAdminEmails();
        if ( !StringUtils.isEmpty(adminEmails) && adminEmails.contains(userInfo.getEmail()) ){
            tokenUserInfoDto.setAdmin(true);
        }else {
            tokenUserInfoDto.setAdmin(false);
        }
        return tokenUserInfoDto;
    }
}




