package com.wecorp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属模块 */
    private String module;

    /** 操作概要 */
    private String operation;

    /** 请求方法（类名.方法名） */
    private String method;

    /** 请求URL */
    private String requestUrl;

    /** 请求方式（GET/POST/PUT/DELETE） */
    private String requestMethod;

    /** 请求头 */
    private String requestHeaders;

    /** 请求体 */
    private String requestBody;

    /** 响应头 */
    private String responseHeaders;

    /** 响应体 */
    private String responseBody;

    /** HTTP状态码 */
    private Integer statusCode;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** IP地址 */
    private String ip;

    /** 操作系统 */
    private String os;

    /** 浏览器类型 */
    private String browser;

    /** TraceId */
    private String traceId;

    /** 异常信息 */
    private String exceptionMsg;

    /** 耗时（毫秒） */
    private Long costTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
