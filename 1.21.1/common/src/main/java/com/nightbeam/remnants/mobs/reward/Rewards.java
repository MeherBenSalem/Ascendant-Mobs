package com.nightbeam.remnants.mobs.reward;

import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.compat.RasBridge;
import com.nightbeam.remnants.mobs.config.LevelsConfig;
import com.nightbeam.remnants.mobs.config.ProgressionConfig;
import com.nightbeam.remnants.mobs.config.RanksConfig;
import com.nightbeam.remnants.mobs.config.RemnantsConfig;
import com.nightbeam.remnants.mobs.config.RewardsConfig;
import com.nightbeam.remnants.mobs.content.ModContent;
import com.nightbeam.remnants.mobs.content.Rank;
import com.nightbeam.remnants.mobs.difficulty.Difficulty;
import com.nightbeam.remnants.mobs.event.EventNights;
import com.nightbeam.remnants.mobs.event.Feedback;
import com.nightbeam.remnants.mobs.event.HuntersMoon;
import com.nightbeam.remnants.mobs.mob.MobData;
import com.nightbeam.remnants.mobs.mob.RemnantBars;
import com.nightbeam.remnants.mobs.mob.RemnantMob;
import com.nightbeam.remnants.mobs.mob.Threat;
import com.nightbeam.remnants.mobs.mob.Titles;
import com.nightbeam.remnants.mobs.trait.TraitEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Experience, loot, shards and Valor for leveled mobs. */
public final class Rewards {
    private Rewards() {
    }

    private static double branded(MobData data) {
        return data.flag(MobData.BRANDED) ? RemnantsConfig.rewards().brandedMultiplier : 1.0;
    }

    /** Dropped experience: (1 + level x per_level) x rank experience x Brand. */
    public static int experience(Mob mob, int base) {
        MobData data = RemnantMob.data(mob);
        if (data == null || base <= 0) {
            return base;
        }
        if (data.flag(MobData.BOSS) && !RemnantsConfig.levels().bossStats) {
            return base;
        }
        double multiplier = (1.0 + data.level() * RemnantsConfig.rewards().experiencePerLevel)
                * RemnantsConfig.ranks().stats(data.rank()).experience() * branded(data);
        return (int) Math.min(Integer.MAX_VALUE / 2, Math.round(base * multiplier));
    }

    /** Called at the end of the mob's death drops (only when it drops loot at all). */
    public static void dropLoot(ServerLevel level, Mob mob, DamageSource source, boolean playerKilled, LootRoller roller) {
        MobData data = RemnantMob.data(mob);
        RewardsConfig rewards = RemnantsConfig.rewards();
        if (data == null || !data.ranked() || data.flag(MobData.MINION) || (rewards.requirePlayerKill && !playerKilled)) {
            return;
        }
        RanksConfig.Stats stats = RemnantsConfig.ranks().stats(data.rank());
        if (rewards.extraLoot) {
            int rolls = (int) Math.round(stats.lootRolls() * branded(data));
            for (int i = 0; i < rolls; i++) {
                roller.roll();
            }
        }
        if (rewards.shards && stats.shardsMax() > 0) {
            double count = stats.shardsMin() + mob.getRandom().nextInt(stats.shardsMax() - stats.shardsMin() + 1);
            if (data.flag(MobData.MOONLIT) || HuntersMoon.affects(level)) {
                count *= RemnantsConfig.levels().moonShardMultiplier;
            }
            count *= branded(data) * EventNights.shardMultiplier(mob);
            int shards = (int) Math.round(count) + EventNights.bossShards(mob);
            while (shards > 0) {
                int stack = Math.min(shards, 64);
                mob.spawnAtLocation(new ItemStack(ModContent.REMNANT_SHARD.get(), stack));
                shards -= stack;
            }
        }
        if (playerKilled) {
            dropProgression(level, mob, data);
        }
    }

    /** Vital Embers and healing items (progression.json), likelier the higher the mob sits in its dimension's band. */
    private static void dropProgression(ServerLevel level, Mob mob, MobData data) {
        ProgressionConfig c = RemnantsConfig.progression();
        if (!c.enabled) {
            return;
        }
        LevelsConfig.Band band = RemnantsConfig.levels().band(level.dimension().location().toString());
        double fraction = Difficulty.fraction(data.level(), band.min(), band.max());
        var random = mob.getRandom();
        double ember = mob.getTags().contains(EventNights.BOSS_TAG) ? c.emberEventBoss : c.emberChance.getOrDefault(data.rank(), 0.0);
        if (ember > 0 && random.nextDouble() < c.chance(ember, fraction)) {
            mob.spawnAtLocation(new ItemStack(ModContent.VITAL_EMBER.get()));
        }
        if (data.rank().atLeast(Rank.VETERAN) && c.salveChance > 0 && random.nextDouble() < c.chance(c.salveChance, fraction)) {
            mob.spawnAtLocation(new ItemStack(ModContent.MENDING_SALVE.get()));
        }
        if (data.rank().atLeast(Rank.ELITE) && c.draughtChance > 0 && random.nextDouble() < c.chance(c.draughtChance, fraction)) {
            mob.spawnAtLocation(new ItemStack(ModContent.HUNTERS_DRAUGHT.get()));
        }
    }

