package com.wetsts.ctf;

import android.util.Log;

/**
 * Created by ewetmill on 8/26/16.
 */
public class Game {
    private static final int NUM_ROWS = 10;
    private static final int NUM_COLUMNS = 10;
    private static final int TOTAL_MINES = 10;

    public enum State {
        WAITING,
        PLAYING,
        DEAD,
        CLEARED
    }

    private State state = State.WAITING;
    private int flaggedMines = 0;
    private CellListener listener;

    private GridCell[][] cells = new GridCell[NUM_ROWS][NUM_COLUMNS];
    private int[][] adjacentMines;

    public Game(){
        boolean[][] mines = scatterMines();
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                cells[row][col] = new GridCell(mines[row][col]);
            }
        }
        adjacentMines = calcAdjMines();
    }

    public void reset() {
        state = State.WAITING;
        flaggedMines = 0;
        boolean[][] mines = scatterMines();
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                cells[row][col] = new GridCell(mines[row][col]);
            }
        }
        adjacentMines = calcAdjMines();

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                CellEvent event = new CellEvent(row, col, adjacentMines[row][col], cells[row][col]);
                notify(event);
            }
        }
    }

    public State getState() {
        return state;
    }

    public State inspect(int row, int column) {
        return clear(row, column, true);
    }

    private State clear(int row, int column, boolean clicked) {
        if (state != State.DEAD  &&
                row >= 0 && row < NUM_COLUMNS &&
                column >= 0 && column < NUM_ROWS ) {
            if (state == State.WAITING) state = State.PLAYING;

            GridCell.State previousState = cells[row][column].getState();
            if (clicked && previousState == GridCell.State.CLEARED) {
                clearBlankBoxes(row, column);
            }
            else {
                if (previousState != GridCell.State.FLAGGED) {
                    GridCell.State cellState = cells[row][column].inspect();

                    checkCellState(row, column, previousState, cells[row][column]);
                    switch (cellState) {
                        case CLEARED:
                            if (//!clicked &&
                                    previousState != GridCell.State.CLEARED &&
                                            isCellBlank(row, column)) {
                                clearBlankBoxes(row, column);
                            }
                            break;
                        case DETONATED:
                            state = State.DEAD;
                            break;
                        default:
                            break;
                    }
                }
            }

            if (hasWon()) {
                state = State.CLEARED;
            }
        }
        return state;
    }

    private boolean isCellBlank(int row, int column) {
        return adjacentMines[row][column] == 0 && !cells[row][column].hasMine();
    }

    public void toggleFlag(int row, int column) {
        if (state != State.DEAD && state != State.CLEARED) {
            GridCell.State previousState = cells[row][column].getState();

            if (previousState == GridCell.State.HIDDEN) {
                cells[row][column].dropFlag();
                flaggedMines++;
            } else if (previousState == GridCell.State.FLAGGED) {
                cells[row][column].clearFlag();
                flaggedMines--;
            }
            checkCellState(row, column, previousState, cells[row][column]);
        }
    }

    public int getRemainingMines() {
        return TOTAL_MINES - flaggedMines;
    }

    public void setCellListener(CellListener listener) {
        this.listener = listener;
    }

    private void checkCellState(int row, int column, GridCell.State previousState, GridCell cell) {
        if (previousState != cell.getState()) {
            CellEvent event = new CellEvent(row, column, adjacentMines[row][column], cell);
            notify(event);
        }
    }

    private void notify(CellEvent event) {
        if (listener != null) {
            listener.cellChanged(event);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * clearBlankBoxes
     */
    //-------------------------------------------------------------------------
    private State clearBlankBoxes( int x, int y )
    {
        Log.i("clearBlankBoxes", "Row:" + x + " Col:" +y);
        if (clear( x+1, y, false   ) == State.DEAD ||
                clear( x-1, y, false   ) == State.DEAD ||

                clear( x  , y-1, false ) == State.DEAD ||
                clear( x+1, y-1, false ) == State.DEAD ||
                clear( x-1, y-1, false ) == State.DEAD ||

                clear( x  , y+1, false ) == State.DEAD ||
                clear( x+1, y+1, false ) == State.DEAD ||
                clear( x-1, y+1, false ) == State.DEAD) {
            state = State.DEAD;
        }
        return state;
    }

    //-------------------------------------------------------------------------
    /**
     * hasWon
     */
    //-------------------------------------------------------------------------
    private boolean hasWon()
    {
        boolean result = true;
        for( int h = 0; h < NUM_ROWS; h++ )
        {
            for( int w = 0; w < NUM_COLUMNS; w++ )
            {
                if( cells[w][h].getState() != GridCell.State.CLEARED &&
                        !cells[w][h].hasMine() )
                    result = false;
            }
        }

        return result;
    }


    //-------------------------------------------------------------------------
    /**
     * calcAdjMines
     */
    //-------------------------------------------------------------------------
    private int[][] calcAdjMines()
    {
        int[][] adjMines = new int[NUM_ROWS][NUM_COLUMNS];
        for( int h = 0; h < NUM_ROWS; h++ )
        {
            for( int w = 0; w < NUM_COLUMNS; w++ )
            {
                int numMines = 0;
                if( !cells[w][h].hasMine() )
                {
                    if( (w - 1) >= 0 &&
                            ( h - 1 ) >= 0 &&
                            cells[w - 1][h - 1].hasMine() )
                        numMines++;
                    if( ( h - 1 ) >= 0 && cells[w][h - 1].hasMine() )
                        numMines++;
                    if( (w + 1) < NUM_COLUMNS &&
                            ( h - 1 ) >= 0 &&
                            cells[w+1][h - 1].hasMine())
                        numMines++;
                    if( (w + 1) < NUM_COLUMNS &&
                            cells[w+1][h].hasMine())
                        numMines++;
                    if( (w + 1) < NUM_COLUMNS &&
                            ( h + 1 ) < NUM_ROWS &&
                            cells[w+1][h+1].hasMine())
                        numMines++;
                    if( ( h + 1 ) < NUM_ROWS &&
                            cells[w][h+1].hasMine())
                        numMines++;
                    if( (w - 1) >= 0 &&
                            ( h + 1 ) < NUM_ROWS &&
                            cells[w-1][h+1].hasMine())
                        numMines++;
                    if( (w - 1) >= 0 &&
                            cells[w-1][h].hasMine())
                        numMines++;
                }
                else {
                    numMines = 44;
                }

                adjMines[w][h]=numMines;
            }
        }
        return adjMines;
    }

    private static boolean[][] scatterMines() {
        boolean[][] result = new boolean[NUM_ROWS][NUM_COLUMNS];

        for (int mines = 0; mines < TOTAL_MINES; mines++) {
            int row = (int)(NUM_COLUMNS * Math.random());
            int column = (int)(NUM_ROWS * Math.random());

            while( result[row][column])
            {
                row = (int)(NUM_COLUMNS * Math.random());
                column = (int)(NUM_ROWS * Math.random());
            }
            result[row][column] = true;
        }

        return result;
    }
}
