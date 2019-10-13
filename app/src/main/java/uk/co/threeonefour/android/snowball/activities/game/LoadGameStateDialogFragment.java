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

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import uk.co.threeonefour.android.snowball.R;
import uk.co.threeonefour.android.snowball.gamestate.FileGameStateDao;
import uk.co.threeonefour.android.snowball.gamestate.GameStateDao;
import uk.co.threeonefour.android.snowball.gamestate.GameStateSummary;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class LoadGameStateDialogFragment extends DialogFragment {

    private GameStateDao gameStateDao;
    private DateFormat dateFormat;
    private DateFormat timeFormat;

    public LoadGameStateDialogFragment() {
    }

    public interface LoadGameStateDialogListener {
        void onLoadGameStateDialogOk(DialogFragment dialog, GameStateSummary selected);
    }

    // Use this instance of the interface to deliver action events
    private LoadGameStateDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (LoadGameStateDialogListener) activity;
            gameStateDao = new FileGameStateDao(activity);
            dateFormat = android.text.format.DateFormat.getDateFormat(activity.getApplicationContext());
            timeFormat = android.text.format.DateFormat.getTimeFormat(activity.getApplicationContext());
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final List<GameStateSummary> infos = gameStateDao.listSavedGames();
        String[] infoStrs = new String[infos.size()];
        for (int i = 0; i < infos.size(); i++) {
            GameStateSummary info = infos.get(i);
            infoStrs[i] = "Snowball - saved "
                    + dateFormat.format(new Date(info.getFileCreated())) + " at "
                            + timeFormat.format(new Date(info.getFileCreated()));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_load_game);

        if (infos.isEmpty()) {
            builder.setMessage("There are no games available to load.");
        } else {

            builder.setItems(infoStrs, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    listener.onLoadGameStateDialogOk(LoadGameStateDialogFragment.this, infos.get(which));
                }
            });
        }

        return builder.create();
    }
}