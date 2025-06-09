package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.po.UserInfo;
import com.mychat.entity.po.UserInfoBeauty;
import com.mychat.exception.BusinessException;
import com.mychat.mapper.UserInfoMapper;
import com.mychat.service.UserInfoBeautyService;
import com.mychat.mapper.UserInfoBeautyMapper;
import com.mychat.utils.PageUtils;
import com.mychat.utils.enums.BeautyAccountStatusEnum;
import com.mychat.utils.enums.ResultCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【user_info_beauty(靓号表)】的数据库操作Service实现
* @createDate 2025-04-01 22:37:43
*/
@Service
public class UserInfoBeautyServiceImpl extends ServiceImpl<UserInfoBeautyMapper, UserInfoBeauty>
    implements UserInfoBeautyService{

    @Resource
    private UserInfoBeautyMapper userInfoBeautyMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 保存靓号（新增、修改）
     * userInfoBeauty对象中，有id是修改，无id是新增
     * @param userInfoBeauty 靓号信息对象
     */
    @Override
    public void saveBeautAccount(UserInfoBeauty userInfoBeauty) {
        // 不能修改已使用的靓号
        if (null != userInfoBeauty.getId()){
            // 根据userId从数据库查询靓号信息
            UserInfoBeauty dbUserInfoBeauty = userInfoBeautyMapper.selectById(userInfoBeauty.getId());

            // 判断靓号是否已被使用
            if (BeautyAccountStatusEnum.USED.getStatus().equals(dbUserInfoBeauty.getStatus())){
                throw new BusinessException(ResultCodeEnum.CODE_600);
            }
        }

        // 根据邮箱查询数据库中靓号信息
        LambdaQueryWrapper<UserInfoBeauty> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfoBeauty::getEmail, userInfoBeauty.getEmail());
        UserInfoBeauty dbUserInfoBeauty = userInfoBeautyMapper.selectOne(queryWrapper);

        // 新增靓号时，检查邮箱是否被占用
        if (null == userInfoBeauty.getId() && null != dbUserInfoBeauty){
            throw new BusinessException("靓号邮箱已存在");
        }

        // 修改靓号时，检查邮箱是否被占用
        if (null != userInfoBeauty.getId() && null != dbUserInfoBeauty && !userInfoBeauty.getId().equals(dbUserInfoBeauty.getId())){
            throw new BusinessException("靓号邮箱已存在");
        }

        // userId查询数据库中靓号信息
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfoBeauty::getUserId, userInfoBeauty.getUserId());
        dbUserInfoBeauty = userInfoBeautyMapper.selectOne(queryWrapper);

        // 新增靓号时，检查靓号是否被占用
        if (null == userInfoBeauty.getId() && null != dbUserInfoBeauty){
            throw new BusinessException("靓号已存在");
        }

        // 修改靓号时，检查靓号是否被占用
        if (null != userInfoBeauty.getId() && null != dbUserInfoBeauty && !userInfoBeauty.getId().equals(dbUserInfoBeauty.getId())){
            throw new BusinessException("靓号已存在");
        }

        // 判断靓号的账号、邮箱是否已被注册
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getEmail, userInfoBeauty.getEmail());
        UserInfo userInfo = userInfoMapper.selectOne(wrapper);
        if (null != userInfo){
            throw new BusinessException("靓号邮箱已被注册");
        }
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserId, userInfoBeauty.getUserId());
        userInfo = userInfoMapper.selectOne(wrapper);
        if (null != userInfo){
            throw new BusinessException("靓号已被注册");
        }

        // 插入或修改靓号
        if (null != userInfoBeauty.getId()){
            userInfoBeautyMapper.updateById(userInfoBeauty);
        }else {
            userInfoBeauty.setStatus(BeautyAccountStatusEnum.NO_USE.getStatus());
            userInfoBeautyMapper.insert(userInfoBeauty);
        }
    }

    /**
     * 获取靓号列表
     *
     * @param userIdFuzzy   靓号（支持模糊搜索）
     * @param emailFuzzy    邮箱（支持模糊搜索）
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return List<UserInfoBeauty>
     */
    @Override
    public Map<String, Object> loadBeautyAccountList(String userIdFuzzy, String emailFuzzy, Integer pageNumber, Integer pageSize) {
        // 判断页码参数是否合法
        if (null == pageNumber || pageNumber <= 0) {
            pageNumber = 1;
        }

        // 判断页容量参数是否合法
        if (null == pageSize || pageSize <= 0) {
            pageSize = 15;
        }

        // IPage接口的实现对象Page(当前页码, 页容量)
        Page<UserInfoBeauty> page = new Page<>(pageNumber, pageSize);
        userInfoBeautyMapper.loadBeautyAccountList(page, userIdFuzzy, emailFuzzy);

        // 获取当前页数据
        // List<UserInfoBeauty> records = page.getRecords();
        // 封装分页数据
        Map<String, Object> pageResultData = PageUtils.getPageResultData(page);
        return pageResultData;
    }
}




