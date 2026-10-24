package com.nightbeam.remnants.mobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nightbeam.remnants.mobs.content.Trait;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/** Draws nameplates for Minecraft 1.21.1 (immediate rendering from EntityRenderer#render). */
public final class Plates {
    private static final int BORDER = 0xD0120E0B;
    private static final int EMPTY = 0xA0281A16;
    private static final int TRAIL = 0xE0E8D8A8;
    private static final int WARD = 0xFFF2D46B;

    private Plates() {
    }

    public static void render(@Nullable Plate plate, PoseStack pose, MultiBufferSource buffers, Quaternionf camera, boolean nameTagShown) {
        if (plate == null) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int light = LightTexture.FULL_BRIGHT;
        int alpha = Math.round(plate.alpha * 255);
        pose.pushPose();
        double lift = nameTagShown ? 0.3 : 0.0;
        pose.translate(plate.attachment.x, plate.attachment.y + 0.5 + lift, plate.attachment.z);
        pose.mulPose(camera);
        float scale = 0.025F * plate.scale;
        pose.scale(scale, -scale, scale);

        int titleWidth = font.width(plate.title);
        Matrix4f matrix = pose.last().pose();
        font.drawInBatch8xOutline(plate.title.getVisualOrderText(), -titleWidth / 2.0F, -13, Colors.color(alpha, 0xFFFFFF),
                Colors.color(Math.round(alpha * 0.85F), 0x000000), matrix, buffers, light);

        float half = Plate.BAR_WIDTH / 2.0F;
        float x0 = -half;
        float x1 = half;
        float y0 = -2;
        float y1 = 3;
        int border = fade(BORDER, plate.alpha);
        VertexConsumer quads = buffers.getBuffer(RenderType.textBackground());
        rect(quads, matrix, x0 - 1, y0, x1 + 1, y0 + 1, plate.ward ? fade(WARD, plate.alpha) : border, light);
        rect(quads, matrix, x0 - 1, y1, x1 + 1, y1 + 1, border, light);
        rect(quads, matrix, x0 - 1, y0 + 1, x0, y1, border, light);
        rect(quads, matrix, x1, y0 + 1, x1 + 1, y1, border, light);
        float fill = x0 + Plate.BAR_WIDTH * plate.health;
        float trail = x0 + Plate.BAR_WIDTH * Math.max(plate.health, plate.trail);
        if (fill > x0) {
            rect(quads, matrix, x0, y0 + 1, fill, y0 + 2, fade(Colors.lerp(0.35F, plate.barColor, 0xFFFFFFFF), plate.alpha), light);
            rect(quads, matrix, x0, y0 + 2, fill, y1, fade(plate.barColor, plate.alpha), light);
        }
        if (trail > fill) {
            rect(quads, matrix, fill, y0 + 1, trail, y1, fade(TRAIL, plate.alpha), light);
        }
        if (x1 > trail) {
            rect(quads, matrix, trail, y0 + 1, x1, y1, fade(EMPTY, plate.alpha), light);
        }

        if (plate.showHealth) {
            pose.pushPose();
            pose.translate(x1 + 3, y0 - 0.5F, 0);
            pose.scale(0.62F, 0.62F, 0.62F);
            font.drawInBatch8xOutline(Component.literal(plate.healthText).getVisualOrderText(), 0, 0, Colors.color(alpha, 0xE6E1D3),
                    Colors.color(Math.round(alpha * 0.8F), 0x000000), pose.last().pose(), buffers, light);
            pose.popPose();
        }

        if (!plate.traits.isEmpty()) {
            MutableComponent line = Component.empty();
            boolean first = true;
            for (Trait trait : plate.traits) {
                if (!first) {
                    line.append(Component.literal(plate.traitNames ? "  " : " "));
                }
                first = false;
                line.append(Glyphs.trait(trait));
                if (plate.traitNames) {
                    line.append(" ").append(Component.translatable(trait.translationKey()).withStyle(style -> style.withColor(trait.color())));
                }
            }
            pose.pushPose();
            pose.translate(0, y1 + 3, 0);
            pose.scale(0.75F, 0.75F, 0.75F);
            int width = font.width(line);
            font.drawInBatch8xOutline(line.getVisualOrderText(), -width / 2.0F, 0, Colors.color(alpha, 0xFFFFFF),
                    Colors.color(Math.round(alpha * 0.8F), 0x000000), pose.last().pose(), buffers, light);
            pose.popPose();
        }
        pose.popPose();
    }

    private static void rect(VertexConsumer buffer, Matrix4f matrix, float x0, float y0, float x1, float y1, int color, int light) {
        buffer.addVertex(matrix, x0, y1, -0.01F).setColor(color).setLight(light);
        buffer.addVertex(matrix, x1, y1, -0.01F).setColor(color).setLight(light);
        buffer.addVertex(matrix, x1, y0, -0.01F).setColor(color).setLight(light);
        buffer.addVertex(matrix, x0, y0, -0.01F).setColor(color).setLight(light);
    }

    private static int fade(int color, float alpha) {
        return Colors.color(Math.round(Colors.alpha(color) * alpha), color);
    }
}