    /** Rolls the mob's loot table once more. */
    @FunctionalInterface
    public interface LootRoller {
        void roll();
    }

    /** A leveled mob died: Valor, advancements, announcements, Volatile. */
    public static void onDeath(ServerLevel level, Mob mob, DamageSource source) {
        MobData data = RemnantMob.data(mob);
        if (data == null) {
            return;
        }
        RemnantBars.remove(mob);
        TraitEffects.onDeath(level, mob, data);
        if (!data.ranked() || data.flag(MobData.MINION)) {
            return;
        }
        ServerPlayer killer = killer(mob, source);
        if (killer != null && RemnantMobs.platform().isFakePlayer(killer)) {
            killer = null;
        }
        if (data.rank().atLeast(Rank.CHAMPION)) {
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                    (data.rank() == Rank.REMNANT ? ModContent.REMNANT_SLAIN : ModContent.RANK_SLAIN).get(), SoundSource.HOSTILE, 1.5F, 1.0F);
        }
        if (data.rank() == Rank.REMNANT && RemnantsConfig.ranks().remnantAnnounce) {
            Component title = Component.literal(Titles.of(data.seed())).withStyle(style -> style.withColor(Rank.REMNANT.color()));
            Component message = killer != null
                    ? Component.translatable("message.remnant_mobs.remnant_slain", title, data.level(), killer.getDisplayName())
                    : Component.translatable("message.remnant_mobs.remnant_fallen", title, data.level());
            level.getServer().getPlayerList().broadcastSystemMessage(message.copy().withStyle(ChatFormatting.GOLD), false);
        }
        if (killer == null) {
            return;
        }
        int hunter = Threat.hunterLevel(killer);
        grantKillAdvancements(killer, data, hunter, level);
        RewardsConfig rewards = RemnantsConfig.rewards();
        if (rewards.valorEnabled && RasBridge.present()) {
            int playerLevel = RasBridge.level(killer);
            double base = rewards.valorPerRank.getOrDefault(data.rank(), 0.0) + data.level() * rewards.valorPerLevel;
            double valor = base * rewards.challenge(playerLevel >= 0 ? data.level() - playerLevel : 0) * branded(data);
            if (valor >= 0.5) {
                RasBridge.addValor(killer, valor);
                if (rewards.valorMessage) {
                    Feedback.actionBar(killer, Component.translatable("message.remnant_mobs.valor", slainName(mob, data),
                            Component.literal("+" + Math.round(valor)).withStyle(ChatFormatting.GOLD)));
                }
            }
        }
    }

    private static void grantKillAdvancements(ServerPlayer killer, MobData data, int hunterLevel, ServerLevel level) {
        if (data.rank().atLeast(Rank.ELITE)) {
            Advancements.award(killer, Advancements.PROVEN_HUNTER);
        }
        if (data.rank().atLeast(Rank.CHAMPION)) {
            Advancements.award(killer, Advancements.CHAMPIONS_BANE);
        }
        if (data.rank() == Rank.REMNANT) {
            Advancements.award(killer, Advancements.REMNANTS_END);
        }
        if (data.traits().size() >= 4) {
            Advancements.award(killer, Advancements.NOTHING_LEFT_TO_HIDE);
        }
        if (hunterLevel >= 0 && data.level() - hunterLevel >= 10) {
            Advancements.award(killer, Advancements.PUNCHING_UP);
        }
        if (HuntersMoon.affects(level)) {
            Advancements.award(killer, Advancements.HUNTERS_MOON);
        }
    }

    /** The player credited with a kill: the attacker, or whoever hurt the mob last. */
    public static @Nullable ServerPlayer killer(LivingEntity victim, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return player;
        }
        LivingEntity credit = victim.getKillCredit();
        return credit instanceof ServerPlayer player ? player : null;
    }

    /** "Elite Vindicator" in the rank's colour, or a named mob's own name (a Remnant's is its title). */
    private static Component slainName(Mob mob, MobData data) {
        Component name = mob.hasCustomName() ? mob.getCustomName()
                : Component.translatable("plate.remnant_mobs.ranked_name", Component.translatable(data.rank().translationKey()), mob.getType().getDescription());
        return name.copy().withStyle(style -> style.withColor(data.rank().color()));
    }
}
