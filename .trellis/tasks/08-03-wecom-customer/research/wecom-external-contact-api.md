# 企微外部联系人API调研报告

## 1. SDK信息
- **SDK**: WxJava (weixin-java-cp) by binarywang
- **版本**: 4.8.0
- **包路径**: me.chanjar.weixin.cp.api.WxCpExternalContactService
- **访问方式**: wxCpService.getExternalContactService().<method>(...)

## 2. 客户列表API

### 2.1 listExternalContacts (旧版)
```java
List<String> listExternalContacts(String userid) throws WxErrorException;
```
- **企微接口**: `GET /cgi-bin/externalcontact/list?userid={userid}`
- **说明**: 通过员工userid获取客户的external_userid列表
- **限制**: 只返回ID列表，无详情

### 2.2 getContactList (新版，推荐)
```java
WxCpExternalContactListInfo getContactList(String cursor, Integer limit) throws WxErrorException;
```
- **企微接口**: `POST /cgi-bin/externalcontact/contact_list`
- **说明**: 全量获取组织客户列表，支持分页
- **返回字段**: externalUserid, name, followUserid, addTime等
- **关键字段**: `followUserid` 可用于过滤特定员工的客户

## 3. 客户详情API

### 3.1 getExternalContact
```java
WxCpExternalContactInfo getExternalContact(String externalUserId) throws WxErrorException;
```
- **企微接口**: `GET /cgi-bin/externalcontact/get?external_userid={externalUserId}`
- **返回**: ExternalContact(姓名、头像、性别等) + FollowedUser列表

### 3.2 getContactDetailBatch
```java
WxCpExternalContactBatchInfo getContactDetailBatch(String[] externalUserIds, String cursor, Integer limit) throws WxErrorException;
```
- **企微接口**: `POST /cgi-bin/externalcontact/batch/get_by_user`
- **限制**: 最多100个external_userid

## 4. 客群列表API

### 4.1 listGroupChat
```java
WxCpUserExternalGroupChatList listGroupChat(Integer limit, String cursor, int statusFilter, String[] userIds) throws WxErrorException;
```
- **企微接口**: `POST /cgi-bin/externalcontact/groupchat/list`
- **参数**: userIds可过滤特定员工的群
- **返回**: chatId和status列表

### 4.2 getGroupChat
```java
WxCpUserExternalGroupChatInfo getGroupChat(String chatId, Integer needName) throws WxErrorException;
```
- **企微接口**: `POST /cgi-bin/externalcontact/groupchat/get`
- **返回**: 群名、群主、成员列表等

## 5. 数据结构

### ExternalContact (客户信息)
- externalUserId, name, nickname, avatar, gender
- corpName, corpFullName, position, unionId
- type, externalProfile

### FollowedUser (跟进人信息)
- userId, remark, description, createTime
- state, remarkCompany, remarkMobiles
- tagIds, tags, remarkCorpName, addWay

### GroupChat (客群信息)
- chatId, name, owner, createTime, notice
- memberList, adminList

### GroupMember (群成员)
- userId, type(1=企业成员,2=外部联系人)
- joinTime, joinScene, groupNickname, name
