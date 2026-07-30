import { http } from "@/utils/http";

export type UserResult = {
  success: boolean;
  data: {
    /** 头像 */
    avatar: string;
    /** 用户名 */
    username: string;
    /** 昵称 */
    nickname: string;
    /** 当前登录用户的角色 */
    roles: Array<string>;
    /** 按钮级别权限 */
    permissions: Array<string>;
    /** 企微成员userId */
    wxUserId?: string;
    /** 企微成员名称 */
    wxUserName?: string;
    /** 企微部门id */
    wxDeptId?: number;
    /** 企微部门名称 */
    wxDeptName?: string;
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
    /** `refreshToken`的过期时间 */
    refreshExpires: Date;
  };
};

export type RefreshTokenResult = {
  success: boolean;
  data: {
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
    /** `refreshToken`的过期时间 */
    refreshExpires: Date;
  };
};

/** 后端原始响应格式 */
type BackendLoginResponse = {
  code: number;
  msg: string;
  data: {
    accessToken: string;
    refreshToken: string;
    username: string;
    nickname: string;
    avatar: string;
    roles: Array<string>;
    permissions: Array<string>;
    wxUserId?: string;
    wxUserName?: string;
    wxDeptId?: number;
    wxDeptName?: string;
    expires: Date;
    refreshExpires: Date;
  };
};

/** 登录 */
export const getLogin = (data?: object) => {
  return http
    .request<BackendLoginResponse>("post", "/api/auth/login", { data })
    .then(res => {
      const result: UserResult = {
        success: res.code === 0,
        data: {
          ...res.data
        }
      };
      return result;
    });
};

/** 刷新`token` */
export const refreshTokenApi = (data?: object) => {
  return http.request<RefreshTokenResult>(
    "post",
    "/api/auth/refresh-token",
    { data }
  );
};
