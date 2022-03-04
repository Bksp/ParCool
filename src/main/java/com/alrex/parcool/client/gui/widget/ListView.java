package com.alrex.parcool.client.gui.widget;

import com.alrex.parcool.utilities.ColorUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;

import java.util.List;
import java.util.function.Consumer;

public class ListView extends Widget {
	private final int menuLineHeight = Minecraft.getInstance().font.lineHeight + 1;
	private List<String> items;
	private Consumer<Integer> listener = null;

	public ListView(int x, int y, int width, int height, List<String> items) {
		super(x, y, width, height);
		this.items = items;
	}

	public ListView(List<String> items) {
		super(0, 0, 0, 0);
		this.items = items;
	}

	@Override
	public void render(MatrixStack stack, FontRenderer fontRenderer, int mouseX, int mouseY, float partial) {

		IRenderTypeBuffer.Impl renderTypeBuffer = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
		for (int i = 0; i < items.size(); i++) {
			String item = items.get(i);
			fontRenderer.draw(
					stack,
					item,
					x,
					y + menuLineHeight * i,
					ColorUtil.getColorCodeFromARGB(0, 0, 0, 0)
			);
		}
		renderTypeBuffer.endBatch();
	}

	public void onClick(int type, double mouseX, double mouseY) {
		if (type == 0) {
			MainWindow window = Minecraft.getInstance().getWindow();
			for (int i = 0; i < items.size(); i++) {
				int itemY = this.y + menuLineHeight * i;
				if (itemY < mouseY && mouseY < itemY + menuLineHeight && x < mouseX && mouseX < x + width) {
					if (listener != null) listener.accept(i);
				}
			}
		}
	}

	public void setListener(Consumer<Integer> listener) {
		this.listener = listener;
	}
}
