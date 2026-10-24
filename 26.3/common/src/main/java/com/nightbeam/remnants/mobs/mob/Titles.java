package com.nightbeam.remnants.mobs.mob;

import java.util.Random;

/** Names for Remnant-rank mobs, the same on server and client because they come from the mob's seed. */
public final class Titles {
    private static final String[] FIRST = {
            "Vor", "Mal", "Kael", "Ash", "Dra", "Mor", "Sel", "Thar", "Uth", "Gor", "Ny", "Ves", "Il", "Kor", "Bel", "Zar",
            "Hro", "Ere", "Skal", "Oth", "Rhae", "Lur", "Grav", "Aza"};
    private static final String[] MIDDLE = {"", "", "a", "e", "i", "o", "u", "ae", "y", "an", "or"};
    private static final String[] LAST = {
            "grim", "thas", "keth", "mir", "dun", "rok", "zar", "ion", "eth", "vex", "dra", "gar", "nos", "ul", "rith", "mund",
            "vael", "dor", "sha", "kai"};
    private static final String[] EPITHETS = {
            "the Hollow", "the Unyielding", "the Ashen", "the Last", "the Pale", "the Hungering", "the Unbroken",
            "the Forsaken", "the Bloodsworn", "the Undying", "the Grave-Touched", "the Starless", "the Ruinous",
            "the Silent", "the Cinderborn", "the Oathless", "the Withered", "the Devourer", "the Nightborn", "the Remnant",
            "the Eclipsed", "the Ironbound", "the Sorrowful", "the Gloam-Eyed"};

    private Titles() {
    }

    public static String of(long seed) {
        Random random = new Random(seed ^ 0x5DEECE66DL);
        String name = FIRST[random.nextInt(FIRST.length)] + MIDDLE[random.nextInt(MIDDLE.length)] + LAST[random.nextInt(LAST.length)];
        return name + " " + EPITHETS[random.nextInt(EPITHETS.length)];
    }
}
