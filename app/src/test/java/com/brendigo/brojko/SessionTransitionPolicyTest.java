package com.brendigo.brojko;

import org.junit.Test;
import static org.junit.Assert.*;

public final class SessionTransitionPolicyTest {
    @Test public void absentOrUnusedGameNeedsNeitherAction() {
        assertFalse(SessionTransitionPolicy.needsArchive(null));
        assertFalse(SessionTransitionPolicy.needsConfirmation(null));
        TileGame game = new TileGame("classic", "", 17L);
        assertFalse(SessionTransitionPolicy.needsArchive(game));
        assertFalse(SessionTransitionPolicy.needsConfirmation(game));
    }

    @Test public void activeGameNeedsConfirmationAndArchive() {
        TileGame game = new TileGame("classic", "", 17L);
        game.moves = 1;
        assertTrue(SessionTransitionPolicy.needsArchive(game));
        assertTrue(SessionTransitionPolicy.needsConfirmation(game));
    }

    @Test public void finishedGameMustArchiveWithoutConfirmation() {
        TileGame game = new TileGame("classic", "", 17L);
        game.moves = 1;
        game.gameOver = true;
        assertTrue(SessionTransitionPolicy.needsArchive(game));
        assertFalse(SessionTransitionPolicy.needsConfirmation(game));
    }

    @Test public void alreadyArchivedGameMustNotArchiveAgain() {
        TileGame game = new TileGame("classic", "", 17L);
        game.moves = 1;
        game.archived = true;
        assertFalse(SessionTransitionPolicy.needsArchive(game));
        assertFalse(SessionTransitionPolicy.needsConfirmation(game));
    }
}
