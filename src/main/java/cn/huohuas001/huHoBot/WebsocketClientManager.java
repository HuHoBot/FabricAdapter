package cn.huohuas001.huHoBot;

import cn.huohuas001.huHoBot.Tools.FabricScheduler;
import com.alibaba.fastjson2.JSONObject;
import org.apache.logging.log4j.Logger;
import cn.huohuas001.config.ServerConfig;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * WebSocket 客户端管理（Fabric 兼容版，无 timer）
 */
public class WebsocketClientManager {
    private static WsClient client; // Websocket客户端
    private static String websocketUrl = ServerConfig.WS_SERVER_URL; // 可从配置读取
    private final long RECONNECT_DELAY = 5; // 重连延迟时间（秒）
    private final int MAX_RECONNECT_ATTEMPTS = 5; // 最大重连尝试次数
    private final HuHoBot plugin;
    private final Logger logger;
    private int reconnectAttempts = 0;
    private boolean shouldReconnect = true; // 控制是否重连
    private FabricScheduler.ScheduledTask currentTask;      // 重连任务
    private FabricScheduler.ScheduledTask autoDisConnectTask; // 超时断开任务
    private FabricScheduler.ScheduledTask heartBeatTask;    // 心跳任务

    public WebsocketClientManager() {
        plugin = HuHoBot.getInstance();
        logger = plugin.LOGGER;
    }

    /**
     * 设置是否重连
     */
    public void setShouldReconnect(boolean shouldReconnect) {
        this.shouldReconnect = shouldReconnect;
    }

    /**
     * 自动重连逻辑（递归调用）
     */
    private void autoReconnect() {
        synchronized (this) {
            reconnectAttempts++;
            if (reconnectAttempts > MAX_RECONNECT_ATTEMPTS) {
                logger.warn("重连尝试已达到最大次数（"+MAX_RECONNECT_ATTEMPTS+"次），不再尝试连接。");
                cancelCurrentTask();
                return;
            }
            if (!shouldReconnect) {
                cancelCurrentTask();
                return;
            }
            logger.info("尝试重新连接 WebSocket（"+reconnectAttempts+"/"+MAX_RECONNECT_ATTEMPTS+"）");
            this.connectServer();
        }
    }

    /**
     * 取消当前重连任务
     */
    public void cancelCurrentTask() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
            reconnectAttempts = 0;
            //logger.("重连任务已取消");
        }
    }

    /**
     * 取消超时断开任务
     */
    private void cancelAutoDisConnectTask() {
        if (autoDisConnectTask != null) {
            autoDisConnectTask.cancel();
            autoDisConnectTask = null;
            //logger.debug("超时断开任务已取消");
        }
    }

    /**
     * 取消心跳任务
     */
    private void cancelHeartBeatTask() {
        if (heartBeatTask != null) {
            heartBeatTask.cancel();
            heartBeatTask = null;
            //logger.debug("心跳任务已取消");
        }
    }

    public WsClient getClient() {
        return client;
    }

    /**
     * 关闭客户端并取消所有任务
     */
    public boolean shutdownClient() {
        boolean closed = false;
        if (client != null && client.isOpen()) {
            client.close(1000); // 正常关闭
            closed = true;
            logger.info("WebSocket客户端已关闭");
        }
        cancelCurrentTask();
        cancelAutoDisConnectTask();
        cancelHeartBeatTask();
        return closed;
    }

    /**
     * 超时断开逻辑（6小时后执行）
     */
    public void autoDisConnectClient() {
        logger.info("WebSocket连接超时（6小时），已自动断开并尝试重连");
        shutdownClient();
        clientReconnect();
    }

    /**
     * 设置超时断开任务
     */
    public void setAutoDisConnectTask() {
        cancelAutoDisConnectTask();
        long sixHoursTicks = 6L * 60 * 60 * 20; // 6小时 → Tick
        autoDisConnectTask = FabricScheduler.runTaskLater(this::autoDisConnectClient, sixHoursTicks);
        //logger.debug("超时断开任务已设置（6小时后执行）");
    }

    /**
     * 启动心跳任务（30秒一次）
     */
    public void startHeartBeatTask() {
        cancelHeartBeatTask();
        heartBeatTask = FabricScheduler.runDelayedLoop(this::sendHeart, 5*20L); // 30秒 = 600 Tick
        logger.info("HuHoBot 心跳任务已启动");
    }

    /**
     * 连接 WebSocket 服务器
     */
    public boolean connectServer() {
        try {
            URI uri = new URI(websocketUrl);
            if (client == null || !client.isOpen()) {
                client = new WsClient(uri, this);
                setShouldReconnect(true);
                client.connect();

                // 连接成功后启动心跳和超时断开
                startHeartBeatTask();
                setAutoDisConnectTask();
            }
            return true;
        } catch (URISyntaxException e) {
            logger.error("WebSocket地址格式错误："+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean isOpen() {
        return client != null && client.isOpen();
    }

    /**
     * 发送心跳
     */
    public void sendHeart() {
        if (isOpen()) {
            client.sendMessage("heart", new JSONObject());
            //logger.info("已发送心跳包");
        } else {
            logger.warn("心跳包发送失败：WebSocket连接已断开");
            cancelHeartBeatTask();
        }
    }

    /**
     * 触发重连（递归延迟实现循环）
     */
    public void clientReconnect() {
        if (shouldReconnect && currentTask == null) {
            long delayTicks = RECONNECT_DELAY * 20L; // 5秒 → Tick
            currentTask = FabricScheduler.runDelayedLoop(this::autoReconnect, delayTicks);
            //logger.debug("重连任务已启动（每{}秒一次）", RECONNECT_DELAY);
        }
    }
}
