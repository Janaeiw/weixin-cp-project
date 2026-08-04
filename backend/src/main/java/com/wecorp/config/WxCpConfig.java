package com.wecorp.config;

import com.wecorp.handler.ExternalContactEventHandler;
import lombok.Data;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import me.chanjar.weixin.cp.constant.WxCpConsts;
import me.chanjar.weixin.cp.message.WxCpMessageRouter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wx.cp")
public class WxCpConfig {

    private String corpId;
    private String corpSecret;
    private String token;
    private String aesKey;
    private Integer agentId;

    @Bean
    public WxCpService wxCpService() {
        WxCpDefaultConfigImpl config = new WxCpDefaultConfigImpl();
        config.setCorpId(corpId);
        config.setCorpSecret(corpSecret);
        config.setToken(token);
        config.setAesKey(aesKey);
        config.setAgentId(agentId);

        WxCpService service = new WxCpServiceImpl();
        service.setWxCpConfigStorage(config);
        return service;
    }

    @Bean
    public WxCpMessageRouter wxCpMessageRouter(WxCpService wxCpService,
                                                 ExternalContactEventHandler externalContactEventHandler) {
        WxCpMessageRouter router = new WxCpMessageRouter(wxCpService);

        // 外部联系人变更事件（客户添加/删除/编辑）
        router.rule().event(WxCpConsts.EventType.CHANGE_EXTERNAL_CONTACT)
                .handler(externalContactEventHandler).end();

        // 客户群变更事件（群创建/更新/解散）
        router.rule().event(WxCpConsts.EventType.CHANGE_EXTERNAL_CHAT)
                .handler(externalContactEventHandler).end();

        return router;
    }
}
