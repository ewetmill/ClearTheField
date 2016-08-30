package com.wetsts.ctf;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
    // TODO Clear the field when the game is lost
    // TODO Make icons better
    // TODO Add Top Times
    // TODO Add Expert Mode when turned Landscape
    // TODO Save state when rotated

    Game game;
    Handler timer;
    int time = 0;

    Button[][] buttons = new Button[10][10];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        game = new Game();
        timer = new Handler();
        game.setCellListener(this);

        TableLayout fieldLayout = (TableLayout) findViewById(R.id.fieldLayout);
        for(int row = 0; row < 10; row++) {
            TableRow rowWidget = new TableRow(this);
            for (int col = 0; col < 10; col++) {
                Button cell = new Button(this);
                buttons[row][col] = cell;

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

                final int x = row;
                final int y = col;
                cell.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Button button = (Button) v;
                        game.toggleFlag(x, y);

                        TextView mineCountView = (TextView) findViewById(R.id.bombsView);
                        mineCountView.setText(Integer.toString(game.getRemainingMines()));
                        return true;
                    }
                });

                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button button = (Button) v;
                        Game.State state = game.inspect(x,y);
                        if (state == Game.State.DEAD) {
                            ImageButton resetButton = (ImageButton) findViewById(R.id.resetButton);
                            resetButton.setImageResource(R.drawable.sad_face_icon);
                        }
                        else if (state == Game.State.CLEARED) {
                            ImageButton resetButton = (ImageButton) findViewById(R.id.resetButton);
                            resetButton.setImageResource(R.drawable.win_face_icon);
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
                buttons[row][col].setBackgroundResource(R.drawable.cleared_cell);
                int adjCells = event.getAdjacentCells();
                if (adjCells > 0) {
                    buttons[row][col].setTextColor(getAdjMinesColor(adjCells));
                    buttons[row][col].setTextSize(16);
                    buttons[row][col].setText(Integer.toString(adjCells));
                }
                break;
            case FLAGGED:
                buttons[row][col].setBackgroundResource(R.drawable.flag_icon);
                break;
            case DETONATED:
                buttons[row][col].setBackgroundResource(R.drawable.boom_bomb_icon);
                break;
            case HIDDEN:
                buttons[row][col].setBackgroundResource(R.drawable.hidden_cell);
                buttons[row][col].setText("");
                break;
            default:
                break;
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
