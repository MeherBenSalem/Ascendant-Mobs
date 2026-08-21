package tn.nightbeam.rpgmoblevelingsystem.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DamageAttributionPathsTest {
    @Test
    void causingLivingWinsOverEverything() {
        Assertions.assertEquals(
                DamageAttributionPaths.CAUSING_LIVING,
                DamageAttributionPaths.choose(true, true, true, true));
    }

    @Test
    void causingProjectileOwnerBeatsDirectPaths() {
        Assertions.assertEquals(
                DamageAttributionPaths.CAUSING_PROJECTILE_OWNER,
                DamageAttributionPaths.choose(false, true, true, true));
    }

    @Test
    void directProjectileOwnerUsedWhenCausingIsNotAttributable() {
        Assertions.assertEquals(
                DamageAttributionPaths.DIRECT_PROJECTILE_OWNER,
                DamageAttributionPaths.choose(false, false, true, true));
    }

    @Test
    void directLivingUsedAsLastResort() {
        Assertions.assertEquals(
                DamageAttributionPaths.DIRECT_LIVING,
                DamageAttributionPaths.choose(false, false, false, true));
    }

    @Test
    void noneWhenNoAttackerResolved() {
        Assertions.assertEquals(
                DamageAttributionPaths.NONE,
                DamageAttributionPaths.choose(false, false, false, false));
    }
}
