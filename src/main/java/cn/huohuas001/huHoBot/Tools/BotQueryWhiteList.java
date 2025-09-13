package cn.huohuas001.huHoBot.Tools;

import cn.huohuas001.huHoBot.HuHoBot;
import cn.huohuas001.huHoBot.WsClient;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;

public class BotQueryWhiteList{

    private final String key;
    private final int pages;
    private final String packId;
    private final QueryMode param;
    /**
     * 取消状态
     */
    private boolean isCancelled = false;
    public BotQueryWhiteList(
            String keyWord,
            int pages,
            QueryMode param,
            String packId) {

        this.key = keyWord;
        this.pages = pages;
        this.param = param;
        this.packId = packId;
    }

    // 新增便捷构造方法
    public static BotQueryWhiteList createKeywordEvent(String keyword, String packId) {
        return new BotQueryWhiteList(keyword, 0, QueryMode.KEYWORD_SEARCH, packId);
    }

    public static BotQueryWhiteList createPageEvent(int page, String packId) {
        return new BotQueryWhiteList(null, page, QueryMode.PAGE_QUERY, packId);
    }

    public String getKeyWord() {
        return this.key;
    }

    public int getPages() {
        return this.pages;
    }

    public QueryMode getQueryMode() {
        return this.param;
    }

    private String formatResponse(List<String> results, int totalPages) {
        StringBuilder sb = new StringBuilder();

        switch (param) {
            case KEYWORD_SEARCH:
                sb.append("查询白名单关键词:").append(key).append("结果如下:\n");
                if (results.isEmpty()) {
                    sb.append("无结果\n");
                } else {
                    results.forEach(name -> sb.append(name).append("\n"));
                    sb.append("共有").append(results.size()).append("个结果");
                }
                break;

            case PAGE_QUERY:
                sb.append("服内白名单如下:\n");
                if (results.isEmpty()) {
                    sb.append("无结果\n");
                } else {
                    results.forEach(name -> sb.append(name).append("\n"));
                }
                sb.append("共有").append(totalPages).append("页，当前为第")
                        .append(pages).append("页\n请使用/查白名单 {页码}来翻页");
                break;
        }

        return sb.toString();
    }

    // 新增响应方法重载
    public void responseList(List<String> results, int totalPages) {
        String formatted = formatResponse(results, totalPages);
        responeString(formatted);
    }

    public void responeString(String whiteList) {
        WsClient client = HuHoBot.getClientManager().getClient();
        JSONObject rBody = new JSONObject();
        rBody.put("list", whiteList);
        client.sendMessage("queryWl", rBody, packId);
    }

    public enum QueryMode {
        KEYWORD_SEARCH,
        PAGE_QUERY
    }
}
