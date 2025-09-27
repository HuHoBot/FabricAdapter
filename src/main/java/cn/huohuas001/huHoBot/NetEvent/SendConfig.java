package cn.huohuas001.huHoBot.NetEvent;

import cn.huohuas001.huHoBot.HuHoBot;
import org.apache.logging.log4j.Logger;

public class SendConfig extends EventRunner {
    private Logger logger = HuHoBot.LOGGER;

    @Override
    public boolean run() {
        String hashKey = body.getString("hashKey");
        getConfigManager().setHashKey(hashKey);
        logger.info("配置文件已接受.");
        logger.info("自动断开连接以刷新配置文件...");
        HuHoBot.getClientManager().shutdownClient();
        return true;
    }
}
