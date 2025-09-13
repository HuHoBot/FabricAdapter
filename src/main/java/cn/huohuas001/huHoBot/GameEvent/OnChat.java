package cn.huohuas001.huHoBot.GameEvent;

import cn.huohuas001.huHoBot.HuHoBot;
import com.alibaba.fastjson2.JSONObject;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnChat {
    public static void register() {
        // 监听玩家发送的聊天消息
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (sender instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = sender;

                String playerName = player.getGameProfile().getName();
                String text = message.toString(); // 聊天内容

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

        });
    }
}
