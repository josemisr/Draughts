package es.urjccode.mastercloudapps.adcs.draughts.controllers;

import org.junit.Test;

import es.urjccode.mastercloudapps.adcs.draughts.models.Coordinate;
import es.urjccode.mastercloudapps.adcs.draughts.models.Game;
import es.urjccode.mastercloudapps.adcs.draughts.models.State;
import es.urjccode.mastercloudapps.adcs.draughts.models.Color;
import es.urjccode.mastercloudapps.adcs.draughts.models.GameBuilder;

import static org.junit.Assert.*;

public class PlayControllerTest {

    private PlayController playController;

    @Test
    public void testGivenPlayControllerWhenMoveThenOk() {
        Game game = new GameBuilder().build();
        playController = new PlayController(game, new State());
        Coordinate origin = new Coordinate(5, 0);
        Coordinate target = new Coordinate(4, 1);
        playController.move(origin, target);
        assertEquals(playController.getColor(target), Color.WHITE);
        assertFalse(game.isBlocked());
    }

    @Test
    public void testGivenPlayControllerWhenMoveWithoutPiecesThenIsBlocked() {
        Game game = new GameBuilder().rows(
            "        ",
            "        ",
            "        ",
            "        ",
            " n      ",
            "b       ",
            "        ",
            "        ").build();
        playController = new PlayController(game, new State());
        Coordinate origin = new Coordinate(5, 0);
        Coordinate target = new Coordinate(3, 2);
        playController.move(origin, target);
        assertEquals(playController.getColor(target), Color.WHITE);
        assertTrue(game.isBlocked());
    }

    @Test
    public void testGivenPlayControllerWhenMoveBadThenIsNotBlocked() {
        Game game = new GameBuilder().rows(
            "        ",
            "        ",
            "   n    ",
            "  b b   ",
            "     b  ",
            "b       ",
            "        ",
            "        ").build();
        playController = new PlayController(game, new State());
        Coordinate origin = new Coordinate(5, 0);
        Coordinate target = new Coordinate(4, 1);
        playController.move(origin, target);
        assertEquals(playController.getColor(target), Color.WHITE);
        assertFalse(game.isBlocked());
    }

    @Test
    public void testGivenPlayControllerWhenMoveThenIsBlocked() {
        Game game = new GameBuilder().rows(
            "        ",
            "        ",
            "n       ",
            " b      ",
            "        ",
            "b  b    ",
            "        ",
            "        ").build();
        playController = new PlayController(game, new State());
        Coordinate origin = new Coordinate(5, 3);
        Coordinate target = new Coordinate(4, 2);
        playController.move(origin, target);
        assertEquals(playController.getColor(target), Color.WHITE);
        assertTrue(game.isBlocked());
    }

    @Test
    public void testGivenPlayControllerWhenMoveAndWhiteCanEatThenRemoveWhite() {
        Game game = new GameBuilder().rows(
            "        ",
            "        ",
            "   n    ",
            "  b     ",
            "     b  ",
            "b       ",
            "        ",
            "        ").build();
        playController = new PlayController(game, new State());
        Coordinate origin = new Coordinate(4, 5);
        Coordinate target = new Coordinate(3, 6);
        Coordinate whiteToRemove = new Coordinate(3, 2);
        assertNotNull(playController.getPiece(whiteToRemove));
        playController.move(origin, target);
        assertEquals(playController.getColor(target), Color.WHITE);
        assertNull(playController.getPiece(whiteToRemove));
    }

    @Test
    public void testGivenPlayControllerWhenMoveAndDraughtCanEatThenRemoveDraught() {
        Game game = new GameBuilder().rows(
            "        ",
            "        ",
            "        ",
            "  b n   ",
            "        ",
            "b     B ",
            "        ",
            "        ").build();
        playController = new PlayController(game, new State());
        Coordinate origin = new Coordinate(3, 2);
        Coordinate target = new Coordinate(2, 1);
        Coordinate draughtToRemove = new Coordinate(5, 6);
        assertNotNull(playController.getPiece(draughtToRemove));
        playController.move(origin, target);
        assertNull(playController.getPiece(draughtToRemove));
    }

    @Test
    public void testGivenPlayControllerWhenMoveAndDraughtCantEatWhenBlackBlocksThenDraught() {
        Game game = new GameBuilder().rows(
            "        ",
            "        ",
            "        ",
            "  b n   ",
            "     n  ",
            "b     B ",
            "        ",
            "        ").build();
        playController = new PlayController(game, new State());
        Coordinate origin = new Coordinate(3, 2);
        Coordinate target = new Coordinate(2, 1);
        Coordinate draughtToRemove = new Coordinate(5, 6);
        playController.move(origin, target);
        assertEquals(playController.getColor(draughtToRemove), Color.WHITE);
    }

    @Test
    public void testGivenPlayControllerWhenMoveAndDraughtCantEatWhenWhiteBlocksThenDraught() {
        Game game = new GameBuilder().rows(
            "        ",
            "        ",
            "   n    ",
            "        ",
            "     b  ",
            "b     B ",
            "        ",
            "        ").build();
        playController = new PlayController(game, new State());
        Coordinate origin = new Coordinate(4, 5);
        Coordinate target = new Coordinate(3, 6);
        Coordinate draughtToRemove = new Coordinate(5, 6);
        playController.move(origin, target);
        assertEquals(playController.getColor(draughtToRemove), Color.WHITE);
    }

    @Test
    public void testGivenPlayControllerWhenMoveAndBlackCanEatThenRemoveBlack() {
        Game game = new GameBuilder().rows(
            "        ",
            "  n n   ",
            "   n    ",
            "  b     ",
            "     b  ",
            "b       ",
            "        ",
            "        ").build();
        playController = new PlayController(game, new State());
        Coordinate originWhite = new Coordinate(4, 5);
        Coordinate targetWhite = new Coordinate(3, 6);
        playController.move(originWhite, targetWhite);
        Coordinate origin = new Coordinate(1, 2);
        Coordinate target = new Coordinate(2, 1);
        Coordinate blackToRemove = new Coordinate(2, 3);
        assertNotNull(playController.getPiece(blackToRemove));
        playController.move(origin, target);
        assertEquals(playController.getColor(target), Color.BLACK);
        assertNull(playController.getPiece(blackToRemove));
    }

    @Test
    public void testGivenPlayControllerWhenCancelThenOk() {
        Game game = new GameBuilder().build();
        playController = new PlayController(game, new State());
        playController.cancel();
        assertEquals(Color.BLACK, playController.getColor());
        assertFalse(game.isBlocked());
    }

}
