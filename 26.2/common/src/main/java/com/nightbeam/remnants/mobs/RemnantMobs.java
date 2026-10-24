package com.nightbeam.remnants.mobs;

import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.content.ModContent;
import com.nightbeam.remnants.mobs.platform.Platform;
import com.nightbeam.remnants.mobs.platform.Registrar;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Remnants: RPG Mobs. Monsters with levels, ranks and traits. */
public final class RemnantMobs {
    public static final String MOD_ID = "remnant_mobs";
    public static final String NAME = "Remnants: RPG Mobs";
    public static final Logger LOG = LoggerFactory.getLogger(NAME);

    private static Platform platform;

    private RemnantMobs() {
    }

    /** Called by each loader's entry point before anything else. */
    public static void init(Platform platform, Registrar registrar) {
        RemnantMobs.platform = platform;
        RemnantsConfig.load(platform.configDir());
        ModContent.register(registrar);
    }

    public static Platform platform() {
        return platform;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
