package cn.huohuas001.huHoBot.GameEvent.Mixin;

import cn.huohuas001.huHoBot.Tools.ChatPoster;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC<11800
//$$ @Mixin(ServerPlayNetworkHandler.class)
//$$ public class ChatListenerMixin {
    //$$ @Inject(method = "onGameMessage", at = @At("HEAD"))
    //$$ private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
    //$$    String msg = packet.getChatMessage();
    //$$    ServerPlayNetworkHandler self = (ServerPlayNetworkHandler)(Object)this;
    //$$    ServerPlayerEntity player = self.player;

    //$$    ChatPoster.postChat(player.getName().getString(),msg);
    //$$ }
//$$ }
//#elseif MC<11900
//$$ @Mixin(ServerPlayNetworkHandler.class)
//$$ public class ChatListenerMixin {
//$$ @Inject(method = "onChatMessage", at = @At("HEAD"))
//$$ private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
//$$    String msg = packet.getChatMessage();
//$$    ServerPlayNetworkHandler self = (ServerPlayNetworkHandler)(Object)this;
//$$    ServerPlayerEntity player = self.player;

//$$    ChatPoster.postChat(player.getName().getString(),msg);
//$$ }
//$$ }
//#endif


