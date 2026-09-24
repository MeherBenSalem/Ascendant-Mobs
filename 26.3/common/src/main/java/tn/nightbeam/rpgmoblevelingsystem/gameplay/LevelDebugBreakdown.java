package tn.nightbeam.rpgmoblevelingsystem.gameplay;

public final class LevelDebugBreakdown {
    public final String entityId;
    public final double baseLevel;
    public final double spawnDifference;
    public final double playerContribution;
    public final double structureMin;
    public final double lockedLevel;
    public final double beforeClamp;
    public final double finalLevel;
    public final String scaleType;
    public final String playerScaleMode;
    public final boolean rasLoaded;

    public LevelDebugBreakdown(
            String entityId,
            double baseLevel,
            double spawnDifference,
            double playerContribution,
            double structureMin,
            double lockedLevel,
            double beforeClamp,
            double finalLevel,
            String scaleType,
            String playerScaleMode,
            boolean rasLoaded
    ) {
        this.entityId = entityId;
        this.baseLevel = baseLevel;
        this.spawnDifference = spawnDifference;
        this.playerContribution = playerContribution;
        this.structureMin = structureMin;
        this.lockedLevel = lockedLevel;
        this.beforeClamp = beforeClamp;
        this.finalLevel = finalLevel;
        this.scaleType = scaleType;
        this.playerScaleMode = playerScaleMode;
        this.rasLoaded = rasLoaded;
    }

    @Override
    public String toString() {
        return "entity=" + entityId
                + " base=" + baseLevel
                + " spawnDiff=" + spawnDifference
                + " player=" + playerContribution
                + " structureMin=" + structureMin
                + " locked=" + lockedLevel
                + " beforeClamp=" + beforeClamp
                + " final=" + finalLevel
                + " scaleType=" + scaleType
                + " playerMode=" + playerScaleMode
                + " ras=" + rasLoaded;
    }
}
