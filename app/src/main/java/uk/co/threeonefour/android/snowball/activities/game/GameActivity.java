/**
 * Copyright 2014 Paul Illingworth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.threeonefour.android.snowball.activities.game;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.threeonefour.android.snowball.R;
import uk.co.threeonefour.android.snowball.activities.game.OutputFormatter.WordClickedHandler;
import uk.co.threeonefour.android.snowball.activities.game.TextInputView.TextInputHandler;
import uk.co.threeonefour.android.snowball.activities.game.graphics.GraphicsCommand;
import uk.co.threeonefour.android.snowball.activities.game.graphics.GraphicsContext;
import uk.co.threeonefour.android.snowball.activities.game.graphics.QueuedGraphicsHandler;
import uk.co.threeonefour.android.snowball.basics.io.IOUtils;
import uk.co.threeonefour.android.snowball.gamestate.FileGameStateDao;
import uk.co.threeonefour.android.snowball.gamestate.GameStateDao;
import uk.co.threeonefour.android.snowball.gamestate.GameStateSummary;
import uk.co.threeonefour.android.snowball.gamestate.IntentConstants;
import uk.co.threeonefour.android.snowball.gamestate.PersistableGameStateFactory;
import uk.co.threeonefour.android.snowball.level9j.icy.Icy;
import uk.co.threeonefour.android.snowball.level9j.icy.IcyVm;
import uk.co.threeonefour.android.snowball.level9j.vm.FileHandler;
import uk.co.threeonefour.android.snowball.level9j.vm.GameState;
import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends ActionBarActivity implements WordClickedHandler, TextInputHandler {

    private static final String RAM_SAVE_SLOTS_STATE = "ramSaveSlotsState";
    private static final String IMAGE_STATE = "imageState";
    private static final String GAME_STATE = "gameState";
    private static final String GAME_STATE_SLOT = "gameStateSlot";

    private final GameStateDao gameStateDao;

    private AsyncTask<Void, Integer, Boolean> createTask;
    private boolean initialised;

    private GraphicsView imageView;
    private TextInputView textInputView;
    private TextView textView;
    private ScrollView textScrollView;
    private ProgressBar progressBar;
    private GestureDetectorCompat gestureDetector;
    private GraphicsTask graphicsTask;

    private QueuedGraphicsHandler graphicsHandler;
    private GraphicsContext graphicsHandlerContext;
    private Icy level9;
    private RamSave[] ramSaveSlots;

    private OutputFormatter formatter;

    private int gameStateSlot;
    private String lastCommand;
    private String lastLocation;

    GameState restoredGameState;
    RamSave[] restoredRamSaveSlots;
    Bitmap restoredBitmap;
    int restoredGameStateSlot;

    private static final Set<String> newLocationCommands = new HashSet<String>();
    static {
        newLocationCommands.addAll(Arrays.asList(new String[] { "north", "n", "south", "s", "east", "e", "west", "w",
                "up", "u", "down", "d", "out", "in", "northeast", "ne", "northwest", "nw", "southeast", "se",
                "southwest", "sw", "look" }));
    }

    public GameActivity() {
        gameStateDao = new FileGameStateDao(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        imageView = (GraphicsView) findViewById(R.id.view_game_image);
        textInputView = (TextInputView) findViewById(R.id.view_game_text_input);
        textView = (TextView) findViewById(R.id.view_game_text_output);
        textScrollView = (ScrollView) findViewById(R.id.view_game_text_output_scroll);
        progressBar = (ProgressBar) findViewById(R.id.view_game_progressbar);
        imageView.setVisibility(View.INVISIBLE);
        textInputView.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        textScrollView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        textInputView.setTextInputHandler(this);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        formatter = new OutputFormatter();
        formatter.setWordClickedHandler(this);
        formatter.setTextColor(textView.getCurrentTextColor());
        formatter.setHighlightDirections(true);
        formatter.setClickableText(true);

        gestureDetector = new GestureDetectorCompat(this, new MyGestureListener());

        graphicsHandler = new QueuedGraphicsHandler();
        graphicsHandlerContext = new GraphicsContext();

        level9 = new IcyVm(graphicsHandler, new MyFilehandler(), new PersistableGameStateFactory());

        createTask = new AsyncTask<Void, Integer, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... arg0) {

                try {
                    AssetManager assetManager = getAssets();
                    InputStream in = assetManager.open("games/SNOWBALL.SNA");
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    IOUtils.copy(in, out);
                    level9.loadGame(out.toByteArray());
                    level9.startGame();
                } catch (IOException e) {
                    Log.e("game", "Failed to load file", e);
                }

                /* configure the selected game state */
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    gameStateSlot = extras.getInt(IntentConstants.GAME_STATE_SLOT, -1);
                }
                if (gameStateSlot == -1) {
                    // TODO should never get here without a slot selected; use dao to get default slot id
                    gameStateSlot = 0;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {

                imageView.setVisibility(View.VISIBLE);
                textInputView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textScrollView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                GameState gameState = gameStateDao.loadGameState(gameStateSlot);
                GameStateSummary gameStateSummary = gameStateDao.loadGameStateSummary(gameStateSlot);
                if (gameState != null && gameStateSummary != null) {
                    level9.setGameState(gameState);
                    graphicsHandlerContext.setBitmap(gameStateDao.loadGraphics(gameStateSummary.getSlot()));
                    imageView.setBitmap(graphicsHandlerContext.getBitmap());
                    textView.setText("");
                    processOutputText(gameStateSummary.getLastLocation());
                    // TODO hacky adding in a sneaky look because VM is not in the correct state for some reason
                    level9.execute("look");

                    // TODO had to add this after putting into background task
                    imageView.invalidate();
                } else {
                    String text = level9.getText().trim();
                    processOutput(text);
                }

                /* did we get a restore whilst we were initialising? */
                if (restoredGameState != null) {
                    level9.setGameState(restoredGameState);
                    ramSaveSlots = restoredRamSaveSlots;
                    imageView.setBitmap(restoredBitmap);
                    gameStateSlot = restoredGameStateSlot;

                    restoredGameState = null;
                    restoredRamSaveSlots = null;
                    restoredBitmap = null;
                    restoredGameStateSlot = 0;
                }

                initialised = true;
            }

        };
        createTask.execute((Void) null);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();
        if (graphicsTask != null) {
            graphicsTask.cancel(false);
            graphicsTask = null;
            graphicsHandlerContext = new GraphicsContext(graphicsHandlerContext);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        createTask.cancel(false);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        if (initialised) {
            savedInstanceState.putParcelable(GAME_STATE, (Parcelable) level9.getGameState());
            savedInstanceState.putParcelableArray(RAM_SAVE_SLOTS_STATE, ramSaveSlots);
            savedInstanceState.putParcelable(IMAGE_STATE, imageView.getBitmap());
            savedInstanceState.putInt(GAME_STATE_SLOT, gameStateSlot);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle restoreInstanceState) {

        if (initialised) {
            level9.setGameState((GameState) restoreInstanceState.getParcelable(GAME_STATE));
            ramSaveSlots = (RamSave[]) restoreInstanceState.getParcelableArray(RAM_SAVE_SLOTS_STATE);
            imageView.setBitmap((Bitmap) restoreInstanceState.getParcelable(IMAGE_STATE));
            gameStateSlot = restoreInstanceState.getInt(GAME_STATE_SLOT);
        } else {
            restoredGameState = (GameState) restoreInstanceState.getParcelable(GAME_STATE);
            restoredRamSaveSlots = (RamSave[]) restoreInstanceState.getParcelableArray(RAM_SAVE_SLOTS_STATE);
            restoredBitmap = (Bitmap) restoreInstanceState.getParcelable(IMAGE_STATE);
            restoredGameStateSlot = restoreInstanceState.getInt(GAME_STATE_SLOT);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onRestoreInstanceState(restoreInstanceState);
    }

    @Override
    public void onBackPressed() {

        if (initialised) {
            saveGame(false);
        }

        super.onBackPressed();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void processCommand(String command) {

        if (!initialised) {
            return;
        }

        SpannableString styledCommand = new SpannableString(command);
        styledCommand.setSpan(new StyleSpan(Typeface.BOLD), 0, styledCommand.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.append(styledCommand);
        textView.append("\n");

        lastCommand = command;
        /* pre: intercept some commands like RESTORE */
        boolean continueExecute = true;
        if ("restore".equalsIgnoreCase(command)) {
            loadGame();
            continueExecute = false;
        }

        if (continueExecute) {
            /* execute command */
            level9.execute(command);
            String text = level9.getText().trim();

            /* post: intercept some commands */

            processOutput(text);
        }
    }

    private void processOutput(String text) {
        processOutputText(text);
        processOutputGraphics();
    }

    private void processOutputText(String text) {

        List<SpannableString> styledLines = formatter.format(text);

        StringBuilder sb = new StringBuilder();
        for (SpannableString styledLine : styledLines) {
            sb.append(styledLine.toString());
            sb.append(" ");
            textView.append(styledLine);
            textView.append("\n");
        }

        if ((lastCommand == null || isNewLocation(lastCommand)) && !isBumpedInto(text)) {
            lastLocation = sb.toString();
        }

        textScrollView.fullScroll(View.FOCUS_DOWN);
        textScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                textScrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 0);
    }

    private void processOutputGraphics() {

        if (graphicsTask != null) {
            graphicsTask.cancel(false);
            graphicsTask = null;
        }
        graphicsTask = new GraphicsTask(graphicsHandlerContext, graphicsHandler.getQueue());
        graphicsHandler.clearQueue();
        graphicsTask.execute();
    }

    @SuppressLint("DefaultLocale")
    private boolean isNewLocation(String text) {
        return newLocationCommands.contains(text.toLowerCase());
    }

    @SuppressLint("DefaultLocale")
    private boolean isBumpedInto(String text) {
        return text.toLowerCase().contains("bump") && text.toLowerCase().contains("ouch");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case android.R.id.home:
            if (initialised) {
                saveGame(false);
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveGame(boolean showToast) {
        GameStateSummary summary = new GameStateSummary(gameStateSlot, System.currentTimeMillis(), lastLocation);
        gameStateDao.save(summary, level9.getGameState(), graphicsHandlerContext.getBitmap());
        if (showToast) {
            Toast toast = Toast.makeText(getApplicationContext(), "Game saved", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private void loadGame() {

        GameState gameState = gameStateDao.loadGameState(gameStateSlot);
        GameStateSummary gameStateSummary = gameStateDao.loadGameStateSummary(gameStateSlot);
        if (gameState != null) {
            level9.setGameState(gameState);
            graphicsHandlerContext.setBitmap(gameStateDao.loadGraphics(gameStateSlot));
            imageView.setBitmap(graphicsHandlerContext.getBitmap());
            textView.setText("");
            processOutputText(gameStateSummary.getLastLocation());
            // TODO hacky adding in a sneaky look because VM is not in the correct state for some reason
            level9.execute("look");
            Toast toast = Toast.makeText(getApplicationContext(), "Game loaded", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Failed to load game", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        private final int SWIPE_THRESHOLD_VELOCITY = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            double xDir = e1.getX() - e2.getX();
            double yDir = e1.getY() - e2.getY();

            if (xDir > SWIPE_THRESHOLD_VELOCITY) {
                if (yDir > SWIPE_THRESHOLD_VELOCITY) {
                    processCommand("northwest");
                } else if (yDir < -SWIPE_THRESHOLD_VELOCITY) {
                    processCommand("southwest");
                } else {
                    processCommand("west");
                }
                return true;
            } else if (xDir < -SWIPE_THRESHOLD_VELOCITY) {
                if (yDir > SWIPE_THRESHOLD_VELOCITY) {
                    processCommand("northeast");
                } else if (yDir < -SWIPE_THRESHOLD_VELOCITY) {
                    processCommand("southeast");
                } else {
                    processCommand("east");
                }
                return true;
            }

            if (yDir < -SWIPE_THRESHOLD_VELOCITY) {
                processCommand("south");
                return true;
            }

            if (yDir > SWIPE_THRESHOLD_VELOCITY) {
                processCommand("north");
                return true;
            }

            return false;
        }
    }

    class MyFilehandler implements FileHandler {

        @Override
        public void saveFile(GameState gameState) throws IOException {
            Log.i("gameActivity", "saveFile: " + gameState);
            saveGame(false);
        }

        @Override
        public GameState loadFile() throws IOException {
            Log.i("gameActivity", "loadFile");
            // TODO what if this is null?
            return gameStateDao.loadGameState(gameStateSlot);
        }

        @Override
        public boolean getGameFile(String newName, int size) {
            Log.i("gameActivity", "getGameFile: " + newName + ", " + size);
            return false;
        }

        @Override
        public void setFileNumber(String newName, int size, int n) {
            Log.i("gameActivity", "setFileNumber: " + newName + ", " + size + ", " + n);
        }

        @Override
        public void ramSave(int x, int maxSaveSlots, int[] varTable) {
            Log.i("gameActivity", "  -> ramSave(" + x + ")");
            if (ramSaveSlots == null || ramSaveSlots.length != maxSaveSlots) {
                ramSaveSlots = new RamSave[maxSaveSlots];
            }
            // take a copy
            ramSaveSlots[x] = new RamSave(varTable);
        }

        @Override
        public int[] ramLoad(int x) {
            Log.i("gameActivity", "  -> ramLoad(" + x + ")");
            return ramSaveSlots[x].getVarTable();
        }

    }

    private class GraphicsTask extends AsyncTask<Integer, Bitmap, Bitmap> {

        private final GraphicsContext context;
        private final List<GraphicsCommand> commandQueue;

        public GraphicsTask(GraphicsContext context, List<GraphicsCommand> commandQueue) {
            this.context = context;
            this.commandQueue = commandQueue;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {

            for (GraphicsCommand command : commandQueue) {
                command.execute(context);
                if (isCancelled()) {
                    break;
                }
            }
            return context.getBitmap();
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            imageView.setBitmap(result);
            imageView.invalidate();
        }
    }

    public void textViewLinkClick(String word) {
        Log.i("gameActivity", "Selection start " + textView.getSelectionStart());
        Log.i("gameActivity", "Selection end " + textView.getSelectionEnd());

    }

    @Override
    public void wordClicked(String word) {
        String text = textInputView.getCommand();
        if (!text.endsWith(" ")) {
            text = text + " ";
        }
        text = text + word;
        textInputView.setCommand(text);
    }

    void dbgScreenSize() {

        // Determine screen size
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (screenSize) {
        case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            Toast.makeText(this, "XLarge screen", Toast.LENGTH_LONG).show();
            break;
        case Configuration.SCREENLAYOUT_SIZE_LARGE:
            Toast.makeText(this, "Large screen", Toast.LENGTH_LONG).show();
            break;
        case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            Toast.makeText(this, "Normal sized screen", Toast.LENGTH_LONG).show();
            break;
        case Configuration.SCREENLAYOUT_SIZE_SMALL:
            Toast.makeText(this, "Small sized screen", Toast.LENGTH_LONG).show();
            break;
        default:
            Toast.makeText(this, "Unknown screen size", Toast.LENGTH_LONG).show();
            break;
        }
    }
}
