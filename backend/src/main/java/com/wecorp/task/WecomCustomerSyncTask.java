package com.wecorp.task;

import com.wecorp.service.WecomCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 企微客户数据同步任务
 *
 * 同步策略：
 * 1. 首次全量同步：应用启动时自动执行一次
 * 2. 定时全量同步：每天凌晨1点执行（兜底，确保数据一致性）
 * 3. 增量同步：通过企微回调事件实时触发（见 ExternalContactEventHandler）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WecomCustomerSyncTask implements ApplicationRunner {

    private final WecomCustomerService wecomCustomerService;
    private final AtomicBoolean syncing = new AtomicBoolean(false);

    /**
     * 首次全量同步：应用启动完成后执行（不阻塞启动）
     */
    @Override
    public void run(ApplicationArguments args) {
        new Thread(() -> doSync("首次全量")).start();
    }

    /**
     * 定时全量同步：每天凌晨1点（兜底，与回调增量同步互补）
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledSync() {
        doSync("定时全量");
    }

    private void doSync(String source) {
        if (!syncing.compareAndSet(false, true)) {
            log.info("{}：同步正在进行，跳过本次执行", source);
            return;
        }
        try {
            log.info("{}：开始同步企微客户和客群数据", source);
            wecomCustomerService.syncCustomers();
            wecomCustomerService.syncGroupChats();
            log.info("{}：企微客户和客群数据同步完成", source);
        } catch (Exception e) {
            log.error("{}：同步企微客户数据失败", source, e);
        } finally {
            syncing.set(false);
        }
    }
}
