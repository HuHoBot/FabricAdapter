package cn.huohuas001.huHoBot.Tools;

import cn.huohuas001.huHoBot.HuHoBot;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FabricScheduler {

    private static final Logger LOGGER = HuHoBot.LOGGER;
    private static final MinecraftServer SERVER = HuHoBot.getInstance().getServer();
    private static final List<ScheduledTask> TASKS = new LinkedList<>();

    public static void startScheduler() {
        Thread tickThread = new Thread(() -> {
            while (SERVER != null && !SERVER.isStopped()) {
                try {
                    processTasks();
                    Thread.sleep(50); // 1 Tick ≈ 50ms
                } catch (InterruptedException ignored) {}
            }
        }, "FabricScheduler-Thread");
        tickThread.setDaemon(true); // 守护线程，不阻止 JVM 退出
        tickThread.start();
    }

    private static void processTasks() {
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
                    task.ticksLeft = task.intervalTicks;
                } else {
                    iterator.remove();
                }
            }
        }
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

    // 一次性延迟任务
    public static ScheduledTask runTaskLater(Runnable task, long delayTicks) {
        ScheduledTask scheduled = new ScheduledTask(task, delayTicks, 0, false);
        TASKS.add(scheduled);
        return scheduled;
    }

    // 循环任务（兼容原 runDelayedLoop）
    public static ScheduledTask runDelayedLoop(Runnable task, long delayTicks, int intervalTicks) {
        ScheduledTask scheduled = new ScheduledTask(task, delayTicks, intervalTicks, true);
        TASKS.add(scheduled);
        return scheduled;
    }

    public static ScheduledTask runDelayedLoop(Runnable task, long delayTicks) {
        return runDelayedLoop(task, delayTicks, 0);
    }

    // 立即执行
    public static void runTask(Runnable task) {
        if (SERVER != null && !SERVER.isStopped()) {
            SERVER.submit(task);
        }
    }
}
