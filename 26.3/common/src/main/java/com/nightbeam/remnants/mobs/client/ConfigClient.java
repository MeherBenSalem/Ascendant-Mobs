package com.nightbeam.remnants.mobs.client;

import com.nightbeam.remnants.mobs.network.ConfigValuesPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.Nullable;

/** Client side of the settings screen: how to reach the server, and the screen's entry points for each loader. */
public final class ConfigClient {
    /** Sends payloads to the server the player is connected to; each loader's client setup installs one. */
    public interface Sender {
        boolean canSend(CustomPacketPayload.Type<?> type);

        void send(CustomPacketPayload payload);
    }

    private static Sender sender = new Sender() {
        @Override
        public boolean canSend(CustomPacketPayload.Type<?> type) {
            return false;
        }

        @Override
        public void send(CustomPacketPayload payload) {
        }
    };

    private ConfigClient() {
    }

    public static void setSender(Sender value) {
        sender = value;
    }

    /** True when connected to a server that knows {@code type}. */
    static boolean canSend(CustomPacketPayload.Type<?> type) {
        return Minecraft.getInstance().getConnection() != null && sender.canSend(type);
    }

    static void send(CustomPacketPayload payload) {
        sender.send(payload);
    }

    /** The settings screen; Mod Menu and the NeoForge/Forge mod list open this. */
    public static Screen screen(@Nullable Screen parent) {
        return new ConfigScreen(parent);
    }

    /** The server's values arrived: update the open screen, or open it ({@code /remnants config}). */
    public static void onValues(ConfigValuesPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Screen current = minecraft.gui.screen();
            if (current instanceof ConfigScreen screen) {
                screen.serverValues(payload);
            } else if (payload.open()) {
                ConfigScreen screen = new ConfigScreen(current, false);
                screen.serverValues(payload);
                minecraft.gui.setScreen(screen);
            }
        });
    }
}
