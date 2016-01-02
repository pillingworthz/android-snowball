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

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import uk.co.threeonefour.android.snowball.R;
import uk.co.threeonefour.android.snowball.gamestate.GameStateDao;
import uk.co.threeonefour.android.snowball.gamestate.GameStateSummary;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GameStateDaoAdapter extends BaseAdapter {

    private List<GameStateSummary> data;
    private final LayoutInflater inflater;
    private final Activity activity;

    private final GameStateDao gameStateDao;
    private final Clipboard clipboard;
    private final DateFormat dateFormat;
    private final DateFormat timeFormat;

    public interface LoadGameHandler {

        void playGame(GameStateSummary summary);

        void newGame(GameStateSummary summary);

        void copyGame(GameStateSummary summary);

        void pasteGame(GameStateSummary summary);

        void clearGame(GameStateSummary summary);
    }

    private LoadGameHandler loadGameHandler;

    public GameStateDaoAdapter(Activity activity, GameStateDao gameStateDao, Clipboard clipboard) {
        this.gameStateDao = gameStateDao;
        this.clipboard = clipboard;
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dateFormat = android.text.format.DateFormat.getDateFormat(activity.getApplicationContext());
        timeFormat = android.text.format.DateFormat.getTimeFormat(activity.getApplicationContext());
    }

    public void setLoadGameHandler(LoadGameHandler loadGameHandler) {
        this.loadGameHandler = loadGameHandler;
    }

    public void updateData(List<GameStateSummary> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public int getCount() {
        return data == null ? 0 : data.size();
    }

    public GameStateSummary getItem(int position) {
        return data == null ? null : data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.load_game_list_row, null);
        }

        TextView title = (TextView) vi.findViewById(R.id.title);
        TextView subText = (TextView) vi.findViewById(R.id.subtext);
        Button clearButton = (Button) vi.findViewById(R.id.button_clear);
        Button pasteButton = (Button) vi.findViewById(R.id.button_paste);
        Button copyButton = (Button) vi.findViewById(R.id.button_copy);
        Button playButton = (Button) vi.findViewById(R.id.button_play);
        Button newGameButton = (Button) vi.findViewById(R.id.button_new_game);
        ImageView previewImage = (ImageView) vi.findViewById(R.id.image_preview);

        final GameStateSummary summary = data.get(position);

        String titleStr;
        if (summary.isEmpty()) {
            titleStr = "Empty slot";
            playButton.setVisibility(View.GONE);
            copyButton.setVisibility(View.GONE);
            clearButton.setVisibility(View.GONE);
            newGameButton.setVisibility(View.VISIBLE);
            if (clipboard.getSummary() != null) {
                pasteButton.setVisibility(View.VISIBLE);
                pasteButton.setEnabled(true);
            } else {
                pasteButton.setVisibility(View.GONE);
                pasteButton.setEnabled(false);
            }
        } else {
            titleStr = "Last saved " + dateFormat.format(new Date(summary.getFileCreated())) + " at "
                    + timeFormat.format(new Date(summary.getFileCreated()));
            playButton.setVisibility(View.VISIBLE);
            copyButton.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.VISIBLE);
            pasteButton.setVisibility(View.VISIBLE);
            newGameButton.setVisibility(View.GONE);
            if (clipboard.getSummary() != null) {
                pasteButton.setEnabled(true);
            } else {
                pasteButton.setEnabled(false);
            }
        }

        title.setText(titleStr);
        subText.setText(summary.getLastLocation());
        Bitmap graphics = gameStateDao.loadGraphics(summary.getSlot());
        if (graphics != null) {
            previewImage.setImageBitmap(graphics);
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            int resourceId = R.drawable.empty1;
            switch (position) {
            case 0:
                resourceId = R.drawable.empty1;
                break;
            case 1:
                resourceId = R.drawable.empty2;
                break;
            case 2:
                resourceId = R.drawable.empty3;
                break;
            case 3:
                resourceId = R.drawable.empty4;
                break;
            case 4:
                resourceId = R.drawable.empty5;
                break;
            case 5:
                resourceId = R.drawable.empty6;
                break;
            }
            Bitmap empty = BitmapFactory.decodeResource(activity.getResources(), resourceId, options);
            previewImage.setImageBitmap(empty);
        }

        copyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (loadGameHandler != null) {
                    loadGameHandler.copyGame(summary);
                }
            }
        });

        pasteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (loadGameHandler != null) {
                    loadGameHandler.pasteGame(summary);
                }
            }
        });

        clearButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (loadGameHandler != null) {
                    loadGameHandler.clearGame(summary);
                }
            }
        });

        playButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (loadGameHandler != null) {
                    loadGameHandler.playGame(summary);
                }
            }
        });

        newGameButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (loadGameHandler != null) {
                    loadGameHandler.newGame(summary);
                }
            }
        });

        return vi;
    }
}