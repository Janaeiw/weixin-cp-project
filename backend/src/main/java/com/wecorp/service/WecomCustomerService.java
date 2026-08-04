package com.wecorp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wecorp.entity.WecomCustomer;
import com.wecorp.entity.WecomGroupChat;

public interface WecomCustomerService {

    /**
     * 从企微同步全量客户数据
     */
    void syncCustomers();

    /**
     * 从企微同步全量客群数据
     */
    void syncGroupChats();

    /**
     * 分页查询员工名下的客户列表
     *
     * @param userid   企微员工userid
     * @param keyword  搜索关键词（姓名）
     * @param gender   性别
     * @param type     类型
     * @param corpName 所属企业
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 客户分页列表
     */
    IPage<WecomCustomer> getCustomerList(String userid, String keyword, Integer gender, Integer type, String corpName, Integer pageNum, Integer pageSize);

    /**
     * 获取客户详情（包含跟进人信息）
     *
     * @param externalUserid 外部用户ID
     * @return 客户详情
     */
    WecomCustomer getCustomerDetail(String externalUserid);

    /**
     * 分页查询员工名下的客群列表
     *
     * @param userid   企微员工userid
     * @param keyword  搜索关键词（群名）
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 客群分页列表
     */
    IPage<WecomGroupChat> getGroupChatList(String userid, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 获取客群详情（包含成员列表）
     *
     * @param chatId 群聊ID
     * @return 客群详情
     */
    WecomGroupChat getGroupChatDetail(String chatId);

    /**
     * 增量同步单个客户（回调事件触发）
     *
     * @param externalUserid 外部联系人ID
     * @param userid         企业成员userid
     */
    void syncSingleCustomer(String externalUserid, String userid);

    /**
     * 增量同步单个客群（回调事件触发）
     *
     * @param chatId 群聊ID
     */
    void syncSingleGroupChat(String chatId);

    /**
     * 删除客户跟进人关系（回调事件触发）
     *
     * @param externalUserid 外部联系人ID
     * @param userid         企业成员userid
     */
    void deleteCustomerFollow(String externalUserid, String userid);

    /**
     * 标记客群为已解散（回调事件触发）
     *
     * @param chatId 群聊ID
     */
    void dismissGroupChat(String chatId);
}
