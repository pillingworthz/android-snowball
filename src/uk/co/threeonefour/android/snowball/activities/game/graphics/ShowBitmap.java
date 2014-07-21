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
package uk.co.threeonefour.android.snowball.activities.game.graphics;

import java.util.logging.Logger;

public class ShowBitmap implements GraphicsCommand {

    private static final Logger LOG = Logger.getLogger(ShowBitmap.class.getName());

    private final int pic;
    private final int x;
    private final int y;

    public ShowBitmap(int pic, int x, int y) {
        this.pic = pic;
        this.x = x;
        this.y = y;
    }

    public void execute(GraphicsContext context) {
        LOG.info("ShowBitmap(" + pic + "," + x + "," + y + ")");
    }
}