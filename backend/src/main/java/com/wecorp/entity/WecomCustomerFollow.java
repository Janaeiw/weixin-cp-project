package com.wecorp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_wecom_customer_follow")
public class WecomCustomerFollow {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String externalUserid;

    private String userid;

    private String remark;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followCreateTime;

    private String state;

    private String remarkCompany;

    private String remarkMobiles;

    private String tagIds;

    private String tags;

    private String remarkCorpName;

    private String addWay;

    private String operatorUserid;

    private String wechatChannelsNickname;

    private Integer wechatChannelsSource;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
