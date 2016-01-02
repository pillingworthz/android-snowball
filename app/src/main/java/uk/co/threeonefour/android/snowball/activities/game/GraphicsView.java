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
package uk.co.threeonefour.android.snowball.activities.game;

import android.R.color;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class GraphicsView extends View {

    private Bitmap bitmap;

    public GraphicsView(Context context) {
        super(context);
        init();
    }

    public GraphicsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphicsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {

            float scale = (float) canvas.getHeight() / (float) bitmap.getHeight();
            canvas.scale(scale, scale);

            float cw = (float) canvas.getWidth() / scale;
            float bw = (float) bitmap.getWidth();
            int leftX = (int) ((cw - bw) / 2);

            
            canvas.drawARGB(0, Color.red(color.background_dark), Color.green(color.background_dark), Color.blue(color.background_dark));
            canvas.drawBitmap(bitmap, leftX, 0, null);
        }
    }

    public void setBitmap(Bitmap locationImage) {
        this.bitmap = locationImage;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
