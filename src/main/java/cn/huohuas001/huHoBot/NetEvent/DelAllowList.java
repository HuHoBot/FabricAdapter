package cn.huohuas001.huHoBot.NetEvent;

public class DelAllowList extends EventRunner {
    @Override
    public boolean run() {
        String XboxId = body.getString("xboxid");
        String command = getConfigManager().getWhiteListDelCommand().replace("{name}", XboxId);
        runCommand(command);
        String name = getConfigManager().getServerName();
        respone(name + "已接受删除名为" + XboxId + "的白名单请求", "success");
        return true;
    }
}
