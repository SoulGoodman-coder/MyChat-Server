package com.mychat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.po.GroupInfo;
import com.mychat.service.GroupInfoService;
import com.mychat.mapper.GroupInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【group_info(群组表)】的数据库操作Service实现
* @createDate 2025-04-19 23:21:32
*/
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo>
    implements GroupInfoService{

}




