package com.wecorp.controller.wx;

import com.wecorp.common.result.R;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.WxCpDepart;
import me.chanjar.weixin.cp.bean.WxCpUser;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wx")
@RequiredArgsConstructor
public class WxMemberController {

    private final WxCpService wxCpService;

    /**
     * 获取部门树（一次请求获取全部门，按 parentId 构建树）
     */
    @GetMapping("/departments")
    public R<List<Map<String, Object>>> getDepartments() {
        try {
            // id=1 返回全部门（含子部门），一次请求搞定
            List<WxCpDepart> allDepts = wxCpService.getDepartmentService().list(1L);
            List<Map<String, Object>> tree = buildDeptTree(allDepts, 0L);
            return R.ok(tree);
        } catch (WxErrorException e) {
            return R.fail("获取部门列表失败: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> buildDeptTree(List<WxCpDepart> allDepts, Long parentId) {
        return allDepts.stream()
                .filter(d -> parentId.equals(d.getParentId()))
                .sorted((a, b) -> Long.compare(
                        a.getOrder() != null ? a.getOrder() : 0,
                        b.getOrder() != null ? b.getOrder() : 0))
                .map(dept -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", dept.getId());
                    map.put("name", dept.getName());
                    map.put("parentId", dept.getParentId());
                    map.put("order", dept.getOrder());
                    List<Map<String, Object>> children = buildDeptTree(allDepts, dept.getId());
                    if (!children.isEmpty()) {
                        map.put("children", children);
                    }
                    return map;
                })
                .toList();
    }

    /**
     * 获取部门下的成员列表（已激活）
     */
    @GetMapping("/departments/{deptId}/members")
    public R<List<Map<String, Object>>> getDepartmentMembers(@PathVariable Long deptId) {
        try {
            // status=4 表示获取已激活的成员
            List<WxCpUser> users = wxCpService.getUserService().listByDepartment(deptId, true, 0);
            List<Map<String, Object>> result = users.stream().map(user -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("userId", user.getUserId());
                map.put("name", user.getName());
                map.put("mainDepartment", user.getMainDepartment());
                return map;
            }).toList();
            return R.ok(result);
        } catch (WxErrorException e) {
            return R.fail("获取部门成员失败: " + e.getMessage());
        }
    }
}
