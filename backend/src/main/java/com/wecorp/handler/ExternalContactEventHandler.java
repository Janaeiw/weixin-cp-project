package com.wecorp.handler;

import com.wecorp.service.WecomCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.constant.WxCpConsts;
import me.chanjar.weixin.cp.message.WxCpMessageHandler;
import me.chanjar.weixin.common.session.WxSessionManager;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 企微外部联系人回调事件处理器
 *
 * 处理事件：
 * - change_external_contact: 客户添加/删除/编辑
 * - change_external_chat: 客户群创建/更新/解散
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalContactEventHandler implements WxCpMessageHandler {

    private final WecomCustomerService wecomCustomerService;

    @Override
    public WxCpXmlOutMessage handle(WxCpXmlMessage wxMessage, Map<String, Object> context,
                                    WxCpService wxCpService, WxSessionManager sessionManager) throws WxErrorException {
        String event = wxMessage.getEvent();
        String changeType = wxMessage.getChangeType();

        log.info("收到企微回调事件: event={}, changeType={}, userId={}, externalUserId={}, chatId={}",
                event, changeType, wxMessage.getUserId(), wxMessage.getExternalUserId(), wxMessage.getChatId());

        try {
            if (WxCpConsts.EventType.CHANGE_EXTERNAL_CONTACT.equals(event)) {
                handleExternalContactChange(changeType, wxMessage);
            } else if (WxCpConsts.EventType.CHANGE_EXTERNAL_CHAT.equals(event)) {
                handleExternalChatChange(changeType, wxMessage);
            } else {
                log.warn("未处理的外部联系人事件: event={}, changeType={}", event, changeType);
            }
        } catch (Exception e) {
            log.error("处理回调事件异常: event={}, changeType={}", event, changeType, e);
        }

        // 回调事件不需要回复消息
        return null;
    }

    /**
     * 处理外部联系人变更事件
     */
    private void handleExternalContactChange(String changeType, WxCpXmlMessage wxMessage) {
        String externalUserId = wxMessage.getExternalUserId();
        String userId = wxMessage.getUserId();

        switch (changeType) {
            case WxCpConsts.ExternalContactChangeType.ADD_EXTERNAL_CONTACT:
            case WxCpConsts.ExternalContactChangeType.EDIT_EXTERNAL_CONTACT:
                // 客户添加或编辑 -> 同步客户详情
                log.info("处理客户添加/编辑事件: changeType={}, externalUserId={}, userId={}",
                        changeType, externalUserId, userId);
                wecomCustomerService.syncSingleCustomer(externalUserId, userId);
                break;

            case WxCpConsts.ExternalContactChangeType.DEL_EXTERNAL_CONTACT:
            case WxCpConsts.ExternalContactChangeType.DEL_FOLLOW_USER:
                // 客户删除跟进人关系
                log.info("处理客户删除事件: changeType={}, externalUserId={}, userId={}",
                        changeType, externalUserId, userId);
                wecomCustomerService.deleteCustomerFollow(externalUserId, userId);
                break;

            case WxCpConsts.ExternalContactChangeType.ADD_HALF_EXTERNAL_CONTACT:
                // 半添加状态（待确认），暂不处理
                log.info("客户半添加事件（待确认），暂不处理: externalUserId={}, userId={}",
                        externalUserId, userId);
                break;

            case WxCpConsts.ExternalContactChangeType.TRANSFER_FAIL:
                // 转接失败，记录日志
                log.warn("客户转接失败: externalUserId={}, userId={}, failReason={}",
                        externalUserId, userId, wxMessage.getFailReason());
                break;

            default:
                log.warn("未处理的外部联系人变更类型: changeType={}", changeType);
        }
    }

    /**
     * 处理客户群变更事件
     */
    private void handleExternalChatChange(String changeType, WxCpXmlMessage wxMessage) {
        String chatId = wxMessage.getChatId();

        switch (changeType) {
            case WxCpConsts.ExternalChatChangeType.CREATE:
            case WxCpConsts.ExternalChatChangeType.UPDATE:
                // 群创建或更新 -> 同步群详情
                log.info("处理客群创建/更新事件: changeType={}, chatId={}", changeType, chatId);
                wecomCustomerService.syncSingleGroupChat(chatId);
                break;

            case WxCpConsts.ExternalChatChangeType.DISMISS:
                // 群解散 -> 标记状态
                log.info("处理客群解散事件: chatId={}", chatId);
                wecomCustomerService.dismissGroupChat(chatId);
                break;

            default:
                log.warn("未处理的客户群变更类型: changeType={}", changeType);
        }
    }
}
