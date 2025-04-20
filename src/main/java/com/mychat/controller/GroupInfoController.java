package com.mychat.controller;

import com.mychat.service.GroupInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 联系人controller
 */

@RestController("groupInfoController")
@RequestMapping("groupInfo")
public class GroupInfoController {

    @Resource
    private GroupInfoService groupInfoService;

}
