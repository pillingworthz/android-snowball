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

import java.util.List;

import uk.co.threeonefour.android.snowball.level9j.vm.GameState;
import android.graphics.Bitmap;

public interface GameStateDao {

    List<GameStateSummary> listSavedGames();

    void save(GameStateSummary gameStateSummary, GameState gameState, Bitmap graphics);

    GameState loadGameState(int slot);

    GameStateSummary loadGameStateSummary(int slot);

    Bitmap loadGraphics(int slot);

    GameState loadLatest();

    void clear(int slot);

    void copy(GameStateSummary from, GameStateSummary to);

}