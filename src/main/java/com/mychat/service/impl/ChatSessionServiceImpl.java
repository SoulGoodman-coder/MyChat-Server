package com.mychat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.po.ChatSession;
import com.mychat.service.ChatSessionService;
import com.mychat.mapper.ChatSessionMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【chat_session(会话信息表)】的数据库操作Service实现
* @createDate 2025-05-14 00:00:01
*/
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
    implements ChatSessionService{

}




