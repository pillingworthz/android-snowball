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

import java.io.Serializable;
import java.util.Arrays;

import uk.co.threeonefour.android.snowball.level9j.ptr.IntPtr;
import uk.co.threeonefour.android.snowball.level9j.ptr.UnsignedShortPtr;

public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    static final int LISTAREASIZE = 0x800;

    protected int codePtr;

    // contains unsigned 16 bit values
    protected int[] varTable = new int[256];

    // contains unsigned 8 bit values
    protected int[] listArea = new int[LISTAREASIZE];

    protected int stackPtr;
    // contains unsigned 16 bit values
    protected int[] stack = new int[1024];

    protected GameState() {

    }

    protected GameState(GameState orig) {
        this.codePtr = orig.codePtr;
        this.varTable = Arrays.copyOf(orig.varTable, orig.varTable.length);
        this.listArea = Arrays.copyOf(orig.listArea, orig.listArea.length);
        this.stackPtr = orig.stackPtr;
        this.stack = Arrays.copyOf(orig.stack, orig.stack.length);
    }
    
    protected void updateFrom(GameState orig) {
        this.codePtr = orig.codePtr;
        System.arraycopy(orig.varTable, 0, varTable, 0, orig.varTable.length);
        System.arraycopy(orig.listArea, 0, listArea, 0, orig.listArea.length);
        this.stackPtr = orig.stackPtr;
        System.arraycopy(orig.stack, 0, stack, 0, orig.stack.length);
    }

    protected void updateVarTable(int[] vt) {
        System.arraycopy(vt, 0, varTable, 0, vt.length);
    }

    public void pushStack(int value) throws StackOverflowException {

        if (stackPtr == stack.length) {
            throw new StackOverflowException();
        }
        stack[stackPtr++] = value;
    }

    public int popStack() throws StackUnderflowException {

        if (stackPtr == 0) {
            throw new StackUnderflowException();
        }
        return stack[--stackPtr];
    }

    public int getStackPtr() {

        return stackPtr;
    }

    public void setStackPtr(int stackPtr) {

        this.stackPtr = stackPtr;
    }

    public void resetStackPtr() {

        stackPtr = 0;
    }

    public int[] getStack() {

        return stack;
    }

    public void setStack(int[] stack) {

        this.stack = stack;
    }

    public IntPtr getVarPtr(int offset) {

        return new UnsignedShortPtr("vartable", varTable, offset);
    }

    public int getVar(int offset) {

        return varTable[offset];
    }

    public void setVar(int offset, int value) {

        varTable[offset] = value;
    }

    public int[] getVarTable() {

        return varTable;
    }

    public void setVarTable(int[] varTable) {

        this.varTable = varTable;
    }

    public void clearWorkspace() {

        for (int i = 0; i < varTable.length; i++) {
            varTable[i] = 0;
        }
    }

    public IntPtr getListAreaPtr(int offset) {

        return new IntPtr("listarea", listArea, offset);
    }

    public int[] getListArea() {

        return listArea;
    }

    public void setListArea(int[] listArea) {

        this.listArea = listArea;
    }

    public void clearListArea() {

        for (int i = 0; i < LISTAREASIZE; i++) {
            listArea[i] = 0;
        }
    }

    public void setCodePtr(int codePtr) {

        this.codePtr = codePtr;
    }

    public int getCodePtr() {

        return codePtr;
    }
}
