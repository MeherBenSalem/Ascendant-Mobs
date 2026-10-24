package com.nightbeam.remnants.mobs.mob;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/** Matches entity types against config lists of ids, {@code #tags} and {@code namespace:} prefixes. */
public final class MobFilter {
    private MobFilter() {
    }

    public static boolean matches(EntityType<?> type, List<String> entries) {
        if (entries.isEmpty()) {
            return false;
        }
        String id = EntityType.getKey(type).toString();
        for (String entry : entries) {
            if (entry.startsWith("#")) {
                ResourceLocation tag = ResourceLocation.tryParse(entry.substring(1));
                if (tag != null && type.builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, tag))) {
                    return true;
                }
            } else if (entry.endsWith(":")) {
                if (id.startsWith(entry)) {
                    return true;
                }
            } else if (entry.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
