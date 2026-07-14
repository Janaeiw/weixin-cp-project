import { http } from "@/utils/http";

type RouteMeta = {
  title: string;
  icon?: string;
  rank?: number;
  roles?: string[];
  auths?: string[];
};

type RouteItem = {
  path: string;
  name?: string;
  component?: string;
  meta: RouteMeta;
  children?: RouteItem[];
};

type Result = {
  code: number;
  msg?: string;
  data: Array<RouteItem>;
};

export const getAsyncRoutes = () => {
  return http.request<Result>("get", "/api/auth/routes");
  return http.request<Result>("get", "/get-async-routes");
};
