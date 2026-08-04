package tn.nightbeam.rpgmoblevelingsystem.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

public final class AttributeResolution {
    private AttributeResolution() {
    }

    public static Attribute resolve(String attributeId) {
        ResourceLocation key = resolveKey(attributeId);
        return key == null ? null : BuiltInRegistries.ATTRIBUTE.get(key);
    }

    public static ResourceLocation resolveKey(String attributeId) {
        ResourceLocation parsed = ResourceLocation.tryParse(attributeId);
        if (parsed == null) {
            return null;
        }
        if (BuiltInRegistries.ATTRIBUTE.containsKey(parsed)) {
            return parsed;
        }
        if ("minecraft".equals(parsed.getNamespace())) {
            if (parsed.getPath().startsWith("generic.")) {
                ResourceLocation modern = new ResourceLocation("minecraft", parsed.getPath().substring("generic.".length()));
                if (BuiltInRegistries.ATTRIBUTE.containsKey(modern)) {
                    return modern;
                }
            } else if (!parsed.getPath().startsWith("generic.")) {
                ResourceLocation legacy = new ResourceLocation("minecraft", "generic." + parsed.getPath());
                if (BuiltInRegistries.ATTRIBUTE.containsKey(legacy)) {
                    return legacy;
                }
            }
        }
        return parsed;
    }

    public static String normalizeAttributeId(String attributeId) {
        ResourceLocation resolved = resolveKey(attributeId);
        return resolved == null ? attributeId : resolved.toString();
    }
}
