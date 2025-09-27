package cn.huohuas001.huHoBot.GameEvent;

import cn.huohuas001.huHoBot.HuHoBot;
import cn.huohuas001.huHoBot.Tools.ChatPoster;
import com.alibaba.fastjson2.JSONObject;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;

//#if MC>=11900
public class ChatListenerApiLegacy {
    public static void register() {
        // 监听玩家发送的聊天消息
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (sender instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = sender;

                String playerName = player.getGameProfile().getName();
                String text = message.toString(); // 聊天内容

                ChatPoster.postChat(playerName, text);
            }
        });
    }
}
//#endif