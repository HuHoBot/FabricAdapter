package cn.huohuas001.huHoBot.NetEvent;

import cn.huohuas001.huHoBot.HuHoBot;

public class Chat extends EventRunner {
    @Override
    public boolean run() {
        String nick = body.getString("nick");
        String msg = body.getString("msg");
        boolean isPostChat = getConfigManager().isPostChatEnabled();
        String message = getConfigManager().getChatFormatFromGame().replace("{nick}", nick).replace("{msg}", msg);
        if (isPostChat) {
            HuHoBot.broadcastMessage(message);
        }
        return true;
    }
}
