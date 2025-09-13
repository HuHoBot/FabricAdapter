package cn.huohuas001.huHoBot.NetEvent;

import cn.huohuas001.huHoBot.HuHoBot;
import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

public class bindRequest extends EventRunner {
    private final Logger logger = HuHoBot.LOGGER;
    private final Map<String, String> bindMap = new HashMap<>();

    @Override
    public boolean run() {
        String bindCode = body.getString("bindCode");
        logger.info("收到一个新的绑定请求，如确认绑定，请输入\"/huhobot bind {}\"来进行确认", bindCode);
        bindMap.put(bindCode, packId);
        return true;
    }

    public boolean confirmBind(String bindCode) {
        if (bindMap.containsKey(bindCode)) {
            sendMessage("bindConfirm", new JSONObject());
            bindMap.remove(bindCode);
            return true;
        }
        return false;
    }
}
