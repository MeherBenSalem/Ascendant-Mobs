package tn.nightbeam.rpgmoblevelingsystem.fabric.client;

/**
 * 26.3 draws levels through {@code EntityRendererNameTagMixin}; no WorldRenderEvents hook.
 */
public final class FabricMobLevelOverlay {
    private FabricMobLevelOverlay() {
    }

    public static void register() {
        // Nameplates are applied in common via EntityRenderer extractNameTags.
    }
}
