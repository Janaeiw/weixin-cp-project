import { http } from "@/utils/http";
import type { ApiResult } from "@/types/api";

/** 企微部门（树结构） */
export type WxDepartment = {
  id: number;
  name: string;
  parentId?: number;
  order?: number;
  children?: WxDepartment[];
};

/** 企微成员 */
export type WxMember = {
  userId: string;
  name: string;
  mainDepartment: string;
};

/** 获取企微部门列表 */
export const getWxDepartments = () => {
  return http.request<ApiResult<WxDepartment[]>>("get", "/api/wx/departments");
};

/** 获取企微部门成员列表 */
export const getWxDepartmentMembers = (deptId: number) => {
  return http.request<ApiResult<WxMember[]>>(
    "get",
    `/api/wx/departments/${deptId}/members`
  );
};
