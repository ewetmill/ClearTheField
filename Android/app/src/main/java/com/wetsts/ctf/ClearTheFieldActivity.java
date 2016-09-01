package com.wetsts.ctf;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ClearTheFieldActivity extends AppCompatActivity implements CellListener {
    // TODO Make icons better
    // TODO Add Top Times
    // TODO Save state when rotated
    // TODO Make application Icon

    Game game;
    Handler timer;
    int time = 0;

    Button[][] buttons;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            game = new Game();
        }
        else {
            game = new Game(6, 15, 15);
        }
        buttons = new Button[game.NUM_COLUMNS][game.NUM_ROWS];
        timer = new Handler();
        game.setCellListener(this);

        TableLayout fieldLayout = (TableLayout) findViewById(R.id.fieldLayout);
        for(int row = 0; row < game.NUM_ROWS; row++) {
            TableRow rowWidget = new TableRow(this);
            for (int col = 0; col < game.NUM_COLUMNS; col++) {
                Button cell = new Button(this);
                buttons[col][row] = cell;

                cell.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (game.getState() == Game.State.PLAYING ||
                                game.getState() == Game.State.WAITING ) {
                            ImageButton resetButton = (ImageButton) findViewById(R.id.resetButton);
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                resetButton.setImageResource(R.drawable.scared_face_icon);
                            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                                    event.getAction() == MotionEvent.ACTION_MOVE) {
                                resetButton.setImageResource(R.drawable.happy_face_icon);
                            }
                        }
                        return false;
                    }
                });

                final int r = row;
                final int c = col;
                cell.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Button button = (Button) v;
                        game.toggleFlag(r, c);

                        TextView mineCountView = (TextView) findViewById(R.id.bombsView);
                        mineCountView.setText(Integer.toString(game.getRemainingMines()));
                        return true;
                    }
                });

                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button button = (Button) v;
                        Game.State state = game.inspect(r,c);
                        if (state == Game.State.DEAD) {
                            ImageButton resetButton = (ImageButton) findViewById(R.id.resetButton);
                            resetButton.setImageResource(R.drawable.sad_face_icon);
                        }
                        else if (state == Game.State.CLEARED) {
                            ImageButton resetButton = (ImageButton) findViewById(R.id.resetButton);
                            resetButton.setImageResource(R.drawable.win_face_icon);
                            endGame();
                        }
                    }
                });

                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
                cell.setLayoutParams(params);
                cell.setBackgroundResource(R.drawable.hidden_cell);
                rowWidget.addView(cell);
            }
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = -6;
            rowWidget.setLayoutParams(params);
            fieldLayout.addView(rowWidget);
        }

        Runnable counter = new Runnable() {
            @Override
            public void run() {
                if (game.getState() == Game.State.PLAYING) {
                    time++;
                    TextView timeView = (TextView) findViewById(R.id.timerView);
                    timeView.setText(Integer.toString(time));
                }
                timer.postDelayed(this, 1000);
            }
        };
        timer.postDelayed(counter, 1000);
        fieldLayout.requestLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    final Handler handler = new Handler();

    public void reset(View view)
    {
        game.reset();
        time = 0;
        ImageButton resetButton = (ImageButton) findViewById(R.id.resetButton);
        resetButton.setImageResource(R.drawable.happy_face_icon);
        TextView timeView = (TextView) findViewById(R.id.timerView);
        timeView.setText("000");
    }

    @Override
    public void cellChanged(CellEvent event) {
            int row = event.getRow();
            int col = event.getColumn();
            switch (event.getState()) {
                case CLEARED:
                    buttons[col][row].setBackgroundResource(R.drawable.cleared_cell);
                    int adjCells = event.getAdjacentCells();
                    if (adjCells > 0) {
                        buttons[col][row].setTextColor(getAdjMinesColor(adjCells));
                        buttons[col][row].setTextSize(16);
                        buttons[col][row].setText(Integer.toString(adjCells));
                    }
                    break;
                case FLAGGED:
                    buttons[col][row].setPadding(20,0,0,0);
                    buttons[col][row].setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.flag_icon,0, 0, 0);
                    //buttons[col][row].setBackgroundResource(R.drawable.flag_icon);
                    break;
                case DETONATED:
                    endGame();
                    break;
                case HIDDEN:
                    buttons[col][row].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    buttons[col][row].setBackgroundResource(R.drawable.hidden_cell);
                    buttons[col][row].setText("");
                    break;
                default:
                    break;
            }
    }

    private void endGame() {
        for (int row = 0 ; row < game.NUM_ROWS; row++) {
            for (int col = 0; col < game.NUM_COLUMNS; col++) {
                GridCell.State state = game.getCellState(row, col);
                switch (state) {
                    case DETONATED:
                        buttons[col][row].setBackgroundResource(R.drawable.cleared_cell);
                        buttons[col][row].setPadding(20,0,0,0);
                        buttons[col][row].setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.boom_bomb_icon,0, 0, 0);
                        break;
                    case FLAGGED:
                        if (!game.hasMine(row, col)) {
                            buttons[col][row].setPadding(20,0,0,0);
                            buttons[col][row].setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.wrong_flag_icon,0, 0, 0);
                        }
                        break;
                    case HIDDEN:
                        if (game.hasMine(row, col)) {
                            buttons[col][row].setBackgroundResource(R.drawable.cleared_cell);
                            buttons[col][row].setPadding(20,0,0,0);
                            buttons[col][row].setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.bomb_icon,0, 0, 0);
                        }
                        else {
                            int adjMines = game.getAdjMines(row, col);
                            if (adjMines > 0) {
                                buttons[col][row].setTextColor(getAdjMinesColor(adjMines));
                                buttons[col][row].setTextSize(16);
                                buttons[col][row].setText(Integer.toString(adjMines));
                            }
                            buttons[col][row].setBackgroundResource(R.drawable.cleared_cell);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private int getAdjMinesColor(int adjMines) {

        int result = Color.BLACK;
        switch (adjMines)
        {
            case 1:
                result = Color.BLUE;
                break;
            case 2:
                result = Color.RED;
                break;
            case 3:
                result = Color.MAGENTA;
                break;
            case 4:
                result = Color.GREEN;
                break;
            case 5:
                result = Color.CYAN;
                break;
            case 6:
                result = Color.rgb(255, 200, 0);
                break;
            case 7:
                result = Color.rgb(255, 175, 175);
                break;
            case 8:
                result = Color.GRAY;
                break;
        }
        return result;
    }
}
