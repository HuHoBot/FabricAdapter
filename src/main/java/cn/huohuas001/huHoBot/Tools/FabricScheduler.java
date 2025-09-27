package cn.huohuas001.huHoBot.Tools;

import cn.huohuas001.huHoBot.HuHoBot;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Fabric 调度器工具类（兼容原 runDelayedLoop 风格）
 * 基于服务器 Tick，不开额外线程，关服安全
 */
public class FabricScheduler {

    private static final Logger LOGGER = HuHoBot.LOGGER;
    private static final MinecraftServer SERVER = HuHoBot.getInstance().getServer();

    private static final List<ScheduledTask> TASKS = new LinkedList<>();

    static {
        // 每个服务器 Tick 更新一次任务
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<ScheduledTask> iterator = TASKS.iterator();
            while (iterator.hasNext()) {
                ScheduledTask task = iterator.next();
                if (task.isCancelled()) {
                    iterator.remove();
                    continue;
                }
                task.ticksLeft--;
                if (task.ticksLeft <= 0) {
                    try {
                        task.runnable.run();
                    } catch (Exception e) {
                        LOGGER.error("任务执行异常", e);
                    }
                    if (task.isLoop) {
                        task.ticksLeft = task.intervalTicks; // 重置间隔
                    } else {
                        iterator.remove();
                    }
                }
            }
        });
    }

    public static class ScheduledTask {
        private final Runnable runnable;
        private long ticksLeft;
        private final int intervalTicks;
        private final boolean isLoop;
        private boolean cancelled = false;

        private ScheduledTask(Runnable runnable, long delayTicks, int intervalTicks, boolean isLoop) {
            this.runnable = runnable;
            this.ticksLeft = delayTicks;
            this.intervalTicks = intervalTicks;
            this.isLoop = isLoop;
        }

        public void cancel() {
            this.cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

    /**
     * 延迟执行一次任务
     */
    public static ScheduledTask runTaskLater(Runnable task, long delayTicks) {
        ScheduledTask scheduled = new ScheduledTask(task, delayTicks, 0, false);
        TASKS.add(scheduled);
        return scheduled;
    }



    /**
     * 延迟循环执行任务（兼容原 runDelayedLoop 风格）
     * delayTicks: 首次延迟
     * intervalTicks: 循环间隔
     */
    public static ScheduledTask runDelayedLoop(Runnable task, long delayTicks, int intervalTicks) {
        ScheduledTask scheduled = new ScheduledTask(task, delayTicks, intervalTicks, true);
        TASKS.add(scheduled);
        return scheduled;
    }

    /**
     * 延迟循环执行任务（兼容原 runDelayedLoop 风格）
     * delayTicks: 首次延迟
     */
    public static ScheduledTask runDelayedLoop(Runnable task, long delayTicks) {
        return runDelayedLoop(task, delayTicks, 0);
    }

    /**
     * 立即执行任务（安全提交到服务器线程）
     */
    public static void runTask(Runnable task) {
        if (SERVER != null && !SERVER.isStopped()) {
            SERVER.submit(task);
        }
    }
}
