package com.mychat.utils;

import com.mychat.utils.enums.ResultCodeEnum;
import com.mychat.utils.enums.ResultStatusEnum;
import lombok.*;

/**
 * projectName: com.mychat.utils
 * author:  SoulGoodman-coder
 * description: 返回结果类
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    // 返回状态 success/error
    @Setter(AccessLevel.NONE)   // 不生成该属性的setter方法，手动实现
    private String status = "success";
    // 返回状态码
    private Integer code;
    //返回消息
    private String msg;
    //返回数据
    private T data;

    private static <T> Result<T> build(T data){
        Result<T> result = new Result<>();
        result.setData(data);
        return result;
    }

    /*
    * 封装返回对象
    * */
    public static <T> Result<T> ok(T data){
        return build(ResultCodeEnum.SUCCESS, data);
    }

    public static <T> Result<T> build(ResultCodeEnum resultCodeEnum, T data){
        Result<T> result = build(data);
        result.setCode(resultCodeEnum.getCode());
        result.setMsg(resultCodeEnum.getMsg());
        return result;
    }

    public void setStatus(ResultStatusEnum statusEnum) {
        this.status = statusEnum.getStatus();
    }
}
