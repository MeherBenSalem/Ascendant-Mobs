package com.nightbeam.remnants.mobs.fabric;

import com.nightbeam.remnants.mobs.client.ConfigClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu's config button. Only Mod Menu loads this entry point, so the mod runs the same without Mod Menu installed;
 * Mod Menu is a compile-only dependency.
 */
public final class RemnantMobsModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigClient::screen;
    }
}
