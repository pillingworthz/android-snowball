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

import java.io.Serializable;

import uk.co.threeonefour.android.snowball.basics.lang.PrimitiveUtils;

/**
 * Used to emulate a C pointer. Basically a pointer into an array of Integers.
 * 
 * @author pauli
 */
public class IntPtr implements Serializable {

    private static final long serialVersionUID = 1L;

    /** name of the array; mainly used for debugging and tracking instances */
    protected String name;

    /** the array being wrapped */
    protected int[] array;

    /** current index into the array, i.e. the pointer into the array */
    protected int index;

    /**
     * Create a named pointer.
     * 
     * @param name
     *            the name of the array; mainly used for debugging and tracking instances
     * @param array
     *            the array being wrapped
     */
    public IntPtr(String name, int[] array) {

        this.name = name;
        this.array = array;
        this.index = 0;
    }

    /**
     * Create a named pointer.
     * 
     * @param name
     *            the name of the array; mainly used for debugging and tracking instances
     * @param array
     *            the array being wrapped
     * @param index
     *            the initial index into the array
     */
    public IntPtr(String name, int[] array, int index) {

        this.name = name;
        this.array = array;
        this.index = index;
    }

    /**
     * Create a named pointer from an array of bytes
     * 
     * @param name
     * @param bytes
     *            the array of bytes being wrapped
     */
    public IntPtr(String name, byte[] bytes) {

        this.name = name;
        this.array = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            array[i] = PrimitiveUtils.fromByte(bytes[i]);
        }
    }

    public int getIndex() {

        return index;
    }

    /**
     * Dereference. Equivalent to *ptr;
     * 
     * @return value from the array for the current index value.
     */

    public int get() {

        return array[index];
    }

    /**
     * Dereference with offset. Equivalent to *(ptr + offset);
     * 
     * @param offset
     *            the offset into the array from the current index
     * @return value from the array for the current index value plus the given offset.
     */

    public int get(int offset) {

        return array[index + offset];
    }

    /**
     * Dereference and post incr. Equivalent to *ptr++;
     * 
     * @return value from the array for the current index value.
     */

    public int getPostIncr() {

        int v = array[index];
        index++;
        return v;
    }

    /**
     * Dereference. Equivalent to *ptr = value;
     * 
     * @param value
     *            the value to store
     */

    public void put(int value) {

        array[index] = value;
    }

    /**
     * Dereference with offset. Equivalent to *(ptr + offset) = value;
     * 
     * @param offset
     *            the offset into the array from the current index
     * @param value
     *            the value to store
     */

    public void put(int offset, int value) {

        array[index + offset] = value;
    }

    /**
     * Dereference with post increment. Equivalent to *ptr++ = value;
     * 
     * @param value
     *            the value to store
     */

    public void putPostIncr(int value) {

        array[index] = value;
        index++;
    }

    /**
     * Increment the pointer. Equivalent to ptr++;
     * 
     * @return this (fluent interface)
     */

    public IntPtr incr() {

        index++;
        return this;
    }

    /**
     * Increment the pointer by the specified amount. Equivalent to ptr+=amount;
     * 
     * @param amount
     *            the amount to increment the pointer by
     * @return this (fluent interface)
     */

    public IntPtr incr(int amount) {

        index += amount;
        return this;
    }

    /**
     * Decrement the pointer. Equivalent to ptr--;
     * 
     * @return this (fluent interface)
     */

    public IntPtr decr() {

        index--;
        return this;
    }

    /**
     * Decrement the pointer by the specified amount. Equivalent to ptr-=amount;
     * 
     * @param amount
     *            the amount to decrement the pointer by
     * @return this (fluent interface)
     */

    public IntPtr decr(int amount) {

        index -= amount;
        return this;
    }

    public boolean inRange() {

        return (index >= 0 && index < array.length);
    }

    public boolean inRange(int offset) {

        return (index + offset >= 0 && index + offset < array.length);
    }

    /**
     * @return size of the array
     */
    public int size() {

        return array.length;
    }

    public int sizeLeft() {

        return array.length - index;
    }

    @Override
    public String toString() {

        return "IntPtr [" + name + ", offset=" + index + "]";
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + array.hashCode();
        result = prime * result + index;
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntPtr other = (IntPtr) obj;
        if (array != other.array)
            return false;
        if (index != other.index)
            return false;
        return true;
    }

    /**
     * @return an IntPtr copy of this object
     */
    public IntPtr copy() {

        return new IntPtr(name, array, index);
    }

    /**
     * Copy a pointer from src to dest
     */
    public static void copyInto(IntPtr dest, IntPtr src) {

        if (dest.getClass() != src.getClass()) {
            throw new IllegalArgumentException("Can't update different pointer types");
        }
        dest.name = src.name;
        dest.array = src.array;
        dest.index = src.index;
    }

    public static boolean lt(IntPtr x, IntPtr y) {

        if (x.array != y.array) {
            throw new IllegalArgumentException("Can't compare different arrays");
        }
        return x.getIndex() < y.getIndex();
    }

    public static boolean lteq(IntPtr x, IntPtr y) {

        if (x.array != y.array) {
            throw new IllegalArgumentException("Can't compare different arrays");
        }
        return x.getIndex() <= y.getIndex();
    }

    public static boolean gt(IntPtr x, IntPtr y) {

        if (x.array != y.array) {
            throw new IllegalArgumentException("Can't compare different arrays");
        }
        return x.getIndex() > y.getIndex();
    }

    public static boolean gteq(IntPtr x, IntPtr y) {

        if (x.array != y.array) {
            throw new IllegalArgumentException("Can't compare different arrays");
        }
        return x.getIndex() >= y.getIndex();
    }

    public static boolean eq(IntPtr x, IntPtr y) {

        if (x.array != y.array) {
            throw new IllegalArgumentException("Can't compare different arrays");
        }
        return x.getIndex() == y.getIndex();
    }

    public static boolean neq(IntPtr x, IntPtr y) {

        if (x.array != y.array) {
            throw new IllegalArgumentException("Can't compare different arrays");
        }
        return x.getIndex() != y.getIndex();
    }

    public static boolean inrange(IntPtr x, IntPtr y, int size) {

        if (x.array != y.array) {
            throw new IllegalArgumentException("Can't compare different arrays");
        }
        return (x.getIndex() >= y.getIndex()) && (x.getIndex() < (y.getIndex() + size));
    }

    public static int diff(IntPtr x, IntPtr y) {

        if (x.array != y.array) {
            throw new IllegalArgumentException("Can't diff different arrays");
        }
        return x.getIndex() - y.getIndex();
    }

    public static int sum(IntPtr x, IntPtr y) {

        if (x.array != y.array) {
            throw new IllegalArgumentException("Can't sum different arrays");
        }
        return x.getIndex() + y.getIndex();
    }

    /**
     * Implementation of <code>#define L9WORD(x) (*(L9UINT16*)(x))</code>
     * 
     * @param ptr
     *            pointer to dereference
     * @param offset
     *            offset from current index
     * @return 16bit word from current index with given offset
     */
    public static int L9WORD(IntPtr ptr, int offset) {

        int lo = ptr.get(offset);
        int hi = ptr.get(offset + 1);
        return PrimitiveUtils.asUnsignedShort(hi * 256 + lo);
    }

    /**
     * Implementation of <code>#define L9SETWORD(x,val) (*(L9UINT16*)(x)=(L9UINT16)val)</code>
     * 
     * @param ptr
     *            pointer to dereference
     * @param offset
     *            offset from current index
     * @param value
     *            value to set
     */
    public static void L9SETWORD(IntPtr ptr, int offset, int value) {

        ptr.put(offset, value & 0xff);
        ptr.put(offset + 1, (value >> 8) & 0xff);
    }

    /**
     * Implementation of <code>   #define L9SETDWORD(x,val) (*(L9UINT32*)(x)=val)</code>
     * 
     * @param ptr
     *            pointer to dereference
     * @param offset
     *            offset from current index
     * @param value
     *            value to set
     */
    public static void L9SETDWORD(IntPtr ptr, int offset, int value) {

        ptr.put(offset, value & 0xff);
        ptr.put(offset + 1, (value >> 8) & 0xff);
        ptr.put(offset + 2, (value >> 16) & 0xff);
        ptr.put(offset + 3, (value >> 24) & 0xff);
    }
}
