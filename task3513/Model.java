package com.javarush.task.task35.task3513;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private final Stack<Tile[][]> previousStates = new Stack<>();
    private final Stack<Integer> previousScores = new Stack<>();
    private Tile[][] gameTiles;
    protected int score = 0;
    protected int maxTile = 0;
    private boolean isSaveNeeded = true;
    public Model() {
        resetGameTiles();
    }

    protected void resetGameTiles() {
        clearGameTiles();
        addTile();
        addTile();
    }

    protected void clearGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
    }

    private void addTile() {
        List<Tile> emptyTitles = getEmptyTiles();

        if(! emptyTitles.isEmpty()) {
            emptyTitles.get((int) (emptyTitles.size() * Math.random())).value =
                    Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTitles = new ArrayList<>();
        for (Tile[] tiles : gameTiles) {
            for (Tile tile : tiles) {
                if (tile.isEmpty()) {
                    emptyTitles.add(tile);
                }
            }
        }
        return emptyTitles;
    }
    
    public void left() {
        if (isSaveNeeded) {
            saveState(getGameTiles());
        }
        boolean changed = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            changed = changed | compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i]);
        }
        if (changed) {
            addTile();
        }
        isSaveNeeded = true;
    }
    
    public void right() {
        saveState(getGameTiles());
        rotateGameTiles180();
        left();
        rotateGameTiles180();
    }
    
    public void up() {
        saveState(getGameTiles());
        rotateGameTiles270();
        left();
        rotateGameTiles90();
    }
    
    public void down() {
        saveState(getGameTiles());
        rotateGameTiles90();
        left();
        rotateGameTiles270();
    }

    private void rotateGameTiles90() {
        Tile[][] tilesCopy = gameTiles.clone();
        clearGameTiles();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = tilesCopy[FIELD_WIDTH - 1 - j][i];
            }
        }
    }

    private void rotateGameTiles180() {
        rotateGameTiles90();
        rotateGameTiles90();
    }

    private void rotateGameTiles270() {
        rotateGameTiles180();
        rotateGameTiles90();
    }

    private boolean compressTiles(Tile[] tiles) {
        Tile[] tilesCopy = tiles.clone();
        List<Tile> notEmptyTitlesList = new ArrayList<>();
        for (Tile tile : tiles) {
            if(! tile.isEmpty()) {
                notEmptyTitlesList.add(tile);
            }
        }

        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (! notEmptyTitlesList.isEmpty()) {
                tiles[i] = notEmptyTitlesList.remove(0);
            } else {
                tiles[i] = new Tile();
            }
        }

        return hasDifference(tilesCopy, tiles);
    }
    
    private boolean mergeTiles(Tile[] tiles) {
        Tile[] tilesCopy = tiles.clone();
        for (int i = 0; i < FIELD_WIDTH - 1; i++) {
            if (tiles[i].value == tiles[i + 1].value) {
                int addScore = tiles[i].value * 2;
                score += addScore;
                maxTile = Math.max(maxTile, addScore);
                tiles[i].value = addScore;
                tiles[i + 1] = new Tile();
                compressTiles(tiles);
            }
        }
        return hasDifference(tilesCopy, tiles);
    }

    public boolean canMove() {
        for(int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH - 1; j++) {
                if (gameTiles[i][j].value == 0 ||
                        gameTiles[i][j].value == gameTiles[i][j + 1].value) {
                    return true;
                }
            }
        }
        for(int j = 0; j < FIELD_WIDTH; j++) {
            for (int i = 0; i < FIELD_WIDTH - 1; i++) {
                if (gameTiles[i][j].value == 0 ||
                        gameTiles[i][j].value == gameTiles[i + 1][j].value) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasDifference(Tile[] begin, Tile[] end) {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (begin[i].value != end[i].value) {
                return true;
            }
        }
        return false;
    }

    private void saveState(Tile[][] tiles) {
        Tile[][] copy = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                copy[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(copy);
        previousScores.push(score);
        isSaveNeeded = false;
    }
    
    public void rollback() {
        if (previousStates.isEmpty() || previousScores.isEmpty()) return;
        gameTiles = previousStates.pop();
        score = previousScores.pop();
    }
    
    public void randomMove() {
        switch ((int) (Math.random() * 4)) {
            case 0 :
                left();
                break;
            case 1 :
                right();
                break;
            case 2 :
                up();
                break;
            case 3 :
                down();
                break;
        }
    }

    private boolean hasBoardChanged() {
        Tile[][] prevTiles = previousStates.peek();
        for(int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value != prevTiles[i][j].value) {
                    return true;
                }
            }
        }
        return false;
    }

    private MoveEfficiency getMoveEfficiency(Move move) {
        move.move();
        if (!hasBoardChanged()) {
            rollback();
            return new MoveEfficiency(-1, 0, move);
        }
        
        MoveEfficiency moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);

        rollback();
        return moveEfficiency;
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4, Collections.reverseOrder());
        priorityQueue.add(getMoveEfficiency(this::left));
        priorityQueue.add(getMoveEfficiency(this::right));
        priorityQueue.add(getMoveEfficiency(this::up));
        priorityQueue.add(getMoveEfficiency(this::down));

        priorityQueue.peek().getMove().move();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

}
