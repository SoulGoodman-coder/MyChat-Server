package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.config.AppConfig;
import com.mychat.entity.dto.SysSettingDto;
import com.mychat.entity.po.GroupInfo;
import com.mychat.entity.po.UserContact;
import com.mychat.exception.BusinessException;
import com.mychat.mapper.UserContactMapper;
import com.mychat.redis.RedisComponent;
import com.mychat.service.GroupInfoService;
import com.mychat.mapper.GroupInfoMapper;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.ResultCodeEnum;
import com.mychat.utils.enums.UserContactStatusEnum;
import com.mychat.utils.enums.UserContactTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
* @author Administrator
* @description 针对表【group_info(群组表)】的数据库操作Service实现
* @createDate 2025-04-19 23:21:32
*/
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements GroupInfoService{

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @Value("${contants.FILE_FOLDER_FILE}")
    private String FILE_FOLDER_FILE;

    @Value("${contants.FILE_FOLDER_AVATAR_NAME}")
    private String FILE_FOLDER_AVATAR_NAME;

    @Value("${contants.PNG_SUFFIX}")
    private String PNG_SUFFIX;

    @Value("${contants.COVER_PNG_SUFFIX}")
    private String COVER_PNG_SUFFIX;

    /**
     * 创建或修改群聊
     *
     * @param groupInfo     群组信息
     * @param avatarFile 原群头像
     * @param avatarCover   群头像缩略图
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        // 当前时间
        Date curDate = new Date();

        //判断创建群聊 还是修改群聊
        if (StringUtils.isEmpty(groupInfo.getGroupId())){   // 创建群聊
            // 查询已创建群聊数
            LambdaQueryWrapper<GroupInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupInfo::getGroupOwnerId, groupInfo.getGroupOwnerId());
            Long count = groupInfoMapper.selectCount(queryWrapper);

            // 判断群聊数是否超出
            SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
            if (count >= sysSettingDto.getMaxGroupCount()){
                throw new BusinessException("最多只能创建" + sysSettingDto.getMaxGroupCount() + "个群聊");
            }

            // 判断群头像参数是否为空
            if (null == avatarCover){
                throw new BusinessException(ResultCodeEnum.CODE_600);
            }

            // 补全参数
            groupInfo.setCreateTime(curDate);
            groupInfo.setGroupId(StringUtils.getGroupId());

            // 写入数据库
            groupInfoMapper.insert(groupInfo);

            // 将群组添加为联系人
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);

            // 写入数据库
            userContactMapper.insert(userContact);

            // TODO 创建会话 发送消息


        }else {                                             // 修改群聊
            // 根据groupId查询GroupInfo
            LambdaQueryWrapper<GroupInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupInfo::getGroupId, groupInfo.getGroupId());
            GroupInfo dbGroupInfo = groupInfoMapper.selectOne(queryWrapper);

            // 判断当前用户是否是群主（不是群主，不允许修改操作）
            if (! dbGroupInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())){
                throw new BusinessException(ResultCodeEnum.CODE_600);
            }

            // 将修改数据写入数据库
            groupInfoMapper.updateById(groupInfo);

            // TODO 更新相关表冗余信息

            // TODO 修改群昵称时，发送ws消息
        }

        /*
         * 上传群头像
         */
        if (null == avatarCover){
            return;
        }
        // 构建存储路径
        String baseFolder = appConfig.getProjectFolder() + FILE_FOLDER_FILE;
        File targetFileFolder = new File(baseFolder + FILE_FOLDER_AVATAR_NAME);
        if (!targetFileFolder.exists()){
            targetFileFolder.mkdirs();
        }
        String filePath = targetFileFolder.getPath() + "/" + groupInfo.getGroupId() + PNG_SUFFIX;

        avatarFile.transferTo(new File(filePath));
        avatarCover.transferTo(new File(filePath + COVER_PNG_SUFFIX));
    }
}




