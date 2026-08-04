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

    private Long joinTime;

    private Integer joinScene;

    private String groupNickname;

    private String name;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
