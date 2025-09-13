package cn.huohuas001.huHoBot.NetEvent;


import cn.huohuas001.huHoBot.HuHoBot;
import cn.huohuas001.huHoBot.Tools.BotQueryWhiteList;
import cn.huohuas001.huHoBot.Tools.SetController;
import com.alibaba.fastjson2.JSONObject;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Whitelist;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryAllowList extends EventRunner {

    /**
     * 获取白名单玩家名字集合
     */
    public static Set<String> getWhitelistNames(MinecraftServer server) {
        Whitelist whitelist = server.getPlayerManager().getWhitelist();
        // getNames() 返回 String[]
        return new HashSet<>(Arrays.asList(whitelist.getNames()));
    }


    public boolean CallEvent() {
        Set<String> whiteList = getWhitelistNames(HuHoBot.getInstance().getServer());

        // 根据参数类型创建事件
        BotQueryWhiteList event = createEvent();

        // 保留原有处理流程
        JSONObject rBody = new JSONObject();
        StringBuilder content = new StringBuilder();

        if (body.containsKey("key")) {
            handleKeyword(event, whiteList, content);
        } else if (body.containsKey("page")) {
            handlePage(event, whiteList, content);
        } else {
            handleDefault(event, whiteList, content);
        }

        rBody.put("list", content.toString());
        sendMessage("queryWl", rBody);
        return true;
    }

    private BotQueryWhiteList createEvent() {
        if (body.containsKey("key")) {
            String key = body.getString("key");
            return BotQueryWhiteList.createKeywordEvent(key, packId);
        } else if (body.containsKey("page")) {
            int page = body.getInteger("page");
            return BotQueryWhiteList.createPageEvent(page, packId);
        }
        return BotQueryWhiteList.createPageEvent(1, packId); // 默认第一页
    }

    private void handleKeyword(BotQueryWhiteList event, Set<String> whitelist, StringBuilder sb) {
        String key = event.getKeyWord();
        if (key.length() < 2) {
            sb.append("请使用两个字母及以上的关键词进行查询!");
            return;
        }

        List<String> results = SetController.searchInSet(whitelist, key);
        event.responseList(results, 0); // 通过事件发送响应
    }

    private void handlePage(BotQueryWhiteList event, Set<String> whitelist, StringBuilder sb) {
        int page = event.getPages();
        List<List<String>> pages = SetController.chunkSet(whitelist, 10);

        if (page - 1 >= pages.size()) {
            sb.append("没有该页码\n");
        }

        List<String> currentPage = pages.get(page - 1);
        event.responseList(currentPage, pages.size()); // 通过事件发送响应
    }

    private void handleDefault(BotQueryWhiteList event, Set<String> whitelist, StringBuilder sb) {
        List<List<String>> pages = SetController.chunkSet(whitelist, 10);
        event.responseList(pages.get(0), pages.size()); // 默认发送第一页
    }

    @Override
    public boolean run() {
        //HuHoBot.getScheduler().runTask(this::CallEvent);

        return true;
    }
}
