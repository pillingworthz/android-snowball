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

import uk.co.threeonefour.android.snowball.level9j.vm.GameState;
import uk.co.threeonefour.android.snowball.level9j.vm.GameStateFactory;

public class PersistableGameStateFactory extends GameStateFactory {

    @Override
    public PersistableGameState create() {
        return new PersistableGameState();
    }

    @Override
    public PersistableGameState copy(GameState orig) {
        return new PersistableGameState(orig);
    }

}
