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
        List<Coordinate> removedCoordinatesBadMovement = new ArrayList<Coordinate>();
        do {
            error = this.isCorrectPairMove(pair, coordinates);
            if (error == null) {
                List<Coordinate> mv= this.getCoordinatesWithActualColor();
                if(removedCoordinates.size() ==0 ){
                    for (Coordinate coordinate : mv)
                        if(this.getPiece(coordinate)!= this.getPiece(coordinates[pair]))
                            this.badMovement(removedCoordinatesBadMovement, coordinate);
                }
                this.pairMove(removedCoordinates, pair, coordinates);
                if(removedCoordinates.size()==0 && removedCoordinatesBadMovement.size()>0 ){
                    int number = new Random().nextInt(removedCoordinatesBadMovement.size());;
                    removedCoordinates.add(0,removedCoordinatesBadMovement.get(number));//borra la actual, no for removing
                    this.board.remove(removedCoordinatesBadMovement.get(number));
                }
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

    private void badMovement(List<Coordinate> removedCoordinates, Coordinate coordinate) {
        if (this.isPossibleToEat(coordinate)) {
            removedCoordinates.add(0, coordinate);
        }
    }

    private boolean isPossibleToEat(Coordinate coordinate){
        Piece piece = this.getPiece(coordinate);
        if(piece.getCode() == "b" || piece.getCode() == "n") {
            List<Coordinate> coordinates = coordinate.getDiagonalCoordinates(2);
            for (Coordinate coor : coordinates) {
                if (this.isCorrectPairMove(0, coordinate, coor) == null) {
                    return true;
                }
            }
        }
        else if(piece.getCode().equals("B") || piece.getCode().equals("N")) {
            Draught d = new Draught(piece.getColor());
            List<Coordinate> coordinates = new ArrayList<Coordinate>();
            List<Coordinate> coordinates1 = coordinate.getDiagonalCoordinates(7);
            List<Coordinate> coordinates2 = coordinate.getDiagonalCoordinates(6);
            List<Coordinate> coordinates3 = coordinate.getDiagonalCoordinates(5);
            List<Coordinate> coordinates4 = coordinate.getDiagonalCoordinates(4);
            List<Coordinate> coordinates5 = coordinate.getDiagonalCoordinates(3);
            List<Coordinate> coordinates6 = coordinate.getDiagonalCoordinates(2);
            coordinates.add(coordinate);
            coordinates.addAll(coordinates1);
            coordinates.addAll(coordinates2);
            coordinates.addAll(coordinates3);
            coordinates.addAll(coordinates4);
            coordinates.addAll(coordinates5);
            coordinates.addAll(coordinates6);
            Coordinate[] myArray = new Coordinate[coordinates.size()];
            coordinates.toArray(myArray);
            List<Piece> betweenDiagonalPieces = this.board.getBetweenDiagonalPieces(myArray[0], myArray[1]);

            for(int i = 1; i < myArray.length; i++)
            {
                if (betweenDiagonalPieces.size()>0 && d.isCorrectDiagonalMovement(betweenDiagonalPieces.size(),0,myArray[i])==null) {
                    return true;
                }
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
