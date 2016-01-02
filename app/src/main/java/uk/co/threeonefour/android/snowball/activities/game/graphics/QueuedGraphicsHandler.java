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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import uk.co.threeonefour.android.snowball.level9j.vm.GraphicsHandler;

public class QueuedGraphicsHandler implements GraphicsHandler {

    private static final Logger LOG = Logger.getLogger(QueuedGraphicsHandler.class.getName());

    private final LinkedList<GraphicsCommand> queue = new LinkedList<GraphicsCommand>();

    public QueuedGraphicsHandler() {
    }

    public List<GraphicsCommand> getQueue() {
        return new ArrayList<GraphicsCommand>(queue);
    }

    public void clearQueue() {
        queue.clear();
    }

    private void queueCommand(GraphicsCommand command) {
        queue.add(command);
    }

    @Override
    public void setGraphicsMode(int mode) {
        LOG.info("setGraphicsMode(" + mode + ")");
    }

    @Override
    public void setGraphicsSize(int width, int height) {
        queueCommand(new GraphicsSize(width, height));
    }

    @Override
    public void clear() {
        queue.clear();
        queueCommand(new Clear());
    }

    @Override
    public void setColour(int colour, int index) {
        queueCommand(new SetColour(colour, index));
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int colour1, int colour2) {
        queueCommand(new DrawLine(x1, y1, x2, y2, colour1, colour2));
    }

    @Override
    public void fill(int x, int y, int colour1, int colour2) {
        queueCommand(new Fill(x, y, colour1, colour2));
    }

    @Override
    public void showBitmap(int pic, int x, int y) {
        queueCommand(new ShowBitmap(pic, x, y));
    }
}