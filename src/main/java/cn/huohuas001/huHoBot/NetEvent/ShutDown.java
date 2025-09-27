package cn.huohuas001.huHoBot.NetEvent;

import cn.huohuas001.huHoBot.HuHoBot;
import org.apache.logging.log4j.Logger;


public class ShutDown extends EventRunner {
    private HuHoBot plugin = HuHoBot.getInstance();
    private Logger logger = HuHoBot.LOGGER;

    @Override
    public boolean run() {
        logger.error("服务端命令断开连接 原因:{}", body.getString("msg"));
        logger.error("此错误具有不可容错性!请检查插件配置文件!");
        logger.warn("正在断开连接...");
        plugin.disConnectServer();
        return true;
    }
}