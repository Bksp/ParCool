package com.alrex.parcool.common.capability.provider;

import com.alrex.parcool.ParCool;
import com.alrex.parcool.common.capability.IDodge;
import com.alrex.parcool.common.capability.capabilities.Capabilities;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DodgeProvider implements ICapabilityProvider {
	public static final ResourceLocation CAPABILITY_LOCATION = new ResourceLocation(ParCool.MOD_ID, "capability.parcool.dodge");

	IDodge instance = Capabilities.DODGE_CAPABILITY.getDefaultInstance();

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return Capabilities.DODGE_CAPABILITY == capability;
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		return Capabilities.DODGE_CAPABILITY == capability ? Capabilities.DODGE_CAPABILITY.cast(instance) : null;
	}
}
