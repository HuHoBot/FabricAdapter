package cn.huohuas001.huHoBot.Tools;

import cn.huohuas001.huHoBot.HuHoBot;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * 带Getter和Setter的Fabric配置管理器（保留原有配置项操作）
 */
public class ConfigManager {
    private final HuHoBot mod;
    private final Logger logger;
    private final File configFile;
    private final File configDir;
    private Map<String, Object> config;

    public ConfigManager(HuHoBot mod) {
        this.mod = mod;
        this.logger = mod.LOGGER;

        // 配置目录：服务器根目录/config/huhobot
        this.configDir = new File(mod.getServer().getRunDirectory().toString()+ "config/huhobot");

        if (!configDir.exists() && !configDir.mkdirs()) {
            logger.error("创建配置目录失败！路径："+configDir.getAbsolutePath());
        }

        // 配置文件路径
        this.configFile = new File(configDir, "config.yml");

        // 加载默认配置
        loadDefaultConfig();

        // 读取配置到内存
        reloadConfig();
    }

    /**
     * 加载默认配置（从resources复制）
     */
    private void loadDefaultConfig() {
        if (configFile.exists()) {
            return;
        }

        try (InputStream defaultConfigStream = mod.getClass().getResourceAsStream("/config.yml")) {
            if (defaultConfigStream == null) {
                logger.error("默认配置文件不存在！请在resources目录下创建config.yml");
                return;
            }

            Files.copy(defaultConfigStream, configFile.toPath());
            logger.info("生成默认配置文件："+configFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("生成默认配置失败"+e);
        }
    }

    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        if (!configFile.exists()) {
            logger.warn("配置文件不存在，重新生成...");
            loadDefaultConfig();
        }

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        try (FileInputStream input = new FileInputStream(configFile)) {
            config = yaml.load(input);
            if (config == null) {
                config = new HashMap<>();
            }
            logger.info("配置加载完成");
        } catch (IOException e) {
            logger.error("加载配置失败"+e);
            config = new HashMap<>();
        }
    }

    /**
     * 保存配置
     */
    public void saveConfig() {
        if (config == null) {
            logger.warn("无配置数据可保存");
            return;
        }

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(configFile)) {
            yaml.dump(config, writer);
            logger.info("配置已保存");
        } catch (IOException e) {
            logger.error("保存配置失败"+e);
        }
    }

    // ------------------------------ 通用配置读写工具方法 ------------------------------
    @SuppressWarnings("unchecked")
    private Object getConfigValueByPath(String path) {
        if (config == null || path == null || path.isEmpty()) {
            return null;
        }

        String[] segments = path.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (!current.containsKey(segment)) {
                return null;
            }

            Object value = current.get(segment);
            if (i < segments.length - 1) {
                if (!(value instanceof Map)) {
                    return null;
                }
                current = (Map<String, Object>) value;
            } else {
                return value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void setConfigValueByPath(String path, Object value) {
        if (config == null) {
            config = new HashMap<>();
        }

        String[] segments = path.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];
            if (!current.containsKey(segment) || !(current.get(segment) instanceof Map)) {
                current.put(segment, new HashMap<String, Object>());
            }
            current = (Map<String, Object>) current.get(segment);
        }

        current.put(segments[segments.length - 1], value);
    }

    // ------------------------------ 原有配置项的Getter和Setter ------------------------------
    // 1. hashKey相关
    public String getHashKey() {
        return getString("hashKey", "");
    }

    public void setHashKey(String hashKey) {
        setConfigValueByPath("hashKey", hashKey);
        saveConfig(); // 立即保存
    }

    public boolean isHashKeyValue() {
        String hashKey = getHashKey();
        return hashKey != null && !hashKey.isEmpty();
    }

    // 2. serverId相关
    public String getServerId() {
        return getString("serverId", "");
    }

    public void setServerId(String serverId) {
        setConfigValueByPath("serverId", serverId);
        saveConfig(); // 立即保存
    }

    // 3. 聊天格式相关（补充原有逻辑中可能用到的配置）
    public String getChatFormatFromGame() {
        return getString("chatFormat.from_game", "[游戏] %player%: %message%");
    }

    public String getChatFormatFromGroup() {
        return getString("chatFormat.from_group", "[群组] %sender%: %message%");
    }

    public boolean isPostChatEnabled() {
        return getBoolean("chatFormat.post_chat", true);
    }

    public String getChatPostPrefix() {
        return getString("chatFormat.post_prefix", "[HuHoBot]");
    }

    // 4. 白名单命令相关
    public String getWhiteListAddCommand() {
        return getString("whiteList.add", "whitelist add %player%");
    }

    public String getWhiteListDelCommand() {
        return getString("whiteList.del", "whitelist remove %player%");
    }

    //5. 服务器名字
    public String getServerName() {
        return getString("serverName", "Fabric");
    }

    // 6.自定义命令
    /**
     * 读取 customCommand 配置（List<Map> 结构）
     * @return 自定义命令列表（每个元素是包含 key/command/permission 的 Map）
     */
    public List<Map<String, Object>> getCustomCommands() {
        // 1. 先读取 customCommand 节点（实际是 List 类型）
        Object customCommandObj = getConfigValueByPath("customCommand");

        // 2. 类型判断：若不是 List，返回空列表（避免空指针）
        if (!(customCommandObj instanceof List)) {
            logger.warn("customCommand 配置格式错误，应为列表结构！");
            return new ArrayList<>();
        }

        // 3. 强制转换为 List，并过滤无效元素（确保每个元素是 Map）
        List<?> rawList = (List<?>) customCommandObj;
        List<Map<String, Object>> customCommands = new ArrayList<>();

        for (Object item : rawList) {
            if (item instanceof Map) {
                // 转换为 Map<String, Object> 并添加到结果中
                @SuppressWarnings("unchecked")
                Map<String, Object> commandMap = (Map<String, Object>) item;
                customCommands.add(commandMap);
            } else {
                logger.warn("customCommand 中存在无效配置项："+item+"（应为键值对结构）");
            }
        }

        return customCommands;
    }

    // ------------------------------ 通用类型Get方法（供扩展） ------------------------------
    public String getString(String path, String defaultValue) {
        Object value = getConfigValueByPath(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    public int getInt(String path, int defaultValue) {
        Object value = getConfigValueByPath(path);
        return value instanceof Integer ? (Integer) value : defaultValue;
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        Object value = getConfigValueByPath(path);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(String path) {
        Object value = getConfigValueByPath(path);
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }

    // ------------------------------ Getter ------------------------------
    public File getConfigFile() {
        return configFile;
    }

    public Map<String, Object> getRawConfig() {
        return new HashMap<>(config);
    }
}
