package tn.mbs.ascendantmobs.procedures;

import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;

public class BackgroundShapeProcedure {
	private static BufferBuilder bufferBuilder = null;
	private static VertexBuffer vertexBuffer = null;
	private static VertexFormat.Mode mode = null;
	private static VertexFormat format = null;

	private static void add(double x, double y, double z, int color) {
		add(x, y, z, 0.0F, 0.0F, color);
	}

	private static void add(double x, double y, double z, float u, float v, int color) {
		if (bufferBuilder == null || !bufferBuilder.building())
			return;
		if (format == DefaultVertexFormat.POSITION_COLOR) {
			bufferBuilder.vertex(x, y, z).color(color).endVertex();
		} else if (format == DefaultVertexFormat.POSITION_TEX_COLOR) {
			bufferBuilder.vertex(x, y, z).uv(u, v).color(color).endVertex();
		}
	}

	private static boolean begin(VertexFormat.Mode mode, VertexFormat format, boolean update) {
		if (BackgroundShapeProcedure.bufferBuilder == null || !BackgroundShapeProcedure.bufferBuilder.building()) {
			if (update)
				clear();
			if (BackgroundShapeProcedure.vertexBuffer == null) {
				if (format == DefaultVertexFormat.POSITION_COLOR) {
					BackgroundShapeProcedure.mode = mode;
					BackgroundShapeProcedure.format = format;
					BackgroundShapeProcedure.bufferBuilder = Tesselator.getInstance().getBuilder();
					BackgroundShapeProcedure.bufferBuilder.begin(mode, DefaultVertexFormat.POSITION_COLOR);
					return true;
				} else if (format == DefaultVertexFormat.POSITION_TEX_COLOR) {
					BackgroundShapeProcedure.mode = mode;
					BackgroundShapeProcedure.format = format;
					BackgroundShapeProcedure.bufferBuilder = Tesselator.getInstance().getBuilder();
					BackgroundShapeProcedure.bufferBuilder.begin(mode, DefaultVertexFormat.POSITION_TEX_COLOR);
					return true;
				}
			}
		}
		return false;
	}

	private static void clear() {
		if (vertexBuffer != null) {
			vertexBuffer.close();
			vertexBuffer = null;
		}
	}

	private static void end() {
		if (bufferBuilder == null || !bufferBuilder.building())
			return;
		if (vertexBuffer != null)
			vertexBuffer.close();
		vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		vertexBuffer.bind();
		vertexBuffer.upload(bufferBuilder.end());
		VertexBuffer.unbind();
	}

	private static VertexBuffer shape() {
		return vertexBuffer;
	}

	public static VertexBuffer execute() {
		return execute(null);
	}

	private static VertexBuffer execute(@Nullable Event event) {
		if (begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, false)) {
			add(0.15, 0.15, 0, 0, 0, 255 << 24 | 255 << 16 | 255 << 8 | 255);
			add(0.15, (-0.15), 0, 0, 1, 255 << 24 | 255 << 16 | 255 << 8 | 255);
			add((-0.15), (-0.15), 0, 1, 1, 255 << 24 | 255 << 16 | 255 << 8 | 255);
			add((-0.15), 0.15, 0, 1, 0, 255 << 24 | 255 << 16 | 255 << 8 | 255);
			end();
		}
		return shape();
	}
}
