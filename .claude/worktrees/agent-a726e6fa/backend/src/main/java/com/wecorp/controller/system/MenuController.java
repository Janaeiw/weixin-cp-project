package com.wecorp.controller.system;

import com.wecorp.common.result.R;
import com.wecorp.entity.Menu;
import com.wecorp.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final SystemService systemService;

    @GetMapping("/tree")
    public R<List<Menu>> tree() {
        return R.ok(systemService.getMenuTree());
    }

    @GetMapping("/{id}")
    public R<Menu> getById(@PathVariable Long id) {
        return R.ok(systemService.getMenuById(id));
    }

    @PostMapping
    public R<Void> create(@RequestBody Menu menu) {
        systemService.createMenu(menu);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody Menu menu) {
        systemService.updateMenu(menu);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        systemService.deleteMenu(id);
        return R.ok();
    }
}
