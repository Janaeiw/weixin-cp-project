import { http } from "@/utils/http";
import type { ApiResult, PageResult } from "@/types/api";

/** 操作日志实体 */
export type OperationLogItem = {
  id: number;
  module: string;
  operation: string;
  method: string;
  requestUrl: string;
  requestMethod: string;
  requestHeaders: string;
  requestBody: string;
  responseHeaders: string;
  responseBody: string;
  statusCode: number;
  operatorId: number;
  operatorName: string;
  ip: string;
  os: string;
  browser: string;
  traceId: string;
  exceptionMsg: string;
  costTime: number;
  createTime: string;
};

/** 分页查询 */
export const getOperationLogPage = (params: {
  pageNum?: number;
  pageSize?: number;
  module?: string;
  operation?: string;
  operatorName?: string;
}) => {
  return http.request<ApiResult<PageResult<OperationLogItem>>>(
    "get",
    "/api/system/operation-log/page",
    { params }
  );
};
