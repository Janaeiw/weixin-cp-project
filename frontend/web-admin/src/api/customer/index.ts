import { http } from "@/utils/http";
import type { ApiResult } from "@/types/api";

/** 企微客户 */
export type WecomCustomer = {
  id: number;
  externalUserid: string;
  name: string;
  nickname: string;
  avatar: string;
  gender: number;
  type: number;
  corpName: string;
  corpFullName: string;
  position: string;
  unionId: string;
  externalProfile: string;
  createTime: string;
  updateTime: string;
};

/** 客户跟进人 */
export type WecomCustomerFollow = {
  id: number;
  externalUserid: string;
  userid: string;
  remark: string;
  description: string;
  followCreateTime: string;
  state: string;
  remarkCompany: string;
  remarkMobiles: string;
  tagIds: string;
  tags: string;
  remarkCorpName: string;
  addWay: string;
  operatorUserid: string;
  wechatChannelsNickname: string;
  wechatChannelsSource: number;
  createTime: string;
};

/** 客群 */
export type WecomGroupChat = {
  id: number;
  chatId: string;
  name: string;
  owner: string;
  createTimeField: string;
  notice: string;
  memberCount: number;
  /** 跟进人状态: 0-跟进人正常 1-跟进人离职 2-离职继承中 3-离职继承完成 */
  status: number;
  adminList: string;
  memberVersion: string;
  createTime: string;
  updateTime: string;
};

/** 客群成员 */
export type WecomGroupChatMember = {
  id: number;
  chatId: string;
  userId: string;
  memberType: number;
  joinTime: string;
  joinScene: number;
  groupNickname: string;
  name: string;
  invitor: string;
  unionId: string;
  createTime: string;
};

/** 分页结果 */
export type PageResult<T> = {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
};

/** 触发全量同步 */
export const syncCustomers = () => {
  return http.request<ApiResult<void>>("post", "/api/customer/sync");
};

/** 获取客户列表 */
export const getCustomerList = (params: {
  userId: string;
  keyword?: string;
  gender?: number;
  type?: number;
  corpName?: string;
  pageNum?: number;
  pageSize?: number;
}) => {
  return http.request<ApiResult<PageResult<WecomCustomer>>>(
    "get",
    "/api/customer/list",
    { params }
  );
};

/** 获取客户详情 */
export const getCustomerDetail = (externalUserid: string) => {
  return http.request<ApiResult<WecomCustomer>>(
    "get",
    `/api/customer/${externalUserid}`
  );
};

/** 获取客户跟进人列表 */
export const getCustomerFollows = (externalUserid: string) => {
  return http.request<ApiResult<WecomCustomerFollow[]>>(
    "get",
    `/api/customer/${externalUserid}/follows`
  );
};

/** 获取客群列表 */
export const getGroupChatList = (params: {
  userId: string;
  keyword?: string;
  owner?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}) => {
  return http.request<ApiResult<PageResult<WecomGroupChat>>>(
    "get",
    "/api/customer/group-chat/list",
    { params }
  );
};

/** 获取客群详情 */
export const getGroupChatDetail = (chatId: string) => {
  return http.request<ApiResult<WecomGroupChat>>(
    "get",
    `/api/customer/group-chat/${chatId}`
  );
};

/** 获取客群成员列表 */
export const getGroupChatMembers = (chatId: string) => {
  return http.request<ApiResult<WecomGroupChatMember[]>>(
    "get",
    `/api/customer/group-chat/${chatId}/members`
  );
};
