package com.nightbeam.remnants.mobs.client;

import com.nightbeam.remnants.mobs.client.ClientState;
import com.nightbeam.remnants.mobs.config.ClientConfig;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.content.Rank;
import com.nightbeam.remnants.mobs.content.Trait;
import com.nightbeam.remnants.mobs.mob.MobData;
import com.nightbeam.remnants.mobs.mob.MobState;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Everything a nameplate shows, worked out once per frame from the mob. Drawing is per version (see Plates). */
public final class Plate {
    public static final int BAR_WIDTH = 64;
    public static final int RED = 0xFFC8423A;

    public Vec3 attachment = Vec3.ZERO;
    public Component title = Component.empty();
    public Rank rank = Rank.COMMON;
    public int barColor;
    public float health;
    public float trail;
    public boolean ward;
    public boolean showHealth;
    public String healthText = "";
    public List<Trait> traits = List.of();
    public boolean traitNames;
    public float alpha = 1.0F;
    public float scale = 1.0F;

    /** Builds the plate, or returns null when this mob should not show one right now. */
    public static @Nullable Plate build(Mob mob, float partialTicks, double distanceSq) {
        ClientConfig config = RemnantsConfig.client();
        Minecraft minecraft = Minecraft.getInstance();
        if (!config.plates || config.hideWithHud && minecraft.gui.hud.isHidden() || !mob.isAlive() || mob.isInvisible() || minecraft.player == null) {
            return null;
        }
        MobState state = ClientState.state(mob);
        MobData data = state.data;
        if (data == null || mob.isVehicle() && mob.hasPassenger(minecraft.player)) {
            return null;
        }
        boolean looking = minecraft.crosshairPickEntity == mob;
        if (config.lookAtOnly && !looking) {
            return null;
        }
        double range = data.ranked() || data.flag(MobData.BOSS) ? config.distance : config.commonDistance;
        if (looking) {
            range = Math.max(range, config.distance);
        }
        if (range <= 0 || distanceSq > range * range) {
            return null;
        }
        Plate plate = new Plate();
        Vec3 attachment = mob.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, mob.getYRot(partialTicks));
        plate.attachment = attachment != null ? attachment : new Vec3(0, mob.getBbHeight(), 0);
        plate.rank = data.rank();
        plate.scale = (float) config.scale;
        double distance = Math.sqrt(distanceSq);
        plate.alpha = (float) Mth.clamp((range - distance) / (range * 0.2), 0.15, 1.0);

        boolean relative = config.relativeColors;
        int levelColor = ClientState.levelColor(data.level(), relative);
        MutableComponent title = Component.empty();
        if (data.ranked()) {
            title.append(Glyphs.crest(data.rank())).append(" ");
        }
        if (data.flag(MobData.BRANDED)) {
            title.append(Glyphs.icon(Glyphs.BRAND, 0xFFE0563C)).append(" ");
        }
        if (ClientState.deadly(data.level(), relative)) {
            title.append(Glyphs.icon(Glyphs.SKULL, levelColor)).append(" ");
        }
        title.append(Component.literal(String.valueOf(data.level())).withStyle(style -> style.withColor(levelColor).withBold(true)));
        title.append("  ");
        Component name = mob.hasCustomName() ? mob.getCustomName() : mob.getType().getDescription();
        int nameColor = data.ranked() ? data.rank().color() : 0xE6E1D3;
        if (data.ranked() && data.rank() != Rank.REMNANT && !mob.hasCustomName()) {
            title.append(Component.translatable("plate.remnant_mobs.ranked_name", Component.translatable(data.rank().translationKey()), name)
                    .withStyle(style -> style.withColor(nameColor)));
        } else {
            title.append(name.copy().withStyle(style -> style.withColor(nameColor)));
        }
        plate.title = title;

        float max = Math.max(1.0F, mob.getMaxHealth());
        float health = Mth.clamp(mob.getHealth(), 0.0F, max);
        long now = System.currentTimeMillis();
        if (state.trailHealth < 0 || health >= state.trailHealth || now - state.trailFrame > 1000) {
            state.trailHealth = health;
        } else {
            if (state.lastSeenHealth > health) {
                state.trailTime = now;
            }
            if (now - state.trailTime > 450) {
                float seconds = Math.min(0.1F, (now - state.trailFrame) / 1000.0F);
                state.trailHealth = Math.max(health, state.trailHealth - max * 0.8F * seconds);
            }
        }
        state.trailFrame = now;
        state.lastSeenHealth = health;
        plate.health = health / max;
        plate.trail = state.trailHealth / max;
        plate.barColor = data.ranked() ? 0xFF000000 | data.rank().color() : RED;
        plate.ward = state.wardUp;
        plate.showHealth = config.healthNumbers;
        plate.healthText = health >= 1000 ? String.format(java.util.Locale.ROOT, "%.1fk", health / 1000.0F) : String.valueOf(Mth.ceil(health));

        double traitsRange = Math.max(config.traitsDistance, 0);
        if (!data.traits().isEmpty() && (looking || distanceSq <= traitsRange * traitsRange)) {
            plate.traits = data.traitList();
            plate.traitNames = looking || plate.traits.size() <= 2;
        }
        return plate;
    }
}
