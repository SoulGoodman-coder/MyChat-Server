package com.mychat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.po.UserContact;
import com.mychat.service.UserContactService;
import com.mychat.mapper.UserContactMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【user_contact(联系人表)】的数据库操作Service实现
* @createDate 2025-04-19 23:22:20
*/
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact>
    implements UserContactService{

}




