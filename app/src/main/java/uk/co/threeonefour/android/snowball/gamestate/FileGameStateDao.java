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
package uk.co.threeonefour.android.snowball.gamestate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.threeonefour.android.snowball.basics.io.IOUtils;
import uk.co.threeonefour.android.snowball.level9j.vm.GameState;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class FileGameStateDao implements GameStateDao {

    private static final String LOGTAG = FileGameStateDao.class.getName();

    private static final String SERIALISED_FILENAME_EXTENSION = "ser";
    private static final String JSON_FILENAME_EXTENSION = "json";
    private static final String PNG_FILENAME_EXTENSION = "png";

    private static final String SUMMARY_SUFFIX = "_summary";
    private static final String STATE_SUFFIX = "_vmstate";
    private static final String GRAPHICS_SUFFIX = "_graphics";

    private static final int MAX_SLOTS = 6;

    private final Activity activity;

    public FileGameStateDao(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void save(GameStateSummary gameStateSummary, GameState gameState, Bitmap graphics) {

        int slot = gameStateSummary.getSlot();

        String filename = getSummaryFilename(slot);
        OutputStream fos = null;
        try {
            Log.i(LOGTAG, "  saveFile " + filename);
            fos = activity.openFileOutput(filename, Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
            String str = gameStateSummary.toJson().toString();
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            Log.e(LOGTAG, "Failed to save game state summary for " + slot, e);
        } finally {
            IOUtils.closeQuietly(fos);
        }

        filename = getStateFilename(slot);
        fos = null;
        try {
            Log.i(LOGTAG, "  saveFile " + filename);
            fos = activity.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(gameState);
            oos.close();
        } catch (IOException e) {
            Log.e(LOGTAG, "Failed to save game state for " + slot, e);
        } finally {
            IOUtils.closeQuietly(fos);
        }

        filename = getGraphicsFilename(slot);
        if (graphics == null) {
            deleteFile(filename);
        } else {
            fos = null;
            try {
                Log.i(LOGTAG, "  saveFile " + filename);
                fos = activity.openFileOutput(filename, Context.MODE_PRIVATE);
                graphics.compress(CompressFormat.PNG, 100, fos);
            } catch (IOException e) {
                Log.e(LOGTAG, "Failed to save preview for " + slot, e);
            } finally {
                IOUtils.closeQuietly(fos);
            }
        }
    }

    @Override
    public void copy(GameStateSummary from, GameStateSummary to) {

        int fromSlot = from.getSlot();
        int toSlot = to.getSlot();

        clear(toSlot);

        copyFile(getSummaryFilename(fromSlot), getSummaryFilename(toSlot));
        copyFile(getStateFilename(fromSlot), getStateFilename(toSlot));
        copyFile(getGraphicsFilename(fromSlot), getGraphicsFilename(toSlot));
    }

    private void copyFile(String from, String to) {

        Log.i(LOGTAG, "  copyFile from " + from + " to " + to);

        InputStream fis = null;
        OutputStream fos = null;
        try {
            fis = activity.openFileInput(from);
            fos = activity.openFileOutput(to, Context.MODE_PRIVATE);
            IOUtils.copy(fis, fos);
        } catch (IOException e) {
            Log.e(LOGTAG, "Failed to copy file from " + from + " to " + to, e);
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
        }
    }

    private void deleteFile(String filename) {

        Log.i(LOGTAG, "  deleteFile " + filename);
        activity.deleteFile(filename);
    }

    @Override
    public void clear(int slot) {

        Log.i(LOGTAG, "Clearing slot + " + slot);

        deleteFile(getSummaryFilename(slot));
        deleteFile(getStateFilename(slot));
        deleteFile(getGraphicsFilename(slot));
    }

    @Override
    public GameState loadLatest() {

        List<GameStateSummary> infos = listSavedGames();
        if (!infos.isEmpty()) {
            GameStateSummary info = infos.get(infos.size() - 1);
            return loadGameState(info.getSlot());
        }

        return null;
    }

    @Override
    public GameState loadGameState(int slot) {

        String filename = getStateFilename(slot);

        InputStream fis = null;
        try {
            Log.i(LOGTAG, "  loadFile " + filename);
            fis = activity.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            GameState gameState = (GameState) ois.readObject();
            ois.close();
            return gameState;
        } catch (FileNotFoundException e) {
            Log.i(LOGTAG, "Failed to find game state file for " + slot);
        } catch (IOException e) {
            Log.e(LOGTAG, "Failed to load game state file for " + slot, e);
        } catch (ClassNotFoundException e) {
            Log.e(LOGTAG, "Failed to load game state file for " + slot, e);
        } finally {
            if (fis != null) {
                IOUtils.closeQuietly(fis);
            }
        }

        return null;
    }

    @Override
    public GameStateSummary loadGameStateSummary(int slot) {

        String filename = getSummaryFilename(slot);

        InputStream fis = null;
        try {
            Log.i(LOGTAG, "  loadFile " + filename);
            fis = activity.openFileInput(filename);
            StringWriter sw = new StringWriter();
            InputStreamReader isr = new InputStreamReader(fis);
            IOUtils.copy(isr, sw);

            JSONObject jo = new JSONObject(sw.toString());
            GameStateSummary gameStateSummary = GameStateSummary.fromJson(jo);
            if (gameStateSummary != null) {
                gameStateSummary.setSlot(slot);
            }

            return gameStateSummary;
        } catch (FileNotFoundException e) {
            Log.i(LOGTAG, "Failed to find game state summary for " + slot);
        } catch (IOException e) {
            Log.e(LOGTAG, "Failed to load game state summary for " + slot, e);
        } catch (JSONException e) {
            Log.e(LOGTAG, "Failed to load game state summary for " + slot, e);
        } finally {
            if (fis != null) {
                IOUtils.closeQuietly(fis);
            }
        }

        return null;
    }

    public Bitmap loadGraphics(int slot) {

        String filename = getGraphicsFilename(slot);

        InputStream fis = null;
        try {
            Log.i(LOGTAG, "  loadFile " + filename);
            fis = activity.openFileInput(filename);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            return bitmap;
        } catch (FileNotFoundException e) {
            Log.i(LOGTAG, "Failed to find preview for " + slot);
        } finally {
            if (fis != null) {
                IOUtils.closeQuietly(fis);
            }
        }

        return null;
    }

    @Override
    public List<GameStateSummary> listSavedGames() {

        List<GameStateSummary> infos = new ArrayList<GameStateSummary>();

        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            GameStateSummary info = loadGameStateSummary(slot);
            if (info != null) {
                infos.add(info);
            } else {
                infos.add(new GameStateSummary(slot, 0, null));
            }
        }

        return infos;
    }

    public static String getId(int slot) {
        return "slot" + slot;
    }

    public static String getSummaryFilename(int slot) {
        return "slot_" + slot + SUMMARY_SUFFIX + "." + JSON_FILENAME_EXTENSION;
    }

    public static String getStateFilename(int slot) {
        return "slot_" + slot + STATE_SUFFIX + "." + SERIALISED_FILENAME_EXTENSION;
    }

    public static String getGraphicsFilename(int slot) {
        return "slot_" + slot + GRAPHICS_SUFFIX + "." + PNG_FILENAME_EXTENSION;
    }

}
