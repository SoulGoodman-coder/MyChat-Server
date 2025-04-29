package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.dto.UserContactSearchDto;
import com.mychat.entity.po.UserContactApply;
import com.mychat.service.UserContactApplyService;
import com.mychat.service.UserContactService;
import com.mychat.service.UserInfoService;
import com.mychat.utils.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 联系人相关controller
 */

@RestController
@RequestMapping("contact")
public class UserContactController extends BaseController{
    @Resource
    private UserContactService userContactService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserContactApplyService userContactApplyService;

    /**
     * 搜索好友、群组
     * @param request       request
     * @param contactId     联系人id
     * @return  Result
     */
    @PostMapping("search")
    @GlobalInterceptor
    public Result search(HttpServletRequest request, @NotBlank String contactId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        UserContactSearchDto userContactSearchDto = userContactService.searchContact(tokenUserInfoDto.getUserId(), contactId);

        return Result.ok(userContactSearchDto);
    }

    /**
     * 好友申请或入群申请
     * @param request       request
     * @param contactId     联系人id
     * @param applyInfo     申请信息
     * @return Result(Integer joinType)
     */
    @PostMapping("applyAdd")
    @GlobalInterceptor
    public Result applyAdd(HttpServletRequest request, @NotBlank String contactId, String applyInfo){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        Integer joinType = userContactService.applyAdd(tokenUserInfoDto, contactId, applyInfo);

        return Result.ok(joinType);
    }

    /**
     * 获取好友申请列表
     * @param request       request
     * @param pageNumber    页码
     * @return Result
     */
    @PostMapping("loadApply")
    @GlobalInterceptor
    public Result loadApply(HttpServletRequest request, Integer pageNumber){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        List<UserContactApply> userContactApplyList = userContactApplyService.loadApply(tokenUserInfoDto.getUserId(), pageNumber);

        return Result.ok(userContactApplyList);
    }

    /**
     * 处理加群申请或好友申请
     * @param request       request
     * @param applyId       申请的id
     * @param status        申请结果：0:待处理、1:同意、2:拒绝、3:拉黑
     * @return Result
     */
    @PostMapping("dealWithApply")
    @GlobalInterceptor
    public Result dealWithApply(HttpServletRequest request, @NotBlank String applyId, @NotBlank Integer status){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        userContactApplyService.dealWithApply(tokenUserInfoDto.getUserId(), applyId, status);

        return Result.ok(null);
    }


}
