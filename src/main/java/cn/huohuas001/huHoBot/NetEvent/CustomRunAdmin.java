package cn.huohuas001.huHoBot.NetEvent;

import cn.huohuas001.huHoBot.HuHoBot;
import cn.huohuas001.huHoBot.Tools.CommandObject;

import java.util.List;
import java.util.Map;

public class CustomRunAdmin extends EventRunner {

    private void CallEvent() {
        String keyWord = body.getString("key");
        List<String> param = body.getList("runParams", String.class);

        Map<String, CommandObject> commandMap = HuHoBot.getInstance().getCommandMap();
        // 测试查找功能
        CommandObject result = commandMap.get(keyWord);
        if (result != null) {
            String command = result.getCommand();
            for (int i = 0; i < param.size(); i++) {
                int replaceNum = i + 1;
                command = command.replace("&" + replaceNum, param.get(i));
            }
            runCommand(command);
        }else{
            respone("未找到关键词" + keyWord + "对应的自定义事件", "error");
        }

    }

    @Override
    public boolean run() {
        HuHoBot.serverInstance.execute(this::CallEvent);
        return false;
    }
}
