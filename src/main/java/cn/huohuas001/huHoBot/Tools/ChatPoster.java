package cn.huohuas001.huHoBot.Tools;

import cn.huohuas001.huHoBot.HuHoBot;
import com.alibaba.fastjson2.JSONObject;

public class ChatPoster {
    public static void postChat(String playerName, String text) {

        HuHoBot plugin = HuHoBot.getInstance();
        String prefix = plugin.getConfigManager().getString("chatFormat.post_prefix", "!");
        boolean isPostChat = plugin.getConfigManager().getBoolean("chatFormat.post_chat", true);
        String format = plugin.getConfigManager().getString("chatFormat.from_game", "[{name}] {msg}");
        String serverId = plugin.getConfigManager().getString("serverId", "default");

        if (text.startsWith(prefix) && isPostChat) {
            JSONObject body = new JSONObject();
            body.put("serverId", serverId);
            String formatted = format.replace("{name}", playerName)
                    .replace("{msg}", text.substring(prefix.length()));
            body.put("msg", formatted);

            HuHoBot.getClientManager().getClient().sendMessage("chat", body);
        }


    }
}
