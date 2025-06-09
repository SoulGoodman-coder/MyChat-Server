package com.mychat.exception;

import com.mychat.utils.enums.ResultCodeEnum;
import lombok.Getter;

/**
 * projectName: com.mychat.exception
 * author:  SoulGoodman-coder
 * description: 业务错误
 */

@Getter
public class BusinessException extends RuntimeException {
    private ResultCodeEnum resultCodeEnum;
    private Integer code = 600;
    private String msg;

    public BusinessException(String msg, Throwable e){
        super(msg, e);
        this.msg = msg;
    }

    public BusinessException(String msg){
        super(msg);
        this.msg = msg;
    }

    public BusinessException(Throwable e){
        super(e);
    }

    public BusinessException(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMsg());
        this.resultCodeEnum = resultCodeEnum;
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMsg();
    }

    public BusinessException(Integer code, String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    /**
     * 重写fillInStackTrace 业务异常不需要堆栈信息，提高效率
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
