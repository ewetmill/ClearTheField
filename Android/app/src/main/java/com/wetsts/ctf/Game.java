package com.wetsts.ctf;

import android.util.Log;

/**
 * Created by ewetmill on 8/26/16.
 */
public class Game {
    public final int NUM_ROWS;
    public final int NUM_COLUMNS;
    public final int TOTAL_MINES;

    public enum State {
        WAITING,
        PLAYING,
        DEAD,
        CLEARED
    }

    private State state = State.WAITING;
    private int flaggedMines = 0;
    private CellListener listener;

    private final GridCell[][] cells;
    private int[][] adjacentMines;

    public Game(){
        this(10,10,10);
    }

    public Game(int rows, int columns, int totalMines){
        NUM_ROWS = rows;
        NUM_COLUMNS = columns;
        TOTAL_MINES = totalMines;
        cells = new GridCell[NUM_COLUMNS][NUM_ROWS];
        boolean[][] mines = scatterMines(rows, columns);
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                cells[col][row] = new GridCell(mines[col][row]);
            }
        }
        adjacentMines = calcAdjMines();
    }

    public void reset() {
        state = State.WAITING;
        flaggedMines = 0;
        boolean[][] mines = scatterMines(NUM_ROWS, NUM_COLUMNS);
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                cells[col][row] = new GridCell(mines[col][row]);
            }
        }
        adjacentMines = calcAdjMines();

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                CellEvent event = new CellEvent(row, col, adjacentMines[col][row], cells[col][row]);
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
                row >= 0 && row < NUM_ROWS &&
                column >= 0 && column < NUM_COLUMNS ) {
            if (state == State.WAITING) state = State.PLAYING;

            GridCell.State previousState = cells[column][row].getState();
            if (clicked && previousState == GridCell.State.CLEARED) {
                clearBlankBoxes(row, column);
            }
            else {
                if (previousState != GridCell.State.FLAGGED) {
                    GridCell.State cellState = cells[column][row].inspect();

                    checkCellState(row, column, previousState, cells[column][row]);
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
        return adjacentMines[column][row] == 0 && !cells[column][row].hasMine();
    }

    public int getAdjMines(int row, int column) {
        return adjacentMines[column][row];
    }

    public GridCell.State getCellState(int row, int column) {
        return cells[column][row].getState();
    }

    public boolean hasMine(int row, int column) {
        return cells[column][row].hasMine();
    }

    public void toggleFlag(int row, int column) {
        if (state != State.DEAD && state != State.CLEARED) {
            GridCell.State previousState = cells[column][row].getState();

            if (previousState == GridCell.State.HIDDEN) {
                cells[column][row].dropFlag();
                flaggedMines++;
            } else if (previousState == GridCell.State.FLAGGED) {
                cells[column][row].clearFlag();
                flaggedMines--;
            }
            checkCellState(row, column, previousState, cells[column][row]);
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
            CellEvent event = new CellEvent(row, column, adjacentMines[column][row], cell);
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
        for( int row = 0; row < NUM_ROWS; row++ )
        {
            for( int column = 0; column < NUM_COLUMNS; column++ )
            {
                if( cells[column][row].getState() != GridCell.State.CLEARED &&
                        !cells[column][row].hasMine() )
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
        int[][] adjMines = new int[NUM_COLUMNS][NUM_ROWS];
        for( int row = 0; row < NUM_ROWS; row++ )
        {
            for( int col = 0; col < NUM_COLUMNS; col++ )
            {
                int numMines = 0;
                if( !cells[col][row].hasMine() )
                {
                    if( (col - 1) >= 0 &&
                            ( row - 1 ) >= 0 &&
                            cells[col - 1][row - 1].hasMine() )
                        numMines++;
                    if( ( row - 1 ) >= 0 && cells[col][row - 1].hasMine() )
                        numMines++;
                    if( (col + 1) < NUM_COLUMNS &&
                            ( row - 1 ) >= 0 &&
                            cells[col+1][row - 1].hasMine())
                        numMines++;
                    if( (col + 1) < NUM_COLUMNS &&
                            cells[col+1][row].hasMine())
                        numMines++;
                    if( (col + 1) < NUM_COLUMNS &&
                            ( row + 1 ) < NUM_ROWS &&
                            cells[col+1][row+1].hasMine())
                        numMines++;
                    if( ( row + 1 ) < NUM_ROWS &&
                            cells[col][row+1].hasMine())
                        numMines++;
                    if( (col - 1) >= 0 &&
                            ( row + 1 ) < NUM_ROWS &&
                            cells[col-1][row+1].hasMine())
                        numMines++;
                    if( (col - 1) >= 0 &&
                            cells[col-1][row].hasMine())
                        numMines++;
                }
                else {
                    numMines = 44;
                }

                adjMines[col][row]=numMines;
            }
        }
        return adjMines;
    }

    private boolean[][] scatterMines(int rows, int columns) {
        boolean[][] result = new boolean[columns][rows];

        for (int mines = 0; mines < TOTAL_MINES; mines++) {
            int row = (int)(rows * Math.random());
            int column = (int)(columns * Math.random());

            while( result[column][row])
            {
                row = (int)(rows * Math.random());
                column = (int)(columns * Math.random());
            }
            result[column][row] = true;
        }

        return result;
    }
}
