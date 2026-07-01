package com.ewcp.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ewcp.common.result.R;
import com.ewcp.entity.Content;
import com.ewcp.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/library/content")
@RequiredArgsConstructor
public class ContentController {

    private final SystemService systemService;

    @GetMapping("/page")
    public R<Page<Content>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status
    ) {
        return R.ok(systemService.getContentPage(pageNum, pageSize, type, title, status));
    }

    @PostMapping
    public R<Void> create(@RequestBody Content content) {
        systemService.createContent(content);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody Content content) {
        systemService.updateContent(content);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        systemService.deleteContent(id);
        return R.ok();
    }
}
