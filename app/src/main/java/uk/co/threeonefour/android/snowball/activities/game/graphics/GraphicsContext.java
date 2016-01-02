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

import java.io.Serializable;
import java.util.Arrays;

import uk.co.threeonefour.android.snowball.basics.lang.ArrayUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

public class GraphicsContext implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;

    static final int[] basePalette = new int[] { Color.rgb(0x00, 0x00, 0x00), /* Black */
    Color.rgb(0xFF, 0x00, 0x00), /* Red */
    Color.rgb(0x30, 0xE8, 0x30), /* Green */
    Color.rgb(0xFF, 0xFF, 0x00), /* Yellow */
    Color.rgb(0x00, 0x00, 0xFF), /* Blue */
    Color.rgb(0xA0, 0x68, 0x00), /* Brown */
    Color.rgb(0x00, 0xFF, 0xFF), /* Cyan */
    Color.rgb(0xFF, 0xFF, 0xFF), /* White */
    };

    private int[][] imageData;
    private int width, height;
    private int[] imagePalette;

    private transient Bitmap bitmap;

    public static final Parcelable.Creator<GraphicsContext> CREATOR = new Parcelable.Creator<GraphicsContext>() {
        public GraphicsContext createFromParcel(Parcel in) {
            return new GraphicsContext(in);
        }

        public GraphicsContext[] newArray(int size) {
            return new GraphicsContext[size];
        }
    };

    public GraphicsContext() {

        clearPalette();

        // TODO magic numbers
        this.width = 160;
        this.height = 128;
        imageData = new int[width][height];
    }

    public GraphicsContext(GraphicsContext src) {

        copyFrom(src);
    }

    public void copyFrom(GraphicsContext src) {
        if (src != null) {
            this.width = src.width;
            this.height = src.height;
            this.imagePalette = Arrays.copyOf(src.imagePalette, src.imagePalette.length);
            this.imageData = ArrayUtils.cloneArray(src.imageData);
            this.bitmap = src.getBitmap();
        }
    }

    private GraphicsContext(Parcel in) {
        this.width = in.readInt();
        this.height = in.readInt();
        in.readIntArray(this.imagePalette);
        imageData = new int[width][height];
        for (int x = 0; x < width; x++) {
            in.readIntArray(imageData[x]);
        }
        this.bitmap = null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeIntArray(this.imagePalette);
        for (int x = 0; x < width; x++) {
            dest.writeIntArray(imageData[x]);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setImagePalette(int index, int colour) {

        if (index < 0 || index >= imagePalette.length)
            return;

        if (colour < 0 || colour >= basePalette.length)
            return;

        imagePalette[index] = basePalette[colour];

    }

    public Bitmap getBitmap() {
        if (bitmap == null) {
            createBitmap();
        }
        return bitmap;
    }

    private Bitmap createBitmap() {
        bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, imagePalette[imageData[x][y]]);
            }
        }
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void clearBitmap() {
        bitmap = null;
        imageData = new int[width][height];
    }

    public void plot(int x, int y, int c1, int c2) {
        if (x >= 0 && y >= 0 && x < width && y < height) {

            int current = getPixel(x, y);
            if (current == c2) {
                imageData[x][y] = c1;
            }
        }
        bitmap = null;
    }

    public int getPixel(int x, int y) {
//        if (x < 0 || x >= width || y < 0 || y >=height) {
//            return 0;
//        }
        return imageData[x][y];
    }

    public int getColour(int index) {
        return basePalette[index];
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        imageData = new int[width][height];
        bitmap = null;
    }

    public void setPixel(int x, int y, int colour) {
//        if (x < 0 || x >= width || y < 0 || y >=height) {
//            return;
//        }
        imageData[x][y] = colour;
        bitmap = null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void clearPalette() {
        imagePalette = new int[32];
        for (int i = 0; i < imagePalette.length; i++) {
            imagePalette[i] = Color.BLACK;
        }
    }

}