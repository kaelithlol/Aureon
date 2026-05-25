package com.kaelith.aureon.mixins;

import com.kaelith.aureon.events.EventBus;
import com.kaelith.aureon.events.core.PacketEvent;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinConnection {
    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void Aureon$onReceivePacket(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if (EventBus.INSTANCE.onPacketReceived(packet)) ci.cancel();
    }

    @Inject(method = "channelRead0*", at = @At("TAIL"))
    private void Aureon$onReceivePacketPost(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        EventBus.INSTANCE.post(new PacketEvent.ReceivedPost(packet));
    }

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void Aureon$onPacketSend(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        if (EventBus.INSTANCE.post(new PacketEvent.Sent(packet))) ci.cancel();
    }
}
