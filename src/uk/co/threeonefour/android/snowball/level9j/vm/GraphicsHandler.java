/***********************************************************************\
 *
 * Java Level 9 interpreter
 * Copyright (c) 2011, Paul Illingworth, www.threeonefour.co.uk
 * 
 * Based version Level 9 version 5 interpreter which is 
 * Copyright (c) 1996-2011 Glen Summers and contributors.
 * Contributions from David Kinder, Alan Staniforth, Simon Baldwin,
 * Dieter Baron and Andreas Scherrer.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111, USA.
 *
 ***********************************************************************/
package uk.co.threeonefour.android.snowball.level9j.vm;

public interface GraphicsHandler {

    void setGraphicsMode(int mode);

    void clear();

    void setColour(int colour, int index);

    void drawLine(int x1, int y1, int x2, int y2, int colour1, int colour2);

    void fill(int x, int y, int colour1, int colour2);

    void showBitmap(int pic, int x, int y);

    void setGraphicsSize(int width, int height);
}
