package cn.huohuas001.huHoBot;

import cn.huohuas001.huHoBot.NetEvent.*;
import cn.huohuas001.huHoBot.Tools.*;
import cn.huohuas001.huHoBot.CommandManager;
import com.alibaba.fastjson2.JSONObject;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import cn.huohuas001.huHoBot.GameEvent.OnChat;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HuHoBot implements DedicatedServerModInitializer {
	public static final String MOD_ID = "huhobot";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static HuHoBot instance;
	public static MinecraftServer serverInstance;
	private ConfigManager configManager;
	private CommandManager commandManager;
	private static WebsocketClientManager clientManager; //Websocket客户端
	private final Map<String, EventRunner> eventList = new HashMap<>(); //事件列表
	private CustomCommand customCommand; //自定义命令对象
	public bindRequest bindRequestObj;

	private void onServerStarted(){
		configManager = new ConfigManager(this);

		//检测是否为null
		if (configManager.getServerId().isEmpty()) {
			String uuidString = PackId.getPackID();
			configManager.setServerId(uuidString);
		}

		//初始化customCommand
		customCommand = new CustomCommand(this);
		customCommand.loadCommandsFromConfig();

		//注册OnChat监听器
		OnChat.register();

		// 初始化命令管理器
		commandManager = new CommandManager();
		commandManager.registerCommands(serverInstance.getCommandManager().getDispatcher());

		//初始化NetEvent
		totalRegEvent();

		//发起连接
		clientManager = new WebsocketClientManager();
		clientManager.connectServer();

		LOGGER.info("HuHoBot Loaded. By HuoHuas001");

	}

	@Override
	public void onInitializeServer() {
		instance = this;

		// 注册服务器启动事件
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			serverInstance = server;
			onServerStarted();
			LOGGER.info("Server starting! Bot mod is ready.");
		});
		LOGGER.info("HuHoBot Initialized.");
	}

	/**
	 * 重连HuHoBot服务器
	 *
	 * @return 是否连接成功
	 */
	public boolean reconnect() {
		if (clientManager.isOpen()) {
			return false;
		}
		clientManager.connectServer();
		return true;
	}

	/**
	 * 断连HuHoBot服务器
	 *
	 * @return 是否断连成功
	 */
	public boolean disConnectServer() {
		clientManager.setShouldReconnect(false);
		return clientManager.shutdownClient();
	}

	/**
	 * 重载插件配置文件
	 *
	 * @return 是否重载成功
	 */
	public boolean reloadBotConfig() {
		configManager.reloadConfig();
		customCommand.loadCommandsFromConfig();
		return true;
	}

	/**
	 * 在控制台输出绑定ID
	 */
	public void sendBindMessage() {
		if (!configManager.isHashKeyValue()) {
			String serverId = configManager.getServerId();
			String message = "服务器尚未在机器人进行绑定，请在群内输入\"/绑定 " + serverId + "\"";
			LOGGER.warn(message);
		}
	}

	/**
	 * 运行命令
	 *
	 * @param command 命令
	 */
	public void runCommand(String command) {
		String newPackId = PackId.getPackID();
		runCommand(command, newPackId);
	}

	/**
	 * 运行命令
	 *
	 * @param command 命令
	 * @param packId  消息包ID
	 */
	public void runCommand(String command, String packId) {
		serverInstance.execute(() -> {
			commandManager.executeCommand(command,
					(CommandManager.CommandResult result) -> {
						String sendCmdMsg = result.getOutput();
						clientManager.getClient().respone("已执行.\n" + sendCmdMsg, "success", packId);
			});
		});

	}

	/**
	 * 注册Websocket事件
	 *
	 * @param eventName 事件名称
	 * @param event     事件对象
	 */
	private void registerEvent(String eventName, EventRunner event) {
		eventList.put(eventName, event);
	}

	/**
	 * 统一事件注册
	 */
	private void totalRegEvent() {
		registerEvent("sendConfig", new SendConfig());
		registerEvent("shaked", new Shaked());
		registerEvent("chat", new Chat());
		registerEvent("add", new AddAllowList());
		registerEvent("delete", new DelAllowList());
		registerEvent("cmd", new RunCommand());
		registerEvent("queryList", new QueryAllowList());
		registerEvent("queryOnline", new QueryOnline());
		registerEvent("shutdown", new ShutDown());
		registerEvent("run", new CustomRun());
		registerEvent("runAdmin", new CustomRunAdmin());
		registerEvent("heart", new Heart());
		bindRequestObj = new bindRequest();
		registerEvent("bindRequest", bindRequestObj);
	}

	/**
	 * 当收到Websocket消息时的回调
	 *
	 * @param data 回调数据
	 */
	public void onWsMsg(JSONObject data) {
		JSONObject header = data.getJSONObject("header");
		JSONObject body = data.getJSONObject("body");

		String type = header.getString("type");
		String packId = header.getString("id");

		EventRunner event = eventList.get(type);
		if (event != null) {
			event.EventCall(packId, body);
		} else {
			LOGGER.error("在处理消息是遇到错误: 未知的消息类型");
			LOGGER.error("此错误具有不可容错性!请检查插件是否为最新!");
			LOGGER.info("正在断开连接...");
			clientManager.shutdownClient();
		}
	}

	public static void broadcastMessage(String message) {
		serverInstance.sendMessage(Text.of(message));
	}

	/**
	 * 获取当前Mod的版本号（对应fabric.mod.json中的version字段）
	 * @return Mod版本号（如"1.0.0"），获取失败返回"unknown"
	 */
	public String getModVersion() {
		// 1. 通过FabricLoader获取当前Mod的容器
		Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(MOD_ID);

		// 2. 读取容器中的元数据，提取版本号
		if (modContainer.isPresent()) {
			ModMetadata metadata = modContainer.get().getMetadata();
			return metadata.getVersion().getFriendlyString(); // 友好格式的版本号（如"1.0.0"）
		}

		// 3. 极端情况（如Mod未正确加载）返回默认值
		LOGGER.warn("获取Mod版本失败，返回默认版本标识");
		return "unknown";
	}

	public static HuHoBot getInstance() {
		return instance;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public MinecraftServer getServer() {
		return serverInstance;
	}

	public static WebsocketClientManager getClientManager() {
		return clientManager;
	}

	public Map<String, CommandObject> getCommandMap() {
		return customCommand.getCommandMap();
	}
}