package com.mychat.controller;

import com.mychat.exception.BusinessException;
import com.mychat.utils.Result;
import com.mychat.utils.enums.ResultCodeEnum;
import com.mychat.utils.enums.ResultStatusEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 全局异常处理
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    Object handleException(Exception e, HttpServletRequest request) {
        log.error("请求错误，请求地址 {},错误信息：", request.getRequestURI(), e);
        Result result = new Result();
        if (e instanceof NoHandlerFoundException){
            // 404
            result = Result.build(ResultCodeEnum.CODE_404, null);
            result.setStatus(ResultStatusEnum.ERROR);
        } else if (e instanceof BusinessException businessException) {
            // 业务处理
            result.setCode(businessException.getCode());
            result.setMsg(businessException.getMsg());
            result.setStatus(ResultStatusEnum.ERROR);
        } else if (e instanceof BindException) {
            // 请求参数错误
            result = Result.build(ResultCodeEnum.CODE_600, null);
            result.setStatus(ResultStatusEnum.ERROR);
        } else if (e instanceof DuplicateKeyException) {
            // 主键冲突
            result = Result.build(ResultCodeEnum.CODE_601, null);
            result.setStatus(ResultStatusEnum.ERROR);
        }else {
            // 服务端返回错误
            result = Result.build(ResultCodeEnum.CODE_500, null);
            result.setStatus(ResultStatusEnum.ERROR);
        }

        return result;
    }
}
