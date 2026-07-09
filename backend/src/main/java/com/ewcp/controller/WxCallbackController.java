package com.ewcp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.config.WxCpConfigStorage;
import me.chanjar.weixin.cp.message.WxCpMessageRouter;
import me.chanjar.weixin.cp.util.crypto.WxCpCryptUtil;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 企业微信回调接口
 * 用于配置"接收消息"服务器 URL 验证与消息接收
 *
 * 企微后台配置路径: 应用管理 -> 自建应用 -> 接收消息 -> 设置API接收
 * 回调 URL 格式: https://test.wizone.work/api/wx/callback
 */
@Slf4j
@RestController
@RequestMapping("/api/wx/callback")
@RequiredArgsConstructor
public class WxCallbackController {

    private final WxCpService wxCpService;

    /**
     * URL 验证（GET）
     * 企微配置接收消息服务器时发送验证请求，需解密 echostr 并原样返回
     */
    @GetMapping
    public void verify(@RequestParam("msg_signature") String msgSignature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("echostr") String echostr,
                       HttpServletResponse response) throws IOException {
        log.info("企微回调验证: msgSignature={}, timestamp={}, nonce={}", msgSignature, timestamp, nonce);

        WxCpCryptUtil cryptUtil = new WxCpCryptUtil(wxCpService.getWxCpConfigStorage());
        String decryptedEchoStr = cryptUtil.decrypt(echostr);

        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(decryptedEchoStr);
    }

    /**
     * 接收消息与事件（POST）
     * 企微推送的消息/事件均为此入口
     */
    @PostMapping(produces = "application/xml;charset=UTF-8")
    public void receive(@RequestParam("msg_signature") String msgSignature,
                        @RequestParam("timestamp") String timestamp,
                        @RequestParam("nonce") String nonce,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {
        log.info("收到企微回调: msgSignature={}, timestamp={}, nonce={}", msgSignature, timestamp, nonce);

        WxCpConfigStorage configStorage = wxCpService.getWxCpConfigStorage();

        // 解密并解析消息
        WxCpXmlMessage inMessage = WxCpXmlMessage.fromEncryptedXml(
                request.getInputStream(), configStorage, timestamp, nonce, msgSignature);

        log.info("消息类型: {}, 内容: {}", inMessage.getMsgType(),
                inMessage.getContent() != null ? inMessage.getContent() : "(event)");

        // 路由消息并返回加密响应
        WxCpMessageRouter router = new WxCpMessageRouter(wxCpService);
        WxCpXmlOutMessage outMessage = router.route(inMessage);

        if (outMessage != null) {
            response.setContentType("application/xml;charset=UTF-8");
            response.getWriter().write(outMessage.toEncryptedXml(configStorage));
        }
    }
}
