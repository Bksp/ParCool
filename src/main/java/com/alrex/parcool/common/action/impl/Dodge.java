package com.alrex.parcool.common.action.impl;

import com.alrex.parcool.ParCoolConfig;
import com.alrex.parcool.client.animation.impl.DodgeAnimator;
import com.alrex.parcool.client.input.KeyBindings;
import com.alrex.parcool.client.input.KeyRecorder;
import com.alrex.parcool.common.action.Action;
import com.alrex.parcool.common.capability.Animation;
import com.alrex.parcool.common.capability.Parkourability;
import com.alrex.parcool.common.capability.Stamina;
import com.alrex.parcool.common.network.SyncDodgeMessage;
import com.alrex.parcool.utilities.BufferUtil;
import com.alrex.parcool.utilities.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;

import java.nio.ByteBuffer;


public class Dodge extends Action {
	public enum DodgeDirections {
		Front, Back, Left, Right
	}

	private DodgeDirections dodgeDirection = null;
	private int coolTime = 0;
	private int dodgingTick = 0;
	private boolean needPitchReset = false;
	private int damageCoolTime = 0;
	private boolean avoided = false;
	private boolean dodging = false;

	public boolean isAvoided() {
		return avoided;
	}

	public boolean isDodging() {
		return dodging;
	}

	@Override
	public void onTick(PlayerEntity player, Parkourability parkourability, Stamina stamina) {
		if (coolTime > 0) coolTime--;
		if (damageCoolTime > 0) damageCoolTime--;

		if (dodging) {
			dodgingTick++;
		} else {
			dodgingTick = 0;
		}
	}

	@OnlyIn(Dist.CLIENT)
	private boolean canDodge(PlayerEntity player, Parkourability parkourability, Stamina stamina) {
		return parkourability.getPermission().canDodge() && coolTime <= 0 && player.isOnGround() && !player.isShiftKeyDown() && !stamina.isExhausted() && (
				KeyRecorder.keyBack.isDoubleTapped() ||
						KeyRecorder.keyLeft.isDoubleTapped() ||
						KeyRecorder.keyRight.isDoubleTapped() ||
						(ParCoolConfig.CONFIG_CLIENT.canFrontFlip.get() && KeyRecorder.keyForward.isDoubleTapped()) ||
						(KeyBindings.getKeyDodge().isDown() && (
								KeyBindings.getKeyForward().isDown() ||
										KeyBindings.getKeyBack().isDown() ||
										KeyBindings.getKeyLeft().isDown() ||
										KeyBindings.getKeyRight().isDown()
						)
						)
		);
	}

	@OnlyIn(Dist.CLIENT)
	private boolean canContinue(PlayerEntity player, Parkourability parkourability, Stamina stamina) {
		return dodging &&
				!parkourability.getRoll().isRolling() &&
				!parkourability.getClingToCliff().isCling() &&
				!player.isOnGround() &&
				!player.isInWaterOrBubble() &&
				!player.isFallFlying() &&
				!player.abilities.flying &&
				parkourability.getPermission().canClingToCliff();
	}

	@OnlyIn(Dist.CLIENT)
	private DodgeDirections getDirectionFromInput() {
		if (KeyBindings.getKeyBack().isDown()) {
			return DodgeDirections.Back;
		}
		if (KeyBindings.getKeyForward().isDown()) {
			return DodgeDirections.Front;
		}
		if (KeyBindings.getKeyLeft().isDown()) {
			return DodgeDirections.Left;
		} else {
			return DodgeDirections.Right;
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onClientTick(PlayerEntity player, Parkourability parkourability, Stamina stamina) {
		if (player.isLocalPlayer()) {
			if (canContinue(player, parkourability, stamina)) {
				dodging = true;
			} else {
				if (dodging && (dodgeDirection == DodgeDirections.Front || dodgeDirection == DodgeDirections.Back)) {
					if (!ParCoolConfig.CONFIG_CLIENT.disableCameraDodge.get()) {
						needPitchReset = true;
					}
				}
				dodgingTick = 0;
				dodging = false;
				avoided = false;
			}
			if (!dodging && canDodge(player, parkourability, stamina)) {
				dodging = true;
				avoided = false;

				stamina.consume(parkourability.getActionInfo().getStaminaConsumptionDodge(), parkourability.getActionInfo());
				dodgeDirection = getDirectionFromInput();

				Vector3d lookVec = player.getLookAngle();
				lookVec = new Vector3d(lookVec.x(), 0, lookVec.z()).normalize();
				double jump = 0;
				Vector3d dodgeVec = Vector3d.ZERO;
				switch (dodgeDirection) {
					case Front:
						dodgeVec = lookVec;
						jump = 0.5;
						break;
					case Back:
						dodgeVec = lookVec.reverse();
						jump = 0.5;
						break;
					case Right:
						dodgeVec = lookVec.yRot((float) Math.PI / -2);
						jump = 0.3;
						break;
					case Left:
						dodgeVec = lookVec.yRot((float) Math.PI / 2);
						jump = 0.3;
						break;
				}
				coolTime = 10;
				dodgeVec = dodgeVec.scale(0.4);
				EntityUtil.addVelocity(player, new Vector3d(dodgeVec.x(), jump, dodgeVec.z()));
			}
		}
		if (dodging) {
			Animation animation = Animation.get(player);
			if (animation != null) animation.setAnimator(new DodgeAnimator());
		}
	}

	@Override
	public void onRender(TickEvent.RenderTickEvent event, PlayerEntity player, Parkourability parkourability) {
		if (!player.isLocalPlayer() || !Minecraft.getInstance().options.getCameraType().isFirstPerson() || ParCoolConfig.CONFIG_CLIENT.disableCameraDodge.get())
			return;
		if (needPitchReset) {
			player.xRot = 0;
			needPitchReset = false;
		}
		if (!dodging) return;
		if (dodgeDirection == DodgeDirections.Front) {
			player.xRot = (getDodgingTick() + event.renderTickTime) * 30;
		} else if (dodgeDirection == DodgeDirections.Back) {
			player.xRot = (getDodgingTick() + event.renderTickTime) * -24;
		}
	}

	@Override
	public boolean needSynchronization(ByteBuffer savedInstanceState) {
		return dodging != BufferUtil.getBoolean(savedInstanceState)
				|| avoided != BufferUtil.getBoolean(savedInstanceState);
	}

	@Override
	public void sendSynchronization(PlayerEntity player) {
		SyncDodgeMessage.sync(player, this);
	}

	@Override
	public void synchronize(Object message) {
		if (message instanceof SyncDodgeMessage) {
			this.dodging = ((SyncDodgeMessage) message).isDodging();
			this.avoided = ((SyncDodgeMessage) message).isAvoided();
			this.dodgeDirection = ((SyncDodgeMessage) message).getDodgeDirection();
		}
	}

	@Override
	public void saveState(ByteBuffer buffer) {
		BufferUtil.wrap(buffer)
				.putBoolean(dodging)
				.putBoolean(avoided);
	}

	public void avoidDamage(ServerPlayerEntity player) {
		avoided = true;
		damageCoolTime = 10;
		SyncDodgeMessage.broadcast(player, this);
	}

	public int getStaminaConsumptionOfAvoiding(float damage) {
		return Math.round(150 + damage * 30);
	}

	public DodgeDirections getDodgeDirection() {
		return dodgeDirection;
	}

	public int getCoolTime() {
		return coolTime;
	}

	public int getDamageCoolTime() {
		return damageCoolTime;
	}

	public int getDodgingTick() {
		return dodgingTick;
	}
}
