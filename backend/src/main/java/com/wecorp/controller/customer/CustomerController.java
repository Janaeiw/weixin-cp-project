package com.wecorp.controller.customer;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wecorp.common.result.R;
import com.wecorp.entity.WecomCustomer;
import com.wecorp.entity.WecomCustomerFollow;
import com.wecorp.entity.WecomGroupChat;
import com.wecorp.entity.WecomGroupChatMember;
import com.wecorp.mapper.WecomCustomerFollowMapper;
import com.wecorp.mapper.WecomGroupChatMemberMapper;
import com.wecorp.service.WecomCustomerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final WecomCustomerService wecomCustomerService;
    private final WecomCustomerFollowMapper customerFollowMapper;
    private final WecomGroupChatMemberMapper groupChatMemberMapper;

    /**
     * 触发全量同步
     */
    @PostMapping("/sync")
    public R<Void> sync() {
        wecomCustomerService.syncCustomers();
        wecomCustomerService.syncGroupChats();
        return R.ok();
    }

    /**
     * 获取客户列表
     */
    @GetMapping("/list")
    public R<IPage<WecomCustomer>> getCustomerList(
            @RequestParam String userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String corpName,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<WecomCustomer> page = wecomCustomerService.getCustomerList(userId, keyword, gender, type, corpName, pageNum, pageSize);
        return R.ok(page);
    }

    /**
     * 获取客户详情
     */
    @GetMapping("/{externalUserid}")
    public R<WecomCustomer> getCustomerDetail(@PathVariable String externalUserid) {
        WecomCustomer customer = wecomCustomerService.getCustomerDetail(externalUserid);
        return R.ok(customer);
    }

    /**
     * 获取客户的跟进人列表
     */
    @GetMapping("/{externalUserid}/follows")
    public R<List<WecomCustomerFollow>> getCustomerFollows(@PathVariable String externalUserid) {
        List<WecomCustomerFollow> follows = customerFollowMapper.selectList(
                new LambdaQueryWrapper<WecomCustomerFollow>()
                        .eq(WecomCustomerFollow::getExternalUserid, externalUserid)
        );
        return R.ok(follows);
    }

    /**
     * 获取客群列表
     */
    @GetMapping("/group-chat/list")
    public R<IPage<WecomGroupChat>> getGroupChatList(
            @RequestParam String userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<WecomGroupChat> page = wecomCustomerService.getGroupChatList(userId, keyword, owner, status, pageNum, pageSize);
        return R.ok(page);
    }

    /**
     * 获取客群详情
     */
    @GetMapping("/group-chat/{chatId}")
    public R<WecomGroupChat> getGroupChatDetail(@PathVariable String chatId) {
        WecomGroupChat groupChat = wecomCustomerService.getGroupChatDetail(chatId);
        return R.ok(groupChat);
    }

    /**
     * 获取客群成员列表
     */
    @GetMapping("/group-chat/{chatId}/members")
    public R<List<WecomGroupChatMember>> getGroupChatMembers(@PathVariable String chatId) {
        List<WecomGroupChatMember> members = groupChatMemberMapper.selectList(
                new LambdaQueryWrapper<WecomGroupChatMember>()
                        .eq(WecomGroupChatMember::getChatId, chatId)
        );
        return R.ok(members);
    }
}
