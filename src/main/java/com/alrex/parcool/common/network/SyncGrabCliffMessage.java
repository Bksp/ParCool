package com.alrex.parcool.common.network;

import com.alrex.parcool.ParCool;
import com.alrex.parcool.common.capability.IGrabCliff;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class SyncGrabCliffMessage implements IMessage {
	private boolean isGrabbing = false;
	private UUID playerID = null;

	public void toBytes(ByteBuf packet) {
		packet.writeBoolean(this.isGrabbing);
		packet.writeLong(this.playerID.getMostSignificantBits());
		packet.writeLong(this.playerID.getLeastSignificantBits());
	}

	public void fromBytes(ByteBuf packet) {
		this.isGrabbing = packet.readBoolean();
		this.playerID = new UUID(packet.readLong(), packet.readLong());
	}

	@SideOnly(Side.SERVER)
	public static SyncGrabCliffMessage handleServer(SyncGrabCliffMessage message, MessageContext context) {
		EntityPlayerMP player = context.getServerHandler().player;

		player.getServerWorld().func_152344_a(() -> {
			ParCool.CHANNEL_INSTANCE.sendToAll(message);

			IGrabCliff grabCliff = IGrabCliff.get(player);
			if (grabCliff == null) return;

			grabCliff.setGrabbing(message.isGrabbing);
		});
		return null;
	}

	@SideOnly(Side.CLIENT)
	public static SyncGrabCliffMessage handleClient(SyncGrabCliffMessage message, MessageContext context) {
		Minecraft.getInstance().func_152344_a(() -> {
			EntityPlayer player;

			if (context.side == Side.CLIENT) {
				World world = Minecraft.getInstance().world;
				if (world == null) return;
				player = world.func_152378_a(message.playerID);
				if (player == null || player.isUser()) return;
			} else {
				player = context.getServerHandler().player;
				ParCool.CHANNEL_INSTANCE.sendToAll(message);
				if (player == null) return;
			}

			IGrabCliff grabCliff = IGrabCliff.get(player);
			if (grabCliff == null) return;

			grabCliff.setGrabbing(message.isGrabbing);
		});
		return null;
	}

	@SideOnly(Side.CLIENT)
	public static void sync(EntityPlayer player) {
		IGrabCliff grabCliff = IGrabCliff.get(player);
		if (grabCliff == null) return;

		SyncGrabCliffMessage message = new SyncGrabCliffMessage();
		message.isGrabbing = grabCliff.isGrabbing();
		message.playerID = player.getUniqueID();

		ParCool.CHANNEL_INSTANCE.sendToServer(message);
	}

}

