package com.nightbeam.remnants.mobs.client;

import com.nightbeam.remnants.mobs.RemnantMobs;
import com.nightbeam.remnants.mobs.content.Rank;
import com.nightbeam.remnants.mobs.content.Trait;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/** Icons from the {@code remnant_mobs:icons} bitmap font: grey-scale glyphs tinted by the text colour. */
public final class Glyphs {
    public static final FontDescription FONT = new FontDescription.Resource(RemnantMobs.id("icons"));
    public static final char CREST = '';
    public static final char TRAIT = '';
    public static final char SKULL = '';
    public static final char MOON = '';
    public static final char BRAND = '';
    public static final char HEART = '';
    public static final char SWORD = '';
    public static final char SHIELD = '';
    public static final char LENS = '';

    private Glyphs() {
    }

    public static MutableComponent icon(char glyph, int color) {
        return Component.literal(String.valueOf(glyph)).withStyle(Style.EMPTY.withFont(FONT).withColor(color));
    }

    public static MutableComponent crest(Rank rank) {
        return icon((char) (CREST + rank.ordinal()), rank.color());
    }

    public static MutableComponent trait(Trait trait) {
        return icon((char) (TRAIT + trait.ordinal()), trait.color());
    }
}
