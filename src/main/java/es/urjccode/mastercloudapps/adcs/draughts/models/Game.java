package es.urjccode.mastercloudapps.adcs.draughts.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {

    private Board board;
    private Turn turn;

    Game(Board board) {
        this.turn = new Turn();
        this.board = board;
    }

    public Game() {
        this(new Board());
        this.reset();
    }

    public void reset() {
        for (int i = 0; i < Coordinate.getDimension(); i++)
            for (int j = 0; j < Coordinate.getDimension(); j++) {
                Coordinate coordinate = new Coordinate(i, j);
                Color color = Color.getInitialColor(coordinate);
                Piece piece = null;
                if (color != null)
                    piece = new Pawn(color);
                this.board.put(coordinate, piece);
            }
        if (this.turn.getColor() != Color.WHITE)
            this.turn.change();
    }

    public Error move(Coordinate... coordinates) {
        Error error = null;
        List<Coordinate> removedCoordinates = new ArrayList<Coordinate>();
        int pair = 0;
        List<Coordinate> canEatCoordinates= new ArrayList<Coordinate>();
        do {
            error = this.isCorrectPairMove(pair, coordinates);
            if (error == null) {
                this.checkMovementsColor(canEatCoordinates,removedCoordinates,pair,coordinates);
                this.pairMove(removedCoordinates, pair, coordinates);
                removeRandomBadMovement(canEatCoordinates,removedCoordinates);
                pair++;
            }
        }  while (pair < coordinates.length - 1 && error == null);
        error = this.isCorrectGlobalMove(error, removedCoordinates, coordinates);
        if (error == null)
            this.turn.change();
        else
            this.unMovesUntilPair(removedCoordinates, pair, coordinates);
        return error;
    }

    private Error isCorrectPairMove(int pair, Coordinate... coordinates) {
        assert coordinates[pair] != null;
        assert coordinates[pair + 1] != null;
        if (board.isEmpty(coordinates[pair]))
            return Error.EMPTY_ORIGIN;
        if (this.turn.getOppositeColor() == this.board.getColor(coordinates[pair]))
            return Error.OPPOSITE_PIECE;
        if (!this.board.isEmpty(coordinates[pair + 1]))
            return Error.NOT_EMPTY_TARGET;
        List<Piece> betweenDiagonalPieces =
            this.board.getBetweenDiagonalPieces(coordinates[pair], coordinates[pair + 1]);
        return this.board.getPiece(coordinates[pair]).isCorrectMovement(betweenDiagonalPieces, pair, coordinates);
    }

    private void pairMove(List<Coordinate> removedCoordinates, int pair, Coordinate... coordinates) {
        Coordinate forRemoving = this.getBetweenDiagonalPiece(pair, coordinates);
        if (forRemoving != null) {
            removedCoordinates.add(0, forRemoving);
            this.board.remove(forRemoving);
        }
        this.board.move(coordinates[pair], coordinates[pair + 1]);
        if (this.board.getPiece(coordinates[pair + 1]).isLimit(coordinates[pair + 1])) {
            Color color = this.board.getColor(coordinates[pair + 1]);
            this.board.remove(coordinates[pair + 1]);
            this.board.put(coordinates[pair + 1], new Draught(color));
        }
    }
    private void checkMovementsColor(List<Coordinate> canEatCoordinates,List<Coordinate> removedCoordinates,int pair, Coordinate...coordinates){
        List<Coordinate> mv= this.getCoordinatesWithActualColor();
        if(removedCoordinates.size() ==0 ){
            for (Coordinate coordinate : mv)
                if(this.getPiece(coordinate)!= this.getPiece(coordinates[pair]))
                    this.checkPossibleBadMovement(canEatCoordinates, coordinate);
        }
    }

    private void removeRandomBadMovement(List<Coordinate> canEatCoordinates,List<Coordinate> removedCoordinates){
        if(removedCoordinates.size()==0 && canEatCoordinates.size()>0 ){
            int number = new Random().nextInt(canEatCoordinates.size());;
            removedCoordinates.add(0,canEatCoordinates.get(number));
            this.board.remove(canEatCoordinates.get(number));
        }
    }
    private void checkPossibleBadMovement(List<Coordinate> removedCoordinates, Coordinate coordinate) {
        if (this.isPossibleToEat(coordinate)) {
            removedCoordinates.add(0, coordinate);
        }
    }

    private boolean isPossibleToEat(Coordinate coordinate){
        Piece piece = this.getPiece(coordinate);
        if(piece.getCode().equals("b") || piece.getCode().equals("n"))
            return this.isPosibleToEatPiece(piece, coordinate);
        else if(piece.getCode().equals("B") || piece.getCode().equals("N")) {
            return this.isPosibleToEatDraught(piece, coordinate);
        }
        return false;
    }

    private boolean isPosibleToEatPiece(Piece piece, Coordinate coordinate){
        List<Coordinate> coordinates = coordinate.getDiagonalCoordinates(2);
        for (Coordinate coor : coordinates) {
            if (this.isCorrectPairMove(0, coordinate, coor) == null) return true;
        }
        return false;
    }

    private boolean isPosibleToEatDraught(Piece piece, Coordinate coordinate){
        Draught draught = new Draught(piece.getColor());
        List<Coordinate> coordinatesToMove = new ArrayList<Coordinate>();
        coordinatesToMove.add(coordinate);
        for(int i = 7; i >= 2; i--)
        {
            coordinatesToMove.addAll(coordinate.getDiagonalCoordinates(i));
        }
        Coordinate[] myCoordinatesToMoveArray = new Coordinate[coordinatesToMove.size()];
        coordinatesToMove.toArray(myCoordinatesToMoveArray);
        List<Piece> betweenDiagonalPieces = this.board.getBetweenDiagonalPieces(myCoordinatesToMoveArray[0], myCoordinatesToMoveArray[1]);
        for(int i = 1; i < myCoordinatesToMoveArray.length; i++)
        {
            if (betweenDiagonalPieces.size()>0 && draught.isCorrectDiagonalMovement(betweenDiagonalPieces.size(),0,myCoordinatesToMoveArray[i])==null) {
                return true;
            }
        }
        return false;
    }

    private Coordinate getBetweenDiagonalPiece(int pair, Coordinate... coordinates) {
        assert coordinates[pair].isOnDiagonal(coordinates[pair + 1]);
        List<Coordinate> betweenCoordinates = coordinates[pair].getBetweenDiagonalCoordinates(coordinates[pair + 1]);
        if (betweenCoordinates.isEmpty())
            return null;
        for (Coordinate coordinate : betweenCoordinates) {
            if (this.getPiece(coordinate) != null)
                return coordinate;
        }
        return null;
    }

    private Error isCorrectGlobalMove(Error error, List<Coordinate> removedCoordinates, Coordinate... coordinates){
        if (error != null)
            return error;
        if (coordinates.length > 2 && coordinates.length > removedCoordinates.size() + 1)
            return Error.TOO_MUCH_JUMPS;
        return null;
    }

    private void unMovesUntilPair(List<Coordinate> removedCoordinates, int pair, Coordinate... coordinates) {
        for (int j = pair; j > 0; j--)
            this.board.move(coordinates[j], coordinates[j - 1]);
        for (Coordinate removedPiece : removedCoordinates)
            this.board.put(removedPiece, new Pawn(this.getOppositeTurnColor()));
    }

    public boolean isBlocked() {
        for (Coordinate coordinate : this.getCoordinatesWithActualColor())
            if (!this.isBlocked(coordinate))
                return false;
        return true;
    }

    private List<Coordinate> getCoordinatesWithActualColor() {
        List<Coordinate> coordinates = new ArrayList<Coordinate>();
        for (int i = 0; i < this.getDimension(); i++) {
            for (int j = 0; j < this.getDimension(); j++) {
                Coordinate coordinate = new Coordinate(i, j);
                Piece piece = this.getPiece(coordinate);
                if (piece != null && piece.getColor() == this.getTurnColor())
                    coordinates.add(coordinate);
            }
        }
        return coordinates;
    }

    private boolean isBlocked(Coordinate coordinate) {
        for (int i = 1; i <= 2; i++)
            for (Coordinate target : coordinate.getDiagonalCoordinates(i))
                if (this.isCorrectPairMove(0, coordinate, target) == null)
                    return false;
        return true;
    }

    public void cancel() {
        for (Coordinate coordinate : this.getCoordinatesWithActualColor())
            this.board.remove(coordinate);
        this.turn.change();
    }

    public Color getColor(Coordinate coordinate) {
        assert coordinate != null;
        return this.board.getColor(coordinate);
    }

    public Color getTurnColor() {
        return this.turn.getColor();
    }

    private Color getOppositeTurnColor() {
        return this.turn.getOppositeColor();
    }

    public Piece getPiece(Coordinate coordinate) {
        assert coordinate != null;
        return this.board.getPiece(coordinate);
    }

    public int getDimension() {
        return Coordinate.getDimension();
    }

    @Override
    public String toString() {
        return this.board + "\n" + this.turn;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((board == null) ? 0 : board.hashCode());
        result = prime * result + ((turn == null) ? 0 : turn.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Game other = (Game) obj;
        if (board == null) {
            if (other.board != null)
                return false;
        } else if (!board.equals(other.board))
            return false;
        if (turn == null) {
            if (other.turn != null)
                return false;
        } else if (!turn.equals(other.turn))
            return false;
        return true;
    }

}
