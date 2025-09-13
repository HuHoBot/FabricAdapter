package cn.huohuas001.huHoBot.NetEvent;

public class AddAllowList extends EventRunner {
    @Override
    public boolean run() {
        String XboxId = body.getString("xboxid");
        String command = getConfigManager().getWhiteListAddCommand().replace("{name}", XboxId);
        runCommand(command);
        String name = getConfigManager().getServerName();
        respone(name + "已接受添加名为" + XboxId + "的白名单请求", "success");
        return true;
    }
}
