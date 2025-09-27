package cn.huohuas001.huHoBot;

import cn.huohuas001.huHoBot.NetEvent.bindRequest;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import cn.huohuas001.huHoBot.Tools.TextBuilder;
import net.minecraft.text.Text;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.brigadier.arguments.StringArgumentType;
import org.slf4j.Logger;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandManager {
    private final List<Consumer<CommandResult>> commandCallbacks = new ArrayList<>();
    private Logger logger = HuHoBot.LOGGER;

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("huhobot")
                .requires(source -> source.getEntity() == null) // 只允许控制台执行
                .then(literal("reload")
                        .executes(this::onReload)
                )
                .then(literal("reconnect")
                        .executes(this::onReconnect)
                )
                .then(literal("disconnect")
                        .executes(this::onDisconnect)
                )
                .then(literal("bind")
                        .then(argument("bindCode", StringArgumentType.word())
                                .executes(this::onBind)
                        )
                )
                .then(literal("help")
                        .executes(this::onHelp)
                )
        );
    }

    private int onReload(CommandContext<ServerCommandSource> context) {
        HuHoBot plugin = HuHoBot.getInstance();
        plugin.reloadBotConfig();
        logger.info("§b重载机器人配置文件成功.");

        triggerCallbacks(new CommandResult("huhobot reload", "", getName(context), true));
        return 1;
    }

    private int onReconnect(CommandContext<ServerCommandSource> context) {
        HuHoBot plugin = HuHoBot.getInstance();
        boolean success = plugin.reconnect();
        if (success) {
            logger.info("§6重连机器人成功.");
        } else {
            logger.warn("§c重连机器人失败：已在连接状态.");
        }

        triggerCallbacks(new CommandResult("huhobot reconnect", "", getName(context), success));
        return 1;
    }

    private int onDisconnect(CommandContext<ServerCommandSource> context) {
        HuHoBot plugin = HuHoBot.getInstance();
        boolean success = plugin.disConnectServer();
        if (success) {
            logger.warn("§6已断开机器人连接.");
        }

        triggerCallbacks(new CommandResult("huhobot disconnect", "", getName(context), success));
        return 1;
    }

    private int onBind(CommandContext<ServerCommandSource> context) {
        String code = StringArgumentType.getString(context, "bindCode");
        bindRequest obj = HuHoBot.getInstance().bindRequestObj;
        boolean success = obj.confirmBind(code);

        if (success) {
            logger.info("§6已向服务器发送确认绑定请求，请等待服务端下发配置文件.");
        } else {
            logger.error("§c绑定码错误，请重新输入.");
        }

        triggerCallbacks(new CommandResult("huhobot bind", code, getName(context), success));
        return 1;
    }

    private int onHelp(CommandContext<ServerCommandSource> context) {
        logger.info("§bHuHoBot 操作相关命令");
        logger.info("§6> §7/huhobot reload - 重载配置文件");
        logger.info("§6> §7/huhobot reconnect - 重新连接服务器");
        logger.info("§6> §7/huhobot disconnect - 断开服务器连接");
        logger.info("§6> §7/huhobot bind <bindCode:string> - 确认绑定");
        return 1;
    }

    // 获取执行者名字（玩家名或控制台）
    private String getName(CommandContext<ServerCommandSource> context) {
        try {
            return context.getSource().getPlayer().getGameProfile().getName();
        } catch (Exception e) {
            return "System";
        }
    }

    // 外部执行命令并提供回调
    public void executeCommand(String command, Consumer<CommandResult> callback) {
        commandCallbacks.add(callback);
        MinecraftServer server = HuHoBot.getInstance().getServer();

        server.execute(() -> {
            try {

                ServerCommandSource source = server.getCommandSource();
                //#if MC>=11900
                server.getCommandManager().executeWithPrefix(source, command);
                //#else
                //$$ server.getCommandManager().execute(source, command);
                //#endif
                callback.accept(new CommandResult(command, "Command executed", "System", true));
            }
            catch (Exception e) {
                callback.accept(new CommandResult(command, "Error: " + e.getMessage(), "System", false));
            }
            finally { commandCallbacks.remove(callback); }
        });
    }


    private void triggerCallbacks(CommandResult result) {
        for (Consumer<CommandResult> callback : new ArrayList<>(commandCallbacks)) {
            callback.accept(result);
        }
    }

    // 命令结果类
    // 命令结果类
    public class CommandResult {
        private final String command;
        private final String output;
        private final String sender;
        private final boolean success;

        public CommandResult(String command, String output, String sender, boolean success) {
            this.command = command;
            this.output = output;
            this.sender = sender;
            this.success = success;
        }

        // getter方法
        public String getCommand() { return command; }
        public String getOutput() { return output; }
        public String getSender() { return sender; }
        public boolean isSuccess() { return success; }

        // equals, hashCode, toString方法（可选）
    }
}
