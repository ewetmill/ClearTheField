package com.wetsts.ctf;

/**
 * Created by Eric on 8/27/2016.
 */
public class CellEvent {
    int row;
    int column;
    int adjacentCells;
    GridCell object;

    public CellEvent(int row, int column, int adjacentCells, GridCell object) {
        this.object = object;
        this.row = row;
        this.column = column;
        this.adjacentCells = adjacentCells;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getAdjacentCells() {
        return adjacentCells;
    }

    public GridCell.State getState() {
        return object.getState();
    }

    public GridCell getObject() {
        return object;
    }
}
