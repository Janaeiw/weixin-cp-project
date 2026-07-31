package com.wecorp.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wecorp.common.result.R;
import com.wecorp.entity.OperationLog;
import com.wecorp.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/operation-log")
@RequiredArgsConstructor
public class OperationLogController {

    private final SystemService systemService;

    @GetMapping("/page")
    public R<Page<OperationLog>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String operatorName
    ) {
        return R.ok(systemService.getOperationLogPage(pageNum, pageSize, module, operation, operatorName));
    }
}
