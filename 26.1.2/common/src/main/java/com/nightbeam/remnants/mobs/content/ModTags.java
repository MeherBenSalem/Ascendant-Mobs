package com.nightbeam.remnants.mobs.content;

import com.nightbeam.remnants.mobs.RemnantMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModTags {
    /** Get a level for show but no rank, traits or (unless boss_stats) stat changes. */
    public static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE, RemnantMobs.id("bosses"));
    /** Never get a level. */
    public static final TagKey<EntityType<?>> NEVER_LEVELED = TagKey.create(Registries.ENTITY_TYPE, RemnantMobs.id("never_leveled"));
    /** Get a level but never a rank. */
    public static final TagKey<EntityType<?>> NEVER_RANKED = TagKey.create(Registries.ENTITY_TYPE, RemnantMobs.id("never_ranked"));

    private ModTags() {
    }

    public static boolean is(EntityType<?> type, TagKey<EntityType<?>> tag) {
        return type.builtInRegistryHolder().is(tag);
    }
}
