package com.nightbeam.remnants.mobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nightbeam.remnants.mobs.content.Rank;
import com.nightbeam.remnants.mobs.content.Trait;
import com.nightbeam.remnants.mobs.mob.MobData;
import com.nightbeam.remnants.mobs.mob.Titles;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class MobDataTest {
    @Test
    void tagRoundTrips() {
        MobData data = new MobData(42, Rank.CHAMPION, EnumSet.of(Trait.VAMPIRIC, Trait.SWIFT, Trait.VOLATILE), -1234567890123L,
                MobData.BRANDED | MobData.MOONLIT);
        MobData decoded = MobData.decode(data.encode());
        assertNotNull(decoded);
        assertEquals(data, decoded);
        assertTrue(decoded.flag(MobData.BRANDED));
    }

    @Test
    void decodesOlderAndForeignTags() {
        assertNull(MobData.decode("am_level_12.0"));
        assertNull(MobData.decode("remnant_mobs:data|x"));
        MobData minimal = MobData.decode("remnant_mobs:data|7|elite");
        assertNotNull(minimal);
        assertEquals(7, minimal.level());
        assertEquals(Rank.ELITE, minimal.rank());
        assertTrue(minimal.traits().isEmpty());
        MobData unknown = MobData.decode("remnant_mobs:data|3|nonsense|vampiric+not_a_trait|ff|0");
        assertNotNull(unknown);
        assertEquals(Rank.COMMON, unknown.rank());
        assertEquals(EnumSet.of(Trait.VAMPIRIC), unknown.traits());
    }

    @Test
    void traitMaskRoundTrips() {
        MobData data = new MobData(1, Rank.REMNANT, EnumSet.of(Trait.FROSTBITE, Trait.WARDED, Trait.PACKLEADER, Trait.VOLATILE), 1, 0);
        assertEquals(data.traits(), MobData.traitsFromMask(data.traitMask()));
    }

    @Test
    void conflictsAreSymmetric() {
        for (Trait a : Trait.values()) {
            for (Trait b : Trait.values()) {
                assertEquals(a.conflictsWith(b), b.conflictsWith(a));
            }
        }
        assertTrue(Trait.INFERNAL.conflictsWith(Trait.FROSTBITE));
        assertTrue(Trait.SWIFT.conflictsWith(Trait.JUGGERNAUT));
    }

    @Test
    void titlesAreStable() {
        assertEquals(Titles.of(99L), Titles.of(99L));
        assertTrue(Titles.of(99L).contains(" the ") || Titles.of(99L).split(" ").length >= 2);
    }
}
