package com.wetsts.ctf;

/**
 * Created by ewetmill on 8/26/16.
 */
public class GridCell {
    public enum State {
        CLEARED,
        DETONATED,
        HIDDEN,
        FLAGGED
    }

    public GridCell(boolean hasMine) {
        this.hasMine = hasMine;
    }

    private State state = State.HIDDEN;
    private boolean hasMine;

    public State inspect() {
        if (hasMine) {
            state = State.DETONATED;
        }
        else if (state != State.FLAGGED) {
            state = State.CLEARED;
        }
        return state;
    }

    public void dropFlag() {
        state = State.FLAGGED;
    }

    public void clearFlag() {
        state = State.HIDDEN;
    }

    public boolean hasMine() {
        return hasMine;
    }

    public State getState() {
        return state;
    }
}
