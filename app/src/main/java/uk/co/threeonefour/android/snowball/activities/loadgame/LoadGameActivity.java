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
package uk.co.threeonefour.android.snowball.activities.loadgame;

import uk.co.threeonefour.android.snowball.R;
import uk.co.threeonefour.android.snowball.activities.game.GameActivity;
import uk.co.threeonefour.android.snowball.activities.loadgame.GameStateDaoAdapter.LoadGameHandler;
import uk.co.threeonefour.android.snowball.gamestate.FileGameStateDao;
import uk.co.threeonefour.android.snowball.gamestate.GameStateDao;
import uk.co.threeonefour.android.snowball.gamestate.GameStateSummary;
import uk.co.threeonefour.android.snowball.gamestate.IntentConstants;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.core.app.NavUtils;

public class LoadGameActivity extends Activity implements LoadGameHandler {

    private final GameStateDao gameStateDao;
    private final Clipboard clipboard;

    private ListView list;
    private GameStateDaoAdapter adapter;

    public LoadGameActivity() {
        gameStateDao = new FileGameStateDao(this);
        clipboard = new Clipboard();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_load_game);

        list = (ListView) findViewById(R.id.list);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new GameStateDaoAdapter(this, gameStateDao, clipboard);
        adapter.setLoadGameHandler(this);
        list.setAdapter(adapter);
    }

    @Override
    public void onRestart() {

        super.onRestart();
        adapter.updateData(gameStateDao.listSavedGames());
    }

    @Override
    public void onStart() {

        super.onStart();
        adapter.updateData(gameStateDao.listSavedGames());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void copyGame(GameStateSummary summary) {
        clipboard.setSummary(summary);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void pasteGame(final GameStateSummary summary) {

        // nothing to paste
        if (clipboard.getSummary() == null) {
            return;
        }

        // pasting same slot
        if (clipboard.getSummary().getSlot() == summary.getSlot()) {
            return;
        }

        if (!summary.isEmpty()) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            alertDialog.setTitle("Confirm paste game");

            alertDialog
                    .setMessage("This slot contains a previously saved game.\n\nAre you sure you want to copy over it?");

            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    confirmedPasteGame(clipboard.getSummary(), summary);
                }
            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        } else {
            confirmedPasteGame(clipboard.getSummary(), summary);
        }
    }

    private void confirmedPasteGame(GameStateSummary from, GameStateSummary to) {

        gameStateDao.copy(from, to);
        clipboard.clear();
        adapter.updateData(gameStateDao.listSavedGames());
    }

    @Override
    public void clearGame(final GameStateSummary summary) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Confirm clear game");

        alertDialog
                .setMessage("This slot contains a previously saved game.\n\nAre you sure you want to delete this data?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                confirmedClearGame(summary);
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void confirmedClearGame(GameStateSummary summary) {

        gameStateDao.clear(summary.getSlot());
        adapter.updateData(gameStateDao.listSavedGames());
    }

    @Override
    public void playGame(GameStateSummary summary) {
        Intent intent = new Intent(LoadGameActivity.this, GameActivity.class);
        intent.putExtra(IntentConstants.GAME_STATE_SLOT, summary.getSlot());
        intent.putExtra(IntentConstants.NEW_GAME, false);
        startActivity(intent);
    }

    @Override
    public void newGame(final GameStateSummary summary) {

        if (!summary.isEmpty()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            alertDialog.setTitle("Confirm new game");

            alertDialog
                    .setMessage("This slot contains a previously saved game.\n\nAre you sure you want to delete this data and start a new game?");

            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    confirmedNewGame(summary);
                }
            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        } else {
            confirmedNewGame(summary);
        }
    }

    private void confirmedNewGame(GameStateSummary summary) {

        Intent intent = new Intent(LoadGameActivity.this, GameActivity.class);
        intent.putExtra(IntentConstants.GAME_STATE_SLOT, summary.getSlot());
        intent.putExtra(IntentConstants.NEW_GAME, true);
        startActivity(intent);
    }
}