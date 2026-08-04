package com.wecorp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_wecom_group_chat")
public class WecomGroupChat {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String chatId;

    private String name;

    private String owner;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeField;

    private String notice;

    private Integer memberCount;

    /** 跟进人状态: 0-跟进人正常 1-跟进人离职 2-离职继承中 3-离职继承完成 */
    private Integer status;

    /** 群管理员userid列表(JSON数组) */
    private String adminList;

    /** 群成员版本号 */
    private String memberVersion;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
