/**
 * Copyright 2013 Paul Illingworth
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

import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Point;

public class QuickFill implements GraphicsCommand {

    private int x, y;

    private int _oldColor;
    private int _newColor;

    public QuickFill(int x, int y, int colour1, int colour2) {
        this.x = x;
        this.y = y;

        _oldColor = colour2;
        _newColor = colour1;
    }

    public void execute(GraphicsContext context) {

        if (_newColor == _oldColor) {
            return;
        }

        int bmpWidth = context.getWidth();
        int bmpHeight = context.getHeight();

        /* we are sometimes given x and y that are out of bounds */
        if (x < 0 || x >= bmpWidth || y < 0 || y >= bmpHeight) {
            return;
        }

        // 1. Set Q to the empty queue.
        Queue<Point> queue = new LinkedList<Point>();

        // 2. If the color of node is not equal to target-color, return.
        if (context.getPixel(x, y) == _oldColor) {
            // 3. Add node to Q.
            queue.add(new Point(x, y));

            // 4. For each element n of Q.
            while (!queue.isEmpty()) {
                // 5. If the color of n is equal to target-color.
                Point n = queue.poll();
                if (context.getPixel(n.x, n.y) == _oldColor) {
                    // 6. Set w and e equal to n.
                    int wx = n.x;
                    int ex = n.x + 1;

                    // 7. Move w to the west until the color of the node to the
                    // west of w no longer matches target-color.
                    while (wx >= 0 && context.getPixel(wx, n.y) == _oldColor) {
                        context.setPixel(wx, n.y, _newColor);
                        wx--;
                    }

                    // 8. Move e to the east until the color of the node to the
                    // east of e no longer matches target-color.
                    while (ex <= bmpWidth - 1 && context.getPixel(ex, n.y) == _oldColor) {
                        context.setPixel(ex, n.y, _newColor);
                        ex++;
                    }

                    // 9. Set the color of nodes between w and e to
                    // replacement-color.
                    // int length = ex - wx - 1;
                    // if (length > 0) {
                    // context.setPixels(scanLine, wx + 1, _bmpWidth, wx + 1, n.y, length, 1);
                    // }

                    // 10. For each node n between w and e.
                    for (int ix = wx + 1; ix < ex; ix++) {
                        // 11. If the color of the node to the north of n is
                        // target-color, add that node to Q.
                        if (n.y - 1 >= 0 && context.getPixel(ix, n.y - 1) == _oldColor) {
                            queue.add(new Point(ix, n.y - 1));
                        }

                        // 12. If the color of the node to the south of n is
                        // target-color, add that node to Q.
                        if (n.y + 1 < bmpHeight && context.getPixel(ix, n.y + 1) == _oldColor) {
                            queue.add(new Point(ix, n.y + 1));
                        }
                    }
                }
            }
        }
    }

}