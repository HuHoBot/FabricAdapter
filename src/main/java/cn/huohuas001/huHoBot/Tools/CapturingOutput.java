package cn.huohuas001.huHoBot.Tools;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class CapturingOutput implements CommandOutput {
    private final List<String> messages = new ArrayList<>();

    @Override
    public void sendMessage(Text message) {
        messages.add(message.getString());
    }

    public List<String> getMessages() {
        return messages;
    }


    @Override
    public boolean shouldReceiveFeedback() {
        return false;
    }

    @Override
    public boolean shouldTrackOutput() {
        return false;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return false;
    }
}

