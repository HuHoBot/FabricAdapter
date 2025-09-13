package cn.huohuas001.huHoBot.Tools;

import cn.huohuas001.huHoBot.HuHoBot;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Fabric 调度器工具类（兼容 0.42+1.16，无 Scheduler）
 * 注意：任务基于「服务器刻（Tick）」，1秒 ≈ 20 Tick
 */
public class FabricScheduler {
    private static final Logger LOGGER = HuHoBot.LOGGER;
    private static final MinecraftServer SERVER = HuHoBot.getInstance().getServer();

    // 封装任务引用（用于取消任务）
    public static class ScheduledTask {
        private final AtomicReference<Runnable> cancelFlag = new AtomicReference<>();
        private final AtomicReference<Runnable> wrappedTask = new AtomicReference<>();

        /**
         * 取消任务
         */
        public void cancel() {
            Runnable task = wrappedTask.getAndSet(null);
            if (task != null) {
                cancelFlag.set(() -> {});
                LOGGER.debug("Fabric 任务已取消");
            }
        }

        /**
         * 判断任务是否已取消
         */
        public boolean isCancelled() {
            return wrappedTask.get() == null;
        }

        private void setWrappedTask(Runnable task) {
            wrappedTask.set(task);
        }

        private Runnable getCancelFlag() {
            return cancelFlag.get();
        }
    }

    /**
     * 延迟执行任务（一次性）
     * @param task 要执行的任务
     * @param delayTicks 延迟 Tick
     * @return 封装任务对象
     */
    public static ScheduledTask runTaskLater(Runnable task, long delayTicks) {
        ScheduledTask wrapper = new ScheduledTask();
        wrapper.setWrappedTask(task);

        if (SERVER == null || SERVER.isStopped()) {
            LOGGER.warn("服务器未启动，无法执行延迟任务");
            return wrapper;
        }

        SERVER.submit(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(delayTicks * 50L); // 1 Tick ≈ 50ms
                } catch (InterruptedException ignored) {}
                if (!wrapper.isCancelled()) {
                    task.run();
                }
            }).start();
        });

        return wrapper;
    }

    /**
     * 递归延迟循环任务（替代 runTaskTimer）
     * @param task 每次执行的任务
     * @param delayTicks 间隔 Tick
     * @return 封装任务对象，可取消
     */
    public static ScheduledTask runDelayedLoop(Runnable task, long delayTicks) {
        ScheduledTask wrapper = new ScheduledTask();

        Runnable loop = new Runnable() {
            @Override
            public void run() {
                if (wrapper.isCancelled()) return;
                task.run();
                // 延迟下一次递归
                runTaskLater(this, delayTicks);
            }
        };

        wrapper.setWrappedTask(loop);
        runTaskLater(loop, delayTicks);
        return wrapper;
    }
}
