package tn.nightbeam.rpgmoblevelingsystem.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies Neat mixins only when {@code vazkii.neat.HealthBarRenderer} is available.
 * Detection is deferred to {@link #shouldApplyMixin} because mixin plugins often load
 * before optional mods are on the classpath.
 */
public final class NeatMixinPlugin implements IMixinConfigPlugin {
    private static final String NEAT_RENDERER = "vazkii.neat.HealthBarRenderer";

    private static boolean isNeatPresent() {
        try {
            Class.forName(NEAT_RENDERER, false, NeatMixinPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return isNeatPresent();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
