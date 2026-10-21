package fr.axephir.tnd.domain;

/** Integer arithmetic keeps progression deterministic on both clients and servers. */
public final class Experience {
    private Experience() {}

    public static long threshold(int level) {
        if (level < 1) throw new IllegalArgumentException("level must be positive");
        long product = (long) (level - 1) * level;
        return product > Long.MAX_VALUE / 50 ? Long.MAX_VALUE : product * 50;
    }

    public static int level(long xp) {
        if (xp < 0) throw new IllegalArgumentException("negative experience");
        int low = 1, high = 429_496_730;
        // Long.MAX_VALUE is an overflow sentinel, never an attainable next level.
        while (low < high) {
            int mid = low + (high - low + 1) / 2;
            long threshold = threshold(mid);
            if (threshold != Long.MAX_VALUE && threshold <= xp) low = mid;
            else high = mid - 1;
        }
        return low;
    }

    public static long add(long xp, long amount) {
        if (xp < 0 || amount < 0) throw new IllegalArgumentException("negative experience");
        return amount > Long.MAX_VALUE - xp ? Long.MAX_VALUE : xp + amount;
    }

    public static int earnedPoints(int level) {
        if (level < 1) throw new IllegalArgumentException("level must be positive");
        return Math.min(level - 1, 99) + Math.max(0, level - 100) / 10;
    }

    /** Asymptotic bonus: prestige can never add more than five percent. */
    public static double prestigeBonus(int level) {
        int prestige = Math.max(0, level - 100);
        return 0.05 * prestige / (prestige + 500.0);
    }
}
