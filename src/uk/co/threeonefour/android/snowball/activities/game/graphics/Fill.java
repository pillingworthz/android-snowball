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


public class Fill implements GraphicsCommand {

    // private static final Logger LOG = Logger.getLogger(Fill.class.getName());

    private final int x;
    private final int y;
    private final int colour1;
    private final int colour2;

    public Fill(int x, int y, int colour1, int colour2) {
        this.x = x;
        this.y = y;
        this.colour1 = colour1;
        this.colour2 = colour2;
    }

    public void execute(GraphicsContext context) {

        // LOG.info("Fill(" + x + "," + y + "," + colour1 + "," + colour2 + ")");
        QuickFill fill = new QuickFill(x, y, colour1, colour2);
        fill.execute(context);

    }
}