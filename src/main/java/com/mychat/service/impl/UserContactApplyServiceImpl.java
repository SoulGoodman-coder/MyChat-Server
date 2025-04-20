package com.mychat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.po.UserContactApply;
import com.mychat.service.UserContactApplyService;
import com.mychat.mapper.UserContactApplyMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【user_contact_apply(好友申请表)】的数据库操作Service实现
* @createDate 2025-04-19 23:22:28
*/
@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply>
    implements UserContactApplyService{

}




