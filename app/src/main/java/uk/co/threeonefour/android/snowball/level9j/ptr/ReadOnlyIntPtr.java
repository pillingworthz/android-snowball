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

/**
 * IntPtr that does not allow put() operations.
 * 
 * @author pauli
 */
public class ReadOnlyIntPtr extends IntPtr {

    public ReadOnlyIntPtr(String name, byte[] bytes) {

        super(name, bytes);
    }

    public ReadOnlyIntPtr(String name, int[] array, int index) {

        super(name, array, index);
    }

    public ReadOnlyIntPtr(String name, int[] array) {

        super(name, array);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public void put(int value) {

        throw new IllegalArgumentException("put");
    }

    @Override
    public void put(int offset, int value) {

        throw new IllegalArgumentException("put");
    }

    @Override
    public void putPostIncr(int value) {

        throw new IllegalArgumentException("put");
    }

    /**
     * @return an IntPtr copy of this object
     */
    @Override
    public IntPtr copy() {

        return new ReadOnlyIntPtr(name, array, index);
    }
}
