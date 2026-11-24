package com.brendigo.brojko;

/** Pure mission threshold policy; persistence and once-only grants stay with the caller. */
final class RewardPolicy {
    private RewardPolicy() {}

    interface Grant {
        void accept(int bit, int amount, boolean reached);
    }

    static void evaluate(int merges, int peak, boolean undoUsed, int streak,
                         int recordTile, int best, int weekPoints, Grant grant) {
        grant.accept(1, 50, merges >= 20);
        grant.accept(2, 100, peak >= 512 && !undoUsed);
        grant.accept(4, 150, streak >= 7);
        grant.accept(8, 500, recordTile >= 1024);
        grant.accept(16, 1000, best >= 50000);
        grant.accept(32, 200, weekPoints >= 10000);
    }
}
