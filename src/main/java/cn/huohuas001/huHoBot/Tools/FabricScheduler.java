package cn.huohuas001.huHoBot.Tools;

import cn.huohuas001.huHoBot.HuHoBot;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Iterator;
import java.util.Map;

public class FabricScheduler {

    private static final Logger LOGGER = HuHoBot.LOGGER;
    private static final MinecraftServer SERVER = HuHoBot.getInstance().getServer();
    private static final Map<Long, ScheduledTask> TASKS = new ConcurrentHashMap<>();
    private static final AtomicLong TASK_ID_GENERATOR = new AtomicLong(0);

    public static void startScheduler() {
        Thread tickThread = new Thread(() -> {
            while (SERVER != null && !SERVER.isStopped()) {
                try {
                    processTasks();
                    Thread.sleep(50); // 1 Tick ≈ 50ms
                } catch (InterruptedException ignored) {}
            }
        }, "HuHoBotScheduler-Thread");
        tickThread.setDaemon(true); // 守护线程，不阻止 JVM 退出
        tickThread.start();
    }

    private static void processTasks() {
        Iterator<Map.Entry<Long, ScheduledTask>> iterator = TASKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, ScheduledTask> entry = iterator.next();
            ScheduledTask task = entry.getValue();

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
        private final long id;
        private final Runnable runnable;
        private long ticksLeft;
        private final int intervalTicks;
        private final boolean isLoop;
        private volatile boolean cancelled = false;

        private ScheduledTask(long id, Runnable runnable, long delayTicks, int intervalTicks, boolean isLoop) {
            this.id = id;
            this.runnable = runnable;
            this.ticksLeft = delayTicks;
            this.intervalTicks = intervalTicks;
            this.isLoop = isLoop;
        }

        public void cancel() {
            this.cancelled = true;
            TASKS.remove(this.id);
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

    // 一次性延迟任务
    public static ScheduledTask runTaskLater(Runnable task, long delayTicks) {
        long taskId = TASK_ID_GENERATOR.incrementAndGet();
        ScheduledTask scheduled = new ScheduledTask(taskId, task, delayTicks, 0, false);
        TASKS.put(taskId, scheduled);
        return scheduled;
    }

    // 循环任务
    public static ScheduledTask runDelayedLoop(Runnable task, long delayTicks, int intervalTicks) {
        long taskId = TASK_ID_GENERATOR.incrementAndGet();
        ScheduledTask scheduled = new ScheduledTask(taskId, task, delayTicks, intervalTicks, true);
        TASKS.put(taskId, scheduled);
        return scheduled;
    }

    public static ScheduledTask runDelayedLoop(Runnable task, long delayTicks) {
        return runDelayedLoop(task, delayTicks, 20); // 默认间隔20 ticks (1秒)
    }

    // 立即执行
    public static void runTask(Runnable task) {
        if (SERVER != null && !SERVER.isStopped()) {
            SERVER.submit(task);
        }
    }
}
