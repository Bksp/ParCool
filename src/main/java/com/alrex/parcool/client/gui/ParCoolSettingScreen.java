package com.alrex.parcool.client.gui;

import com.alrex.parcool.client.gui.widget.WidgetListView;
import com.alrex.parcool.utilities.ColorUtil;
import com.alrex.parcool.utilities.FontUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alrex.parcool.ParCoolConfig.CONFIG_CLIENT;

public class ParCoolSettingScreen extends Screen {
	private final int width = 400;
	private final int height = 225;
	private final int xOffset = 10;
	private final int yOffset = 10;
	private final ButtonSet[] itemList = new ButtonSet[]{
			new ButtonSet("action.name.catleap", CONFIG_CLIENT.canCatLeap::set, CONFIG_CLIENT.canCatLeap::get),
			new ButtonSet("action.name.crawl", CONFIG_CLIENT.canCrawl::set, CONFIG_CLIENT.canCrawl::get),
			new ButtonSet("action.name.frontflip", CONFIG_CLIENT.canFrontFlip::set, CONFIG_CLIENT.canFrontFlip::get),
			new ButtonSet("action.name.dodge", CONFIG_CLIENT.canDodge::set, CONFIG_CLIENT.canDodge::get),
			new ButtonSet("action.name.fastrun", CONFIG_CLIENT.canFastRunning::set, CONFIG_CLIENT.canFastRunning::get),
			new ButtonSet("action.name.clingtocliff", CONFIG_CLIENT.canClingToCliff::set, CONFIG_CLIENT.canClingToCliff::get),
			new ButtonSet("action.name.roll", CONFIG_CLIENT.canRoll::set, CONFIG_CLIENT.canRoll::get),
			new ButtonSet("action.name.vault", CONFIG_CLIENT.canVault::set, CONFIG_CLIENT.canVault::get),
			new ButtonSet("action.name.walljump", CONFIG_CLIENT.canWallJump::set, CONFIG_CLIENT.canWallJump::get),
			new ButtonSet("action.name.wallslide", CONFIG_CLIENT.canWallSlide::set, CONFIG_CLIENT.canWallJump::get),
			new ButtonSet("hide stamina HUD", CONFIG_CLIENT.hideStaminaHUD::set, CONFIG_CLIENT.hideStaminaHUD::get),
			new ButtonSet("use light Stamina HUD", CONFIG_CLIENT.useLightHUD::set, CONFIG_CLIENT.useLightHUD::get),
			new ButtonSet("auto-turning when WallJump", CONFIG_CLIENT.autoTurningWallJump::set, CONFIG_CLIENT.autoTurningWallJump::get),
			new ButtonSet("disable WallJump toward walls", CONFIG_CLIENT.disableWallJumpTowardWall::set, CONFIG_CLIENT.disableWallJumpTowardWall::get),
			new ButtonSet("disable a camera rotation of Rolling", CONFIG_CLIENT.disableCameraRolling::set, CONFIG_CLIENT.disableCameraRolling::get),
			new ButtonSet("disable a camera rotation of Dodge", CONFIG_CLIENT.disableCameraDodge::set, CONFIG_CLIENT.disableCameraDodge::get),
			new ButtonSet("disable a Kong Vault Animation", CONFIG_CLIENT.disableKongVault::set, CONFIG_CLIENT.disableKongVault::get),
			new ButtonSet("disable double-tapping for dodge", CONFIG_CLIENT.disableDoubleTappingForDodge::set, CONFIG_CLIENT.disableDoubleTappingForDodge::get),
			new ButtonSet("disable flipping of dodge", CONFIG_CLIENT.disableFlipping::set, CONFIG_CLIENT.disableFlipping::get),
			new ButtonSet("ParCool is active", CONFIG_CLIENT.parCoolActivation::set, CONFIG_CLIENT.parCoolActivation::get)
	};
	private final WidgetListView<Checkbox> buttons = new WidgetListView<Checkbox>(
			0, 0, 0, 0,
			Arrays.stream(itemList)
					.map((ButtonSet item) ->
							new Checkbox
									(
											0, 0, 0, 0,
											new TranslatableComponent(item.name),
											item.getter.getAsBoolean()
									))
					.collect(Collectors.toList()),
			Minecraft.getInstance().font.lineHeight + 11
	);

	public ParCoolSettingScreen(Component titleIn) {
		super(titleIn);
	}

	//render?
	@Override
	public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);

		Window window = Minecraft.getInstance().getWindow();
		buttons.setX((window.getGuiScaledWidth() - width) + xOffset);
		buttons.setY((window.getGuiScaledHeight() - height) + yOffset);
		buttons.setWidth(width - (xOffset * 2));
		buttons.setHeight(height - (yOffset * 2));

		renderBackground(p_230430_1_, ColorUtil.getColorCodeFromARGB(0x77, 0x66, 0x66, 0xCC));
		buttons.render(p_230430_1_, Minecraft.getInstance().font, p_230430_2_, p_230430_3_, p_230430_4_);
		FontUtil.drawCenteredText(p_230430_1_, "ParCool Settings", window.getGuiScaledWidth() / 2, yOffset, 0x8888FF);
	}

	//renderBackground?
	@Override
	public void renderBackground(PoseStack p_230446_1_) {
		super.renderBackground(p_230446_1_);
	}

	//renderBackground?
	@Override
	public void renderBackground(PoseStack p_238651_1_, int p_238651_2_) {
		super.renderBackground(p_238651_1_, p_238651_2_);
	}

	//mouseScrolled?
	@Override
	public boolean mouseScrolled(double x, double y, double value) {
		if (buttons.contains(x, y)) {
			buttons.scroll((int) -value);
		}
		return true;
	}

	private static class ButtonSet {
		final String name;
		final Consumer<Boolean> setter;
		final BooleanSupplier getter;

		ButtonSet(String name, Consumer<Boolean> setter, BooleanSupplier getter) {
			this.name = name;
			this.getter = getter;
			this.setter = setter;
		}
	}

	//mouseClicked?
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {//type:1->right 0->left
		if (buttons.contains(mouseX, mouseY)) {
			Tuple<Integer, Checkbox> item = buttons.clicked(mouseX, mouseY, type);
			if (item == null) return false;
			if (item.getA() < 0 || itemList.length <= item.getA()) return false;
			ButtonSet selected = itemList[item.getA()];

			item.getB().onPress();
			selected.setter.accept(item.getB().selected());

			Player player = Minecraft.getInstance().player;
			if (player != null) player.playSound(SoundEvents.UI_BUTTON_CLICK, 1.0f, 1.0f);
		}
		return false;
	}
}
