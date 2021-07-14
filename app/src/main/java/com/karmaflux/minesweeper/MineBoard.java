package com.karmaflux.minesweeper;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

class MineBoard implements Serializable {
    private final CellState[][] cells;
    private final int[][] adjacentBombs;
    private final int sizeX;
    private final int sizeY;

    MineBoard(int sizeX, int sizeY, float difficulty) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.cells = new CellState[sizeX][sizeY];

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                cells[x][y] = CellState.COVERED;
            }
        }

        final Random rng = new Random();
        final int numBombs = estimateBombs(sizeX, sizeY, difficulty);
        int bombsPlaced = 0;
        do {
            int bombX = rng.nextInt(sizeX);
            int bombY = rng.nextInt(sizeY);
            if (cells[bombX][bombY] == CellState.COVERED) {
                cells[bombX][bombY] = CellState.COVERED_BOMB;
                bombsPlaced++;
            }
        } while (bombsPlaced < numBombs);

        adjacentBombs = new int[sizeX][sizeY];
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                adjacentBombs[x][y] = numOfAdjacentBombs(x, y);
            }
        }
    }

    private int numOfAdjacentBombs(int ox, int oy) {
        final int left = Math.max(0, ox - 1);
        final int right = Math.min(sizeX - 1, ox + 1);
        final int top = Math.max(0, oy - 1);
        final int bottom = Math.min(sizeY - 1, oy + 1);
        int result = 0;

        for (int x = left; x <= right; x++) {
            for (int y = top; y <= bottom; y++) {
                if (isBomb(x, y)) {
                    result++;
                }
            }
        }

        if (isBomb(ox, oy)) {
            result--; // don't count center cell
        }

        return result;
    }

    public static int estimateBombs(int sizeX, int sizeY, float difficulty) {
        return (int) (sizeX * sizeY * (difficulty * 0.75f + 0.05f));
    }

    CellState getCellState(int x, int y) {
        return cells[x][y];
    }

    int getAdjacentBombs(int x, int y) {
        return adjacentBombs[x][y];
    }

    int getSizeX() {
        return sizeX;
    }

    int getSizeY() {
        return sizeY;
    }

    /**
     * Uncovers the cell at x/y coordinates
     *
     * @return True if uncovered a bomb, false otherwise
     */
    public boolean dig(int x, int y) {
        switch (cells[x][y]) {
            case COVERED:
                cells[x][y] = CellState.UNCOVERED;
                uncoverNeighbors(x, y);
                return false;
            case COVERED_BOMB:
                cells[x][y] = CellState.BOMB;
                return true;
            default:
                Log.e("MineBoard", "dig triggered on unknown cell type " + cells[x][y]);
                return false;
        }
    }

    private void uncoverNeighbors(int x, int y) {
        if (adjacentBombs[x][y] == 0) {
            // flood fill and open the "empty" zero cells
            boolean[][] painted = new boolean[sizeX][sizeY];
            Queue<Point> q = new LinkedList<>();
            q.add(new Point(x, y));
            while (!q.isEmpty()) {
                Point p = q.remove();
                if (p.x < 0 || p.x >= sizeX || p.y < 0 || p.y >= sizeY || painted[p.x][p.y]) {
                    continue;
                }
                painted[p.x][p.y] = true;
                if (!isBomb(p.x, p.y)) {
                    cells[p.x][p.y] = CellState.UNCOVERED;
                    if (adjacentBombs[p.x][p.y] == 0) {
                        q.add(new Point(p.x + 1, p.y));
                        q.add(new Point(p.x - 1, p.y));
                        q.add(new Point(p.x, p.y + 1));
                        q.add(new Point(p.x, p.y - 1));
                        q.add(new Point(p.x + 1, p.y + 1));
                        q.add(new Point(p.x - 1, p.y - 1));
                        q.add(new Point(p.x - 1, p.y + 1));
                        q.add(new Point(p.x + 1, p.y - 1));
                    }
                }
            }
        }
    }

    /**
     * Uncovers a cell for "free"
     */
    void freeDig() {
        int minDig = 9;
        int minX = 0;
        int minY = 0;
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                if (!isBomb(i, j) && adjacentBombs[i][j] < minDig) {
                    minDig = adjacentBombs[i][j];
                    minX = i;
                    minY = j;
                }
            }
        }
        if (minDig < 9) {
            dig(minX, minY);
        }
    }

    boolean checkWin() {
        int uncovered = 0;
        int bombs = 0;
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                if (cells[i][j] == CellState.UNCOVERED) {
                    uncovered++;
                } else if (isBomb(i, j)) {
                    bombs++;
                }
            }
        }

        return sizeX * sizeY == uncovered + bombs;
    }

    void flag(int x, int y) {
        switch (cells[x][y]) {
            case COVERED:
                cells[x][y] = CellState.FLAGGED;
                break;
            case COVERED_BOMB:
                cells[x][y] = CellState.FLAGGED_BOMB;
                break;
            case FLAGGED:
                cells[x][y] = CellState.COVERED;
                break;
            case FLAGGED_BOMB:
                cells[x][y] = CellState.COVERED_BOMB;
                break;
        }
    }

    private boolean isBomb(int x, int y) {
        return cells[x][y] == CellState.BOMB ||
                cells[x][y] == CellState.FLAGGED_BOMB ||
                cells[x][y] == CellState.COVERED_BOMB;
    }
}
