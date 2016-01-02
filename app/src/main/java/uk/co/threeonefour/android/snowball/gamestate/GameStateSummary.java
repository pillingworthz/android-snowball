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

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GameStateSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String LAST_LOCATION_PROPERTY = "lastLocation";

    private static final String FILE_CREATED_PROPERTY = "fileCreated";

    private static final String SLOT_PROPERTY = "slot";

    private static final String LOGTAG = GameStateSummary.class.getName();

    private int slot;
    private long fileCreated;
    private String lastLocation;

    public GameStateSummary() {
    }

    public GameStateSummary(int slot, long fileCreated, String lastLocation) {
        this.slot = slot;
        this.fileCreated = fileCreated;
        this.lastLocation = lastLocation;
    }

    public int getSlot() {
        return slot;
    }

    public long getFileCreated() {
        return fileCreated;
    }

    public String getLastLocation() {
        return lastLocation;
    }

    public boolean isEmpty() {
        return fileCreated == 0;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public static GameStateSummary fromJson(JSONObject jo) {
        GameStateSummary summary = new GameStateSummary();
        try {
            summary.slot = jo.getInt(SLOT_PROPERTY);
            summary.fileCreated = jo.getLong(FILE_CREATED_PROPERTY);
            summary.lastLocation = jo.getString(LAST_LOCATION_PROPERTY);
            return summary;
        } catch (JSONException e) {
            Log.e(LOGTAG, "Failed to read GameStateSummary from " + jo, e);
        }
        return null;
    }

    public JSONObject toJson() {
        JSONObject jo = new JSONObject();
        try {
            jo.put(SLOT_PROPERTY, getSlot());
            jo.put(FILE_CREATED_PROPERTY, getFileCreated());
            jo.put(LAST_LOCATION_PROPERTY, getLastLocation());
            return jo;
        } catch (JSONException e) {
            Log.e(LOGTAG, "Failed to write GameStateSummary to " + jo, e);
        }
        return null;
    }

}