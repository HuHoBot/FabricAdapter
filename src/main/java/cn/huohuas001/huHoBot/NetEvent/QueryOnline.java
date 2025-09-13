package cn.huohuas001.huHoBot.NetEvent;

import cn.huohuas001.huHoBot.HuHoBot;
import com.alibaba.fastjson2.JSONObject;

public class QueryOnline extends EventRunner {

    @Override
    public boolean run() {
        //获取motd Config
        String server_ip = getConfigManager().getString("motd.server_ip","127.0.0.1");
        int server_port = getConfigManager().getInt("motd.server_port",25565);
        String api = getConfigManager().getString("motd.api","");
        String text = getConfigManager().getString("motd.text","");
        boolean output_online_list = getConfigManager().getBoolean("motd.output_online_list",true);

        StringBuilder onlineNameString = new StringBuilder();
        int onlineSize = -1;
        if (output_online_list) {
            onlineNameString.append("\n在线玩家列表：\n");
            String[] onlineList = HuHoBot.getInstance().getServer().getPlayerNames();
            for (String playerName : onlineList) {
                onlineNameString.append(playerName).append("\n");
            }
            onlineSize = onlineList.length;
        }

        onlineNameString.append(text.replace("{online}", String.valueOf(onlineSize)));


        // 构造JSON对象
        JSONObject list = new JSONObject();
        list.put("msg", onlineNameString);
        list.put("url", server_ip + ":" + server_port);
        list.put("imgUrl", api.replace("{server_ip}", server_ip).replace("{server_port}", String.valueOf(server_port)));
        list.put("post_img", getConfigManager().getBoolean("motd.post_img",false));
        list.put("serverType", "java");
        JSONObject rBody = new JSONObject();
        rBody.put("list", list);

        //返回消息
        sendMessage("queryOnline", rBody);
        return true;
    }
}
