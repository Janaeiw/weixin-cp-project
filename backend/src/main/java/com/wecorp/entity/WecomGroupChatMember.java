package com.wecorp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_wecom_group_chat_member")
public class WecomGroupChatMember {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String chatId;

    private String userId;

    private Integer memberType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    private Integer joinScene;

    private String groupNickname;

    private String name;

    /** 邀请者userid */
    private String invitor;

    /** 微信开放平台unionid */
    private String unionId;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
