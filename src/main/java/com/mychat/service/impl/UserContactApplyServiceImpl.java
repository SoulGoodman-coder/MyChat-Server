package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.po.UserContact;
import com.mychat.entity.po.UserContactApply;
import com.mychat.exception.BusinessException;
import com.mychat.service.UserContactApplyService;
import com.mychat.mapper.UserContactApplyMapper;
import com.mychat.service.UserContactService;
import com.mychat.utils.enums.ResultCodeEnum;
import com.mychat.utils.enums.UserContactApplyStatusEnum;
import com.mychat.utils.enums.UserContactStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【user_contact_apply(好友申请表)】的数据库操作Service实现
* @createDate 2025-04-19 23:22:28
*/
@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply>
    implements UserContactApplyService{

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Resource
    private UserContactService userContactService;

    /**
     * 获取好友申请列表
     *
     * @param receiveUserId 接收人id（即当前用户id）
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return List<UserContactApply>
     */
    @Override
    public List<UserContactApply> loadApply(String receiveUserId, Integer pageNumber, Integer pageSize) {
        // 判断页码参数是否合法
        if (null == pageNumber || pageNumber <= 0) {
            pageNumber = 1;
        }

        // 判断页容量参数是否合法
        if (null == pageSize || pageSize <= 0) {
            pageSize = 5;
        }

        // IPage接口的实现对象Page(当前页码, 页容量)
        Page<UserContactApply> page = new Page<>(pageNumber, pageSize);
        userContactApplyMapper.loadApply(page, receiveUserId);
        // 获取当前页数据
        List<UserContactApply> records = page.getRecords();
        return records;
    }

    /**
     * 处理加群申请或好友申请
     *
     * @param userId  当前用户id
     * @param applyId 申请的id
     * @param status  申请结果：0:待处理、1:同意、2:拒绝、3:拉黑
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealWithApply(String userId, String applyId, Integer status) {
        // 判断申请状态是否合法
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if (null == statusEnum|| UserContactApplyStatusEnum.INIT == statusEnum) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 查询id查询申请详情
        UserContactApply userContactApply = userContactApplyMapper.selectById(applyId);
        // 判断申请的接收者是否是当前用户
        if (null == userContactApply || !userId.equals(userContactApply.getReceiveUserId())) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 封装更新后的数据对象
        UserContactApply updateInfo = new UserContactApply();
        updateInfo.setStatus(status);
        updateInfo.setLastApplyTime(System.currentTimeMillis());

        // 更新数据库user_contact_apply表申请状态status字段，校验
        LambdaUpdateWrapper<UserContactApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserContactApply::getReceiveUserId, userId)
                     .eq(UserContactApply::getContactId, userContactApply.getContactId())
                     .eq(UserContactApply::getStatus, UserContactApplyStatusEnum.INIT.getStatus());
        int row = userContactApplyMapper.update(updateInfo, updateWrapper);
        if (row != 1){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 同意申请
        if (UserContactApplyStatusEnum.PASS == statusEnum){
            // 添加联系人
            userContactService.addContact(userContactApply.getApplyUserId(), userContactApply.getReceiveUserId(), userContactApply.getContactId(), userContactApply.getContactType(), userContactApply.getApplyInfo());

            return;
        }

        // 拒接申请：只需要更新状态，不需要其他操作

        // 拉黑申请
        if (UserContactApplyStatusEnum.BLICKLIST == statusEnum){
            Date curDate = new Date();
            // 构建user_contact表数据对象
            UserContact userContact = new UserContact();
            userContact.setUserId(userContactApply.getApplyUserId());   // 发出申请的人id
            userContact.setContactId(userContactApply.getContactId());  // 被申请的用户|群组id
            userContact.setContactType(userContactApply.getContactType());
            userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_BEFORE.getStatus());
            // userContact.setLastUpdateTime(curDate);

            // 判断user_contact表中是否已经存在该条好友记录。存在则更新，不存在则插入
            // LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
            // queryWrapper.eq(UserContact::getUserId, userContact.getUserId())
            //             .eq(UserContact::getContactId, userContact.getContactId());
            // UserContact selectedUserContact = userContactMapper.selectOne(queryWrapper);
            //
            // if (null == selectedUserContact) {
            //     userContact.setCreateTime(curDate);
            //     userContactMapper.insert(userContact);
            // }else {
            //     LambdaUpdateWrapper<UserContact> wrapper = new LambdaUpdateWrapper<>();
            //     wrapper.eq(UserContact::getUserId, userContactApply.getApplyUserId())
            //            .eq(UserContact::getContactId, userContactApply.getContactId());
            //     userContactMapper.update(userContact, wrapper);
            // }

            userContactService.insertOrUpdateContact(userContact);
            return;
        }

    }


}




