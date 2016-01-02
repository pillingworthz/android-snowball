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


public class DrawLine implements GraphicsCommand {

//    private static final Logger LOG = Logger.getLogger(DrawLine.class.getName());

    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    private final int colour1;
    private final int colour2;

    public DrawLine(int x1, int y1, int x2, int y2, int colour1, int colour2) {
//        LOG.info("Drawline(" + x1 + "," + y1 + "," + x2 + "," + y2 + "," + colour1 + "," + colour2 + ")");
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.colour1 = colour1;
        this.colour2 = colour2;
    }

    public void execute(GraphicsContext context) {
//        LOG.info("drawLine(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ", " + colour1 + ", " + colour2 + ")");

        int c1 = colour1;
        int c2 = colour2;

        // Bresenham, see https://github.com/fragkakis/bresenham

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;
        int e2;
        int currentX = x1;
        int currentY = y1;

        while (true) {
            context.plot(currentX, currentY, c1, c2);

            if (currentX == x2 && currentY == y2) {
                break;
            }

            e2 = 2 * err;
            if (e2 > -1 * dy) {
                err = err - dy;
                currentX = currentX + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                currentY = currentY + sy;
            }
        }
    }
}