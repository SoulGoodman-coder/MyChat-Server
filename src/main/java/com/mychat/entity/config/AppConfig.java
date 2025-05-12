package com.mychat.entity.config;

import com.mychat.utils.StringUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * projectName: com.mychat.entity.config
 * author:  SoulGoodman-coder
 * description: 配置文件属性实体类
 */

@Component("appConfig")
@Getter
public class AppConfig {
    @Value("${ws.port}")
    private Integer wsPort;     // websocket端口

    @Value("${project.folder}")
    private String projectFolder;   // 文件目录

    @Value("#{'${admin.emails}'.split(',')}")
    private List<String> adminEmails;     // 管理员邮箱

    // 密码校验正则（8-18为字母数字组合，数字不能开头）
    public static final String REGEX_PASSWORD = "^(?=[^0-9])(?=.*[A-Za-z])(?=.*\\d).{8,18}$";

    public String getProjectFolder() {
        // 如果不是以 "/" 结尾的，加上 "/"
        if (!StringUtils.isEmpty(projectFolder) && !projectFolder.endsWith("/")) {
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }
}
