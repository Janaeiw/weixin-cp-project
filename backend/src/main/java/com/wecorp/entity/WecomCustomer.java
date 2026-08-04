package com.wecorp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_wecom_customer")
public class WecomCustomer {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String externalUserid;

    private String name;

    private String nickname;

    private String avatar;

    private Integer gender;

    private Integer type;

    private String corpName;

    private String corpFullName;

    private String position;

    private String unionId;

    private String externalProfile;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
