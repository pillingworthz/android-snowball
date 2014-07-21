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
package uk.co.threeonefour.android.snowball.level9j.ptr;

import uk.co.threeonefour.android.snowball.basics.lang.PrimitiveUtils;

/**
 * Used to emulate a C pointer. Basically a pointer into an array of unsigned shorts.
 * 
 * @author pauli
 */
public class UnsignedShortPtr extends IntPtr {

    private static final long serialVersionUID = 1L;

    /**
     * Create a named pointer.
     * 
     * @param name
     *            the name of the array; mainly used for debugging and tracking instances
     * @param array
     *            the array being wrapped
     * @param offset
     *            the offset into the given array
     */
    public UnsignedShortPtr(String name, int[] array, int offset) {

        super(name, array, offset);
    }

    /**
     * Dereference. Equivalent to *ptr = value;
     * 
     * @param value
     *            the value to store
     */

    @Override
    public void put(int value) {

        array[index] = PrimitiveUtils.asUnsignedShort(value);
    }

    /**
     * Dereference with offset. Equivalent to *(ptr + offset) = value;
     * 
     * @param offset
     *            the offset into the array from the current index
     * @param value
     *            the value to store
     */

    @Override
    public void put(int offset, int value) {

        array[index + offset] = PrimitiveUtils.asUnsignedShort(value);
    }

    /**
     * Dereference with post increment. Equivalent to *ptr++ = value;
     * 
     * @param value
     *            the value to store
     */

    @Override
    public void putPostIncr(int value) {

        array[index] = PrimitiveUtils.asUnsignedShort(value);
        index++;
    }

    /**
     * @return an IntPtr copy of this object
     */
    @Override
    public UnsignedShortPtr copy() {

        return new UnsignedShortPtr(name, array, index);
    }

}
