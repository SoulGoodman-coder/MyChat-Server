package com.mychat.controller;

import com.mychat.entity.config.AppConfig;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description:
 */

@RestController
@RequestMapping
public class TestController {

    @Resource
    private AppConfig appConfig;

    @GetMapping("test")
    public List<String> test() {
        List<String> adminEmails = appConfig.getAdminEmails();
        adminEmails.forEach(System.out::println);
        return adminEmails;
    }
}
