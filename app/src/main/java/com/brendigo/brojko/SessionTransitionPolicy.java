package com.brendigo.brojko;

/** Pure session transition decisions; durable commits remain with the coordinator. */
final class SessionTransitionPolicy {
    private SessionTransitionPolicy() {}

    static boolean needsArchive(TileGame game) {
        return game != null && !game.archived && game.moves > 0;
    }

    static boolean needsConfirmation(TileGame game) {
        return needsArchive(game) && !game.gameOver;
    }
}
