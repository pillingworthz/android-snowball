package uk.co.threeonefour.android.snowball.activities.loadgame;

import uk.co.threeonefour.android.snowball.gamestate.GameStateSummary;

public class Clipboard {
    private GameStateSummary summary;

    public GameStateSummary getSummary() {
        return summary;
    }

    public void setSummary(GameStateSummary summary) {
        this.summary = summary;
    }

    public void clear() {
        summary = null;
    }
}