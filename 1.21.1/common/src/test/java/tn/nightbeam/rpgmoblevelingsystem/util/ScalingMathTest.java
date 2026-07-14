package tn.nightbeam.rpgmoblevelingsystem.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ScalingMathTest {
    @Test
    void applyRoundingFloor() {
        Assertions.assertEquals(3.0, ScalingMath.applyRounding(3.9, "floor"));
    }

    @Test
    void applyRoundingCeil() {
        Assertions.assertEquals(4.0, ScalingMath.applyRounding(3.1, "ceil"));
    }

    @Test
    void applyRoundingRound() {
        Assertions.assertEquals(4.0, ScalingMath.applyRounding(3.6, "round"));
    }

    @Test
    void horizontalDistanceUsesEuclideanNotSquared() {
        Assertions.assertEquals(5.0, ScalingMath.horizontalDistance(3, 4));
    }

    @Test
    void threeDimensionalDistance() {
        Assertions.assertEquals(5.0, ScalingMath.threeDimensionalDistance(3, 0, 4));
    }
}
