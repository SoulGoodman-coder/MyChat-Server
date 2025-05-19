package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.dto.UserContactSearchDto;
import com.mychat.entity.po.UserContact;
import com.mychat.entity.po.UserContactApply;
import com.mychat.entity.vo.UserInfoVo;
import com.mychat.exception.BusinessException;
import com.mychat.service.UserContactApplyService;
import com.mychat.service.UserContactService;
import com.mychat.service.UserInfoService;
import com.mychat.utils.Result;
import com.mychat.utils.enums.ResultCodeEnum;
import com.mychat.utils.enums.UserContactStatusEnum;
import com.mychat.utils.enums.UserContactTypeEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
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
@Validated         // 参数校验
public class UserContactController extends BaseController{
    @Resource
    private UserContactService userContactService;

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

        Integer joinType = userContactApplyService.applyAdd(tokenUserInfoDto, contactId, applyInfo);

        return Result.ok(joinType);
    }

    /**
     * 获取好友申请列表
     * @param request       request
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return Result
     */
    @PostMapping("loadApply")
    @GlobalInterceptor
    public Result loadApply(HttpServletRequest request, Integer pageNumber, Integer pageSize){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        List<UserContactApply> userContactApplyList = userContactApplyService.loadApply(tokenUserInfoDto.getUserId(), pageNumber, pageSize);

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
    public Result dealWithApply(HttpServletRequest request, @NotBlank String applyId, @NotNull Integer status){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        userContactApplyService.dealWithApply(tokenUserInfoDto.getUserId(), applyId, status);

        return Result.ok(null);
    }

    /**
     * 获取联系人列表
     * @param request       request
     * @param contactType   联系人类型:user/group（忽略大小写）
     * @return Result
     */
    @PostMapping("loadContact")
    @GlobalInterceptor
    public Result loadContact(HttpServletRequest request, @NotBlank String contactType){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByName(contactType);

        if (null == contactTypeEnum){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        List<UserContact> userContactList = userContactService.loadContact(tokenUserInfoDto.getUserId(), contactTypeEnum);

        return Result.ok(userContactList);
    }


    /**
     * 获取联系人详情（可查询非好友）
     * @param request       request
     * @param contactId     要查询的联系人id
     * @return Result
     */
    @PostMapping("getContactInfo")
    @GlobalInterceptor
    public Result getContactInfo(HttpServletRequest request, @NotBlank String contactId){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        UserInfoVo userInfoVo = userContactService.getContactInfo(tokenUserInfoDto.getUserId(), contactId);

        return Result.ok(userInfoVo);
    }

    /**
     * 获取联系人详情（仅可查询好友）
     * @param request       request
     * @param contactId     要查询的联系人id
     * @return Result
     */
    @PostMapping("getContactUserInfo")
    @GlobalInterceptor
    public Result getContactUserInfo(HttpServletRequest request, @NotBlank String contactId){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        UserInfoVo userInfoVo = userContactService.getContactUserInfo(tokenUserInfoDto.getUserId(), contactId);

        return Result.ok(userInfoVo);
    }

    /**
     * 删除联系人
     * @param request       request
     * @param contactId     要删除的联系人id
     * @return Result
     */
    @PostMapping("delContact")
    @GlobalInterceptor
    public Result delContact(HttpServletRequest request, @NotBlank String contactId){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        userContactService.removeUserContact(tokenUserInfoDto.getUserId(), contactId, UserContactStatusEnum.DEL);

        return Result.ok(null);
    }

    /**
     * 拉黑联系人
     * @param request       request
     * @param contactId     要拉黑的联系人id
     * @return Result
     */
    @PostMapping("addContact2BlackList")
    @GlobalInterceptor
    public Result addContact2BlackList(HttpServletRequest request, @NotBlank String contactId){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        userContactService.removeUserContact(tokenUserInfoDto.getUserId(), contactId, UserContactStatusEnum.BLACKLIST);

        return Result.ok(null);
    }
}

