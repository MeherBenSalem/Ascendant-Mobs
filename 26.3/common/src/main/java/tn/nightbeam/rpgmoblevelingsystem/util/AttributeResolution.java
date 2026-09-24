package tn.nightbeam.rpgmoblevelingsystem.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;

public final class AttributeResolution {
    private AttributeResolution() {
    }

    public static Attribute resolve(String attributeId) {
        Holder<Attribute> holder = resolveHolder(attributeId);
        return holder == null ? null : holder.value();
    }

    public static Holder<Attribute> resolveHolder(String attributeId) {
        Identifier key = resolveKey(attributeId);
        if (key == null) {
            return null;
        }
        return BuiltInRegistries.ATTRIBUTE.get(key).orElse(null);
    }

    public static Identifier resolveKey(String attributeId) {
        Identifier parsed = Identifier.tryParse(attributeId);
        if (parsed == null) {
            return null;
        }
        if (BuiltInRegistries.ATTRIBUTE.containsKey(parsed)) {
            return parsed;
        }
        if ("minecraft".equals(parsed.getNamespace())) {
            if (parsed.getPath().startsWith("generic.")) {
                Identifier modern = Identifier.fromNamespaceAndPath("minecraft", parsed.getPath().substring("generic.".length()));
                if (BuiltInRegistries.ATTRIBUTE.containsKey(modern)) {
                    return modern;
                }
            } else if (!parsed.getPath().startsWith("generic.")) {
                Identifier legacy = Identifier.fromNamespaceAndPath("minecraft", "generic." + parsed.getPath());
                if (BuiltInRegistries.ATTRIBUTE.containsKey(legacy)) {
                    return legacy;
                }
            }
        }
        return parsed;
    }

    public static String normalizeAttributeId(String attributeId) {
        Identifier resolved = resolveKey(attributeId);
        return resolved == null ? attributeId : resolved.toString();
    }
}
