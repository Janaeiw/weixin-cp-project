package com.wecorp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wecorp.entity.WecomCustomer;
import com.wecorp.entity.WecomCustomerFollow;
import com.wecorp.entity.WecomGroupChat;
import com.wecorp.entity.WecomGroupChatMember;
import com.wecorp.mapper.WecomCustomerFollowMapper;
import com.wecorp.mapper.WecomCustomerMapper;
import com.wecorp.mapper.WecomGroupChatMapper;
import com.wecorp.mapper.WecomGroupChatMemberMapper;
import com.wecorp.service.WecomCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.WxCpDepart;
import me.chanjar.weixin.cp.bean.WxCpUser;
import me.chanjar.weixin.cp.bean.external.contact.ExternalContact;
import me.chanjar.weixin.cp.bean.external.contact.FollowedUser;
import me.chanjar.weixin.cp.bean.external.contact.WxCpExternalContactInfo;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalGroupChatInfo;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalGroupChatList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WecomCustomerServiceImpl implements WecomCustomerService {

    private final WxCpService wxCpService;
    private final WecomCustomerMapper customerMapper;
    private final WecomCustomerFollowMapper customerFollowMapper;
    private final WecomGroupChatMapper groupChatMapper;
    private final WecomGroupChatMemberMapper groupChatMemberMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCustomers() {
        log.info("========== 开始同步企微客户数据 ==========");
        try {
            // 1. 获取所有部门
            List<WxCpDepart> allDepts = wxCpService.getDepartmentService().list(1L);
            if (allDepts == null || allDepts.isEmpty()) {
                log.info("没有部门数据，跳过同步");
                return;
            }
            log.info("获取到 {} 个部门", allDepts.size());

            // 2. 获取所有员工（去重）
            java.util.Set<String> allUserIds = new java.util.HashSet<>();
            for (WxCpDepart dept : allDepts) {
                try {
                    List<WxCpUser> users = wxCpService.getUserService().listByDepartment(dept.getId(), true, 0);
                    if (users != null) {
                        for (WxCpUser user : users) {
                            allUserIds.add(user.getUserId());
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取部门 {} 成员失败: {}", dept.getId(), e.getMessage());
                }
            }

            log.info("共获取到 {} 个员工，开始逐个同步客户", allUserIds.size());

            // 3. 遍历每个员工，同步客户
            int total = 0;
            int userIndex = 0;
            int userSize = allUserIds.size();
            for (String userid : allUserIds) {
                userIndex++;
                try {
                    List<String> externalUserids = wxCpService.getExternalContactService()
                            .listExternalContacts(userid);

                    if (externalUserids == null || externalUserids.isEmpty()) {
                        log.info("[{}/{}] 员工 {} 没有客户", userIndex, userSize, userid);
                        continue;
                    }

                    for (String externalUserid : externalUserids) {
                        syncCustomerDetail(externalUserid, userid);
                        total++;
                    }

                    log.info("[{}/{}] 员工 {} 同步完成，客户数 {}，累计 {}", userIndex, userSize, userid, externalUserids.size(), total);
                } catch (Exception e) {
                    log.error("[{}/{}] 同步员工 {} 的客户失败", userIndex, userSize, userid, e);
                }
            }

            log.info("========== 客户数据同步完成，共 {} 个 ==========", total);
        } catch (Exception e) {
            log.error("同步客户数据失败", e);
            throw new RuntimeException("同步客户数据失败: " + e.getMessage());
        }
    }

    private void syncCustomerDetail(String externalUserid, String followUserid) {
        try {
            WxCpExternalContactInfo contactInfo = wxCpService.getExternalContactService()
                    .getExternalContact(externalUserid);

            if (contactInfo == null || contactInfo.getExternalContact() == null) {
                return;
            }

            // 保存客户信息
            ExternalContact ext = contactInfo.getExternalContact();
            WecomCustomer customer = new WecomCustomer();
            customer.setExternalUserid(ext.getExternalUserId());
            customer.setName(ext.getName());
            customer.setNickname(ext.getNickname());
            customer.setAvatar(ext.getAvatar());
            customer.setGender(ext.getGender());
            customer.setType(ext.getType());
            customer.setCorpName(ext.getCorpName());
            customer.setCorpFullName(ext.getCorpFullName());
            customer.setPosition(ext.getPosition());
            customer.setUnionId(ext.getUnionId());

            WecomCustomer exist = customerMapper.selectOne(
                    new LambdaQueryWrapper<WecomCustomer>()
                            .eq(WecomCustomer::getExternalUserid, ext.getExternalUserId())
            );

            if (exist != null) {
                customer.setId(exist.getId());
                customerMapper.updateById(customer);
            } else {
                customerMapper.insert(customer);
            }

            // 保存跟进人关系
            if (contactInfo.getFollowedUsers() != null) {
                for (FollowedUser followedUser : contactInfo.getFollowedUsers()) {
                    WecomCustomerFollow follow = new WecomCustomerFollow();
                    follow.setExternalUserid(externalUserid);
                    follow.setUserid(followedUser.getUserId());
                    follow.setRemark(followedUser.getRemark());
                    follow.setDescription(followedUser.getDescription());
                    follow.setFollowCreateTime(followedUser.getCreateTime());
                    follow.setState(followedUser.getState());
                    follow.setRemarkCompany(followedUser.getRemarkCompany());
                    follow.setRemarkCorpName(followedUser.getRemarkCorpName());
                    follow.setAddWay(followedUser.getAddWay());
                    follow.setOperatorUserid(followedUser.getOperatorUserId());

                    // 手机号备注
                    if (followedUser.getRemarkMobiles() != null) {
                        follow.setRemarkMobiles(toJson(followedUser.getRemarkMobiles()));
                    }

                    // 标签
                    if (followedUser.getTagIds() != null) {
                        follow.setTagIds(toJson(followedUser.getTagIds()));
                    }
                    if (followedUser.getTags() != null) {
                        follow.setTags(toJson(followedUser.getTags()));
                    }

                    // 视频号
                    if (followedUser.getWechatChannels() != null) {
                        follow.setWechatChannelsNickname(followedUser.getWechatChannels().getNickname());
                        follow.setWechatChannelsSource(followedUser.getWechatChannels().getSource());
                    }

                    WecomCustomerFollow existFollow = customerFollowMapper.selectOne(
                            new LambdaQueryWrapper<WecomCustomerFollow>()
                                    .eq(WecomCustomerFollow::getExternalUserid, externalUserid)
                                    .eq(WecomCustomerFollow::getUserid, followedUser.getUserId())
                    );

                    if (existFollow != null) {
                        follow.setId(existFollow.getId());
                        customerFollowMapper.updateById(follow);
                    } else {
                        customerFollowMapper.insert(follow);
                    }
                }
            }
        } catch (Exception e) {
            log.error("同步客户详情失败, externalUserid={}", externalUserid, e);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncGroupChats() {
        log.info("========== 开始同步企微客群数据 ==========");
        try {
            String cursor = null;
            int total = 0;
            int batch = 0;

            do {
                batch++;
                WxCpUserExternalGroupChatList listInfo = wxCpService.getExternalContactService()
                        .listGroupChat(100, cursor, 0, null);

                if (listInfo == null || listInfo.getGroupChatList() == null || listInfo.getGroupChatList().isEmpty()) {
                    log.info("第 {} 批无数据，结束同步", batch);
                    break;
                }

                int batchSize = listInfo.getGroupChatList().size();
                for (WxCpUserExternalGroupChatList.ChatStatus chatStatus : listInfo.getGroupChatList()) {
                    syncGroupChatDetail(chatStatus.getChatId());
                    total++;
                }

                cursor = listInfo.getNextCursor();
                log.info("第 {} 批完成，本批 {} 个，累计 {} 个，hasMore={}", batch, batchSize, total, StringUtils.isNotBlank(cursor));
            } while (StringUtils.isNotBlank(cursor));

            log.info("========== 客群数据同步完成，共 {} 个 ==========", total);
        } catch (Exception e) {
            log.error("同步客群数据失败", e);
            throw new RuntimeException("同步客群数据失败: " + e.getMessage());
        }
    }

    private void syncGroupChatDetail(String chatId) {
        try {
            log.debug("同步客群详情: {}", chatId);
            WxCpUserExternalGroupChatInfo chatInfo = wxCpService.getExternalContactService()
                    .getGroupChat(chatId, 1);

            if (chatInfo == null || chatInfo.getGroupChat() == null) {
                return;
            }

            WxCpUserExternalGroupChatInfo.GroupChat groupChat = chatInfo.getGroupChat();
            int memberCount = groupChat.getMemberList() != null ? groupChat.getMemberList().size() : 0;
            log.info("同步客群: {} ({}), 成员数: {}", groupChat.getName(), chatId, memberCount);

            // 保存群信息
            WecomGroupChat chat = new WecomGroupChat();
            chat.setChatId(groupChat.getChatId());
            chat.setName(groupChat.getName());
            chat.setOwner(groupChat.getOwner());
            chat.setCreateTimeField(groupChat.getCreateTime());
            chat.setNotice(groupChat.getNotice());
            chat.setMemberCount(memberCount);
            chat.setStatus(1);

            WecomGroupChat exist = groupChatMapper.selectOne(
                    new LambdaQueryWrapper<WecomGroupChat>()
                            .eq(WecomGroupChat::getChatId, chatId)
            );

            if (exist != null) {
                chat.setId(exist.getId());
                groupChatMapper.updateById(chat);
            } else {
                groupChatMapper.insert(chat);
            }

            // 清除旧成员，重新插入
            groupChatMemberMapper.delete(
                    new LambdaQueryWrapper<WecomGroupChatMember>()
                            .eq(WecomGroupChatMember::getChatId, chatId)
            );

            // 保存成员列表
            if (groupChat.getMemberList() != null) {
                for (WxCpUserExternalGroupChatInfo.GroupMember member : groupChat.getMemberList()) {
                    WecomGroupChatMember chatMember = new WecomGroupChatMember();
                    chatMember.setChatId(chatId);
                    chatMember.setUserId(member.getUserId());
                    chatMember.setMemberType(member.getType());
                    chatMember.setJoinTime(member.getJoinTime());
                    chatMember.setJoinScene(member.getJoinScene());
                    chatMember.setGroupNickname(member.getGroupNickname());
                    chatMember.setName(member.getName());
                    groupChatMemberMapper.insert(chatMember);
                }
            }
        } catch (Exception e) {
            log.error("同步客群详情失败, chatId={}", chatId, e);
        }
    }

    @Override
    public IPage<WecomCustomer> getCustomerList(String userid, String keyword, Integer gender, Integer type, String corpName, Integer pageNum, Integer pageSize) {
        Page<WecomCustomer> page = new Page<>(pageNum, pageSize);

        // 先查询该员工跟进的客户external_userid列表
        LambdaQueryWrapper<WecomCustomerFollow> followWrapper = new LambdaQueryWrapper<WecomCustomerFollow>()
                .eq(WecomCustomerFollow::getUserid, userid);
        List<WecomCustomerFollow> follows = customerFollowMapper.selectList(followWrapper);

        if (follows.isEmpty()) {
            return page;
        }

        List<String> externalUserids = follows.stream()
                .map(WecomCustomerFollow::getExternalUserid)
                .toList();

        // 查询客户信息
        LambdaQueryWrapper<WecomCustomer> wrapper = new LambdaQueryWrapper<WecomCustomer>()
                .in(WecomCustomer::getExternalUserid, externalUserids);

        // 客户姓名
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(WecomCustomer::getName, keyword);
        }

        // 性别
        if (gender != null) {
            wrapper.eq(WecomCustomer::getGender, gender);
        }

        // 类型
        if (type != null) {
            wrapper.eq(WecomCustomer::getType, type);
        }

        // 所属企业
        if (StringUtils.isNotBlank(corpName)) {
            wrapper.like(WecomCustomer::getCorpName, corpName);
        }

        wrapper.orderByDesc(WecomCustomer::getCreateTime);
        return customerMapper.selectPage(page, wrapper);
    }

    @Override
    public WecomCustomer getCustomerDetail(String externalUserid) {
        return customerMapper.selectOne(
                new LambdaQueryWrapper<WecomCustomer>()
                        .eq(WecomCustomer::getExternalUserid, externalUserid)
        );
    }

    @Override
    public IPage<WecomGroupChat> getGroupChatList(String userid, String keyword, Integer pageNum, Integer pageSize) {
        Page<WecomGroupChat> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<WecomGroupChat> wrapper = new LambdaQueryWrapper<WecomGroupChat>()
                .eq(WecomGroupChat::getOwner, userid);

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(WecomGroupChat::getName, keyword);
        }

        wrapper.orderByDesc(WecomGroupChat::getCreateTime);
        return groupChatMapper.selectPage(page, wrapper);
    }

    @Override
    public WecomGroupChat getGroupChatDetail(String chatId) {
        return groupChatMapper.selectOne(
                new LambdaQueryWrapper<WecomGroupChat>()
                        .eq(WecomGroupChat::getChatId, chatId)
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON序列化失败", e);
            return null;
        }
    }
}
