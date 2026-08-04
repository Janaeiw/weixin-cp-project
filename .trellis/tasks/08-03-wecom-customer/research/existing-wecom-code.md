# 现有企微代码结构

## 1. 后端结构

### 配置类
- `backend/src/main/java/com/wecorp/config/WxCpConfig.java`
- 使用 @ConfigurationProperties(prefix = "wx.cp")
- 创建 WxCpServiceImpl bean

### 现有Controller
- `WxCallbackController` - 回调接口
- `MomentController` - 朋友圈相关
- `WxMemberController` - 部门和成员相关

### 使用的Service
- WxCpExternalContactService (仅朋友圈API)
- WxCpDepartmentService
- WxCpUserService

## 2. 前端结构

### API层
- `frontend/web-admin/src/api/wx/member.ts`
  - getWxDepartments()
  - getWxDepartmentMembers(deptId)

### 菜单API
- `frontend/web-admin/src/api/system/menu.ts`
  - getMenuTree()
  - createMenu(data)
  - updateMenu(data)
  - deleteMenu(id)

## 3. 数据库Migration
- 路径: `backend/src/main/resources/db/migration/`
- 格式: V{版本}__{描述}.sql
- 最新版本: V1.0.8

## 4. 现有Entity示例
- `backend/src/main/java/com/wecorp/entity/User.java`
- 包含wxUserId, wxUserName, wxDeptId, wxDeptName字段
