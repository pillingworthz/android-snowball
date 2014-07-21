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

import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.threeonefour.android.snowball.level9j.ptr.IntPtr;
import uk.co.threeonefour.android.snowball.level9j.ptr.ReadOnlyIntPtr;
import uk.co.threeonefour.android.snowball.level9j.vm.Vm.L9GameTypes;

public class GraphicsVm {

    private static Logger LOG = Logger.getLogger(GraphicsVm.class.getName());

    /**
     * Graphics type Resolution Scale stack reset -------------------------------------------------
     * <ul>
     * <li>GFX_V2 160 x 128 yes</li>
     * <li>GFX_V3A 160 x 96 yes</li>
     * <li>GFX_V3B 160 x 96 no</li>
     * <li>GFX_V3C 320 x 96 no</li>
     * </ul>
     */
    enum L9GfxTypes {
        GFX_V2, GFX_V3A, GFX_V3B, GFX_V3C
    };

    L9GfxTypes gfx_mode = L9GfxTypes.GFX_V2;

    int reflectflag, scale, gintcolour, option;
    int l9textmode = 0, drawx = 0, drawy = 0, screencalled = 0, showtitle = 1;
    IntPtr gfxa5;
    boolean scalegfx = true;

    private static final int GFXSTACKSIZE = 100;

    IntPtr picturedata = null;
    int picturesize = 0;

    IntPtr[] gfxA5Stack = new IntPtr[GFXSTACKSIZE];
    int gfxA5StackPos = 0;
    int[] gfxScaleStack = new int[GFXSTACKSIZE];
    int gfxScaleStackPos = 0;

    private final GraphicsHandler callbackGraphics;
    private final L9GameTypes gameType;

    public GraphicsVm(GraphicsHandler callbackGraphics, L9GameTypes gameType) {

        this.callbackGraphics = callbackGraphics;
        this.gameType = gameType;
    }

    public void show_picture(int pic) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("show_picture(" + pic + ")");
        }

        gfxa5 = picturedata.copy();

        /* gintinit */
        gintcolour = 3;
        option = 0x80;
        reflectflag = 0;
        drawx = 0x1400;
        drawy = 0x1400;
        /* sizereset */
        scale = 0x80;

        gfxA5StackPos = 0;
        gfxScaleStackPos = 0;
        absrunsub(0);
        // if (!findsub(pic, gfxa5)) {
        // gfxa5 = null;
        // }
        absrunsub(pic);
    }

    void absrunsub(int d0) {

        IntPtr a5 = picturedata.copy();
        if (!findsub(d0, a5))
            return;
        while (getinstruction(a5))
            ;

    }

    boolean getinstruction(IntPtr a5) {

        int d7 = a5.getPostIncr();
        if ((d7 & 0xc0) != 0xc0) {
            switch ((d7 >> 6) & 3) {
            case 0:
                sdraw(d7);
                break;
            case 1:
                smove(d7);
                break;
            case 2:
                sgosub(d7, a5);
                break;
            }
        } else if ((d7 & 0x38) != 0x38) {
            switch ((d7 >> 3) & 7) {
            case 0:
                draw(d7, a5);
                break;
            case 1:
                _move(d7, a5);
                break;
            case 2:
                icolour(d7);
                break;
            case 3:
                size(d7);
                break;
            case 4:
                gintfill(d7);
                break;
            case 5:
                gosub(d7, a5);
                break;
            case 6:
                reflect(d7);
                break;
            }
        } else {
            switch (d7 & 7) {
            case 0:
                notimp();
                break;
            case 1:
                gintchgcol(a5);
                break;
            case 2:
                notimp();
                break;
            case 3:
                amove(a5);
                break;
            case 4:
                opt(a5);
                break;
            case 5:
                restorescale();
                break;
            case 6:
                notimp();
                break;
            case 7:
                return rts(a5);
            }
        }
        return true;
    }

    /*
     * sdraw instruction plus arguments are stored in an 8 bit word. 76543210 iixxxyyy where i is instruction code x is
     * x argument, high bit is sign y is y argument, high bit is sign
     */
    void sdraw(int d7) {

        int x, y, x1, y1;

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("sdraw(" + d7 + ")");
        }

        /* getxy1 */
        x = (d7 & 0x18) >> 3;
        if ((d7 & 0x20) != 0) {
            x = (x | 0xfc) - 0x100;
        }
        y = (d7 & 0x3) << 2;
        if ((d7 & 0x4) != 0) {
            y = (y | 0xf0) - 0x100;
        }

        if ((reflectflag & 2) != 0) {
            x = -x;
        }
        if ((reflectflag & 1) != 0) {
            y = -y;
        }

        /* gintline */
        x1 = drawx;
        y1 = drawy;
        newxy(x, y);

        callbackGraphics.drawLine(scalex(x1), scaley(y1), scalex(drawx), scaley(drawy), gintcolour & 3, option & 3);
    }

    /*
     * smove instruction plus arguments are stored in an 8 bit word. 76543210 iixxxyyy where i is instruction code x is
     * x argument, high bit is sign y is y argument, high bit is sign
     */
    void smove(int d7) {

        int x, y;

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("smove(" + d7 + ")");
        }

        /* getxy1 */
        x = (d7 & 0x18) >> 3;
        if ((d7 & 0x20) != 0)
            x = (x | 0xfc) - 0x100;
        y = (d7 & 0x3) << 2;
        if ((d7 & 0x4) != 0)
            y = (y | 0xf0) - 0x100;

        if ((reflectflag & 2) != 0)
            x = -x;
        if ((reflectflag & 1) != 0)
            y = -y;
        newxy(x, y);
    }

    void sgosub(int d7, IntPtr a5) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("sgosub(" + d7 + ", " + a5.get() + ")");
        }

        int d0 = d7 & 0x3f;
        gosubd0(d0, a5);
    }

    void newxy(int x, int y) {

        drawx += (x * scale) & ~7;
        drawy += (y * scale) & ~7;
    }

    void gosubd0(int d0, IntPtr a5) {

        if (gfxA5StackPos < GFXSTACKSIZE) {
            gfxA5Stack[gfxA5StackPos] = a5.copy();
            gfxA5StackPos++;
            gfxScaleStack[gfxScaleStackPos] = scale;
            gfxScaleStackPos++;

            if (findsub(d0, a5) == false) {
                gfxA5StackPos--;
                IntPtr.copyInto(a5, gfxA5Stack[gfxA5StackPos]);
                gfxScaleStackPos--;
                scale = gfxScaleStack[gfxScaleStackPos];
            }
        }
    }

    int scalex(int x) {
        return (gfx_mode != L9GfxTypes.GFX_V3C) ? (x >> 6) : (x >> 5);
    }

    int scaley(int y) {
        return (gfx_mode != L9GfxTypes.GFX_V2) ? 127 - (y >> 7) : 95 - (((y >> 5) + (y >> 6)) >> 3);
    }

    /*
     * draw instruction plus arguments are stored in a 16 bit word. FEDCBA9876543210 iiiiixxxxxxyyyyy where i is
     * instruction code x is x argument, high bit is sign y is y argument, high bit is sign
     */
    void draw(int d7, IntPtr a5) {

        int xy, x, y, x1, y1;

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("draw(" + d7 + ", " + a5.get() + ")");
        }

        /* getxy2 */
        xy = (d7 << 8) + a5.getPostIncr();
        x = (xy & 0x3e0) >> 5;
        if ((xy & 0x400) != 0)
            x = (x | 0xe0) - 0x100;
        y = (xy & 0xf) << 2;
        if ((xy & 0x10) != 0)
            y = (y | 0xc0) - 0x100;

        if ((reflectflag & 2) != 0)
            x = -x;
        if ((reflectflag & 1) != 0)
            y = -y;

        /* gintline */
        x1 = drawx;
        y1 = drawy;
        newxy(x, y);

        callbackGraphics.drawLine(scalex(x1), scaley(y1), scalex(drawx), scaley(drawy), gintcolour & 3, option & 3);
    }

    /*
     * move instruction plus arguments are stored in a 16 bit word. FEDCBA9876543210 iiiiixxxxxxyyyyy where i is
     * instruction code x is x argument, high bit is sign y is y argument, high bit is sign
     */
    void _move(int d7, IntPtr a5) {

        int xy, x, y;

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("_move(" + d7 + ", " + a5.get() + ")");
        }

        /* getxy2 */
        xy = (d7 << 8) + a5.getPostIncr();
        x = (xy & 0x3e0) >> 5;
        if ((xy & 0x400) != 0)
            x = (x | 0xe0) - 0x100;
        y = (xy & 0xf) << 2;
        if ((xy & 0x10) != 0)
            y = (y | 0xc0) - 0x100;

        if ((reflectflag & 2) != 0)
            x = -x;
        if ((reflectflag & 1) != 0)
            y = -y;
        newxy(x, y);
    }

    void icolour(int d7) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("icolour(" + d7 + ")");
        }

        gintcolour = d7 & 3;
    }

    static final int[] sizetable = { 0x02, 0x04, 0x06, 0x07, 0x09, 0x0c, 0x10 };

    void size(int d7) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("size(" + d7 + ")");
        }

        d7 &= 7;
        if (d7 != 0) {
            int d0 = (scale * sizetable[d7 - 1]) >> 3;
            scale = (d0 < 0x100) ? d0 : 0xff;
        } else {
            /* sizereset */
            scale = 0x80;
        }
    }

    void gintfill(int d7) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("gintfill(" + d7 + ")");
        }

        if ((d7 & 7) == 0)
            /* filla */
            d7 = gintcolour;
        else
            d7 &= 3;
        /* fillb */

        callbackGraphics.fill(scalex(drawx), scaley(drawy), d7 & 3, option & 3);
    }

    void gosub(int d7, IntPtr a5) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("gosub(" + d7 + ", " + a5.get() + ")");
        }

        int d0 = ((d7 & 7) << 8) + a5.getPostIncr();
        gosubd0(d0, a5);
    }

    void reflect(int d7) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("reflect(" + d7 + ")");
        }

        if ((d7 & 4) != 0) {
            d7 &= 3;
            d7 ^= reflectflag;
        }
        /* reflect1 */
        reflectflag = d7;
    }

    void notimp() {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("notimp()");
        }

    }

    void gintchgcol(IntPtr a5) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("gintchgcol(" + a5.get() + ")");
        }

        int d0 = a5.getPostIncr();

        callbackGraphics.setColour((d0 >> 3) & 3, d0 & 7);
    }

    void amove(IntPtr a5) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("amove(" + a5.get() + ")");
        }

        drawx = 0x40 * a5.getPostIncr();
        drawy = 0x40 * a5.getPostIncr();
    }

    void opt(IntPtr a5) {

        int d0 = a5.getPostIncr();

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("opt(" + d0 + ")");
        }

        if (d0 != 0)
            d0 = (d0 & 3) | 0x80;
        /* optend */
        option = d0;
    }

    void restorescale() {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("restorescale()");
        }

        if (gfxScaleStackPos > 0)
            scale = gfxScaleStack[gfxScaleStackPos - 1];
    }

    boolean rts(IntPtr a5) {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("rts()");
        }

        if (gfxA5StackPos > 0) {
            gfxA5StackPos--;
            IntPtr.copyInto(a5, gfxA5Stack[gfxA5StackPos]);
            if (gfxScaleStackPos > 0) {
                gfxScaleStackPos--;
                scale = gfxScaleStack[gfxScaleStackPos];
            }
            return true;
        }
        return false;
    }

    public void scanPicture(IntPtr startfile, IntPtr startdata) {

        if (!findsubs(startdata, startdata.sizeLeft()) && !findsubs(startfile, IntPtr.diff(startdata, startfile))) {
            picturedata = new ReadOnlyIntPtr("empty", new byte[0]);
            picturesize = 0;
        }

        if (picturedata == null) {
            LOG.info("no picture data");
        } else {
            LOG.info("picture data: ptr: " + picturedata + ", size: " + picturesize);
        }
    }

    boolean findsubs(IntPtr testptr, int testsize) {
        if (testsize < 16)
            return false;

        /*
         * Try to traverse the graphics subroutines.
         * 
         * Each subroutine starts with a header: nn | nl | ll nnn : the subroutine number ( 0x000 - 0x7ff ) lll : the
         * subroutine length ( 0x004 - 0x3ff )
         * 
         * The first subroutine usually has the number 0x000. Each subroutine ends with 0xff.
         * 
         * findsubs() searches for the header of the second subroutine (pattern: 0xff | nn | nl | ll) and then tries to
         * find the first and next subroutines by evaluating the length fields of the subroutine headers.
         */
        int i, secondSubStart = 0, count = 0;
        for (i = 4; i < testsize - 4; i++) {
            int eos = testptr.get(i - 1);
            int nn = testptr.get(i);
            int nl = testptr.get(i + 1);
            int subNum = (nn << 4) + (nl >> 4);
            int ll = testptr.get(i + 2);
            int subLen = ((nl & 0x0f) << 8) + ll;
            if (eos == 0xff && subNum < 0x800 && subLen > 4 && subLen < 0x400) {

                secondSubStart = i;
                count = 0;

                int test = secondSubStart;
                while (testptr.inRange(test + 4)) {

                    // int eosm3 = testptr.get(test - 3);
                    // int eosm2 = testptr.get(test - 2);
                    // int eosm1 = testptr.get(test - 1);
                    // int eosm0 = testptr.get(test - 0);
                    // int eosp1 = testptr.get(test + 1);
                    // int eosp2 = testptr.get(test + 2);
                    // int eosp3 = testptr.get(test + 3);

                    eos = testptr.get(test - 1);
                    nn = testptr.get(test);
                    nl = testptr.get(test + 1);
                    subNum = (nn << 4) + (nl >> 4);
                    ll = testptr.get(test + 2);
                    subLen = ((nl & 0x0f) << 8) + ll;

                    if (eos == 0xff && subNum < 0x800 && subLen > 4 && subLen < 0x400) {
                        count++;
                    } else {
                        break;
                    }

                    test += subLen;
                }

                if (count > 10) {

                    for (int j = 4; j < 0x3ff; j++) {
                        eos = testptr.get(secondSubStart - j - 1);
                        nn = testptr.get(secondSubStart - j);
                        nl = testptr.get(secondSubStart - j + 1);
                        subNum = (nn << 4) + (nl >> 4);
                        ll = testptr.get(secondSubStart - j + 2);
                        subLen = ((nl & 0x0f) << 8) + ll;

                        if (subLen == j) {

                            int firstSubStart = secondSubStart - j;
                            picturedata = testptr.copy().incr(firstSubStart);
                            picturesize = test - firstSubStart;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    boolean findsub(int d0, IntPtr a5) {

        IntPtr.copyInto(a5, picturedata);

        while (true) {
            if (!validgfxptr(a5)) {
                return false;
            }

            int nn = a5.get();
            int nl = a5.get(1);
            int subNum = (nn << 4) + (nl >> 4);
            int ll = a5.get(2);
            int subLen = ((nl & 0x0f) << 8) + ll;

            // System.out.println("subNum:" + subNum + ", subLen: " + subLen);

            if (d0 == subNum) {
                a5.incr(3);
                return true;
            }

            a5.incr(subLen);
            if (!validgfxptr(a5))
                return false;
        }
    }

    boolean validgfxptr(IntPtr a5) {

        return IntPtr.inrange(a5, picturedata, picturesize);
    }

    void detect_gfx_mode(IntPtr startdata, String firstLine) {
        if (gameType == L9GameTypes.V3) {
            if (firstLine != null) {
                /* These V3 games use graphics logic similar to the V2 games */
                if (firstLine.indexOf("price of magik") != -1)
                    gfx_mode = L9GfxTypes.GFX_V3A;
                else if (firstLine.indexOf("the archers") != -1)
                    gfx_mode = L9GfxTypes.GFX_V3A;
                else if (firstLine.indexOf("secret diary of adrian mole") != -1)
                    gfx_mode = L9GfxTypes.GFX_V3A;
                else if ((firstLine.indexOf("worm in paradise") != -1) && (firstLine.indexOf("silicon dreams") == -1))
                    gfx_mode = L9GfxTypes.GFX_V3A;
                else if (firstLine.indexOf("growing pains of adrian mole") != -1)
                    gfx_mode = L9GfxTypes.GFX_V3B;
                else if (firstLine.indexOf("jewels of darkness") != -1 && picturesize < 11000)
                    gfx_mode = L9GfxTypes.GFX_V3B;
                else if (firstLine.indexOf("silicon dreams") != -1) {
                    /*
                     * Return to Eden / SD ( PC )
                     */
                    /*
                     * Worm in Paradise / SD ( PC )
                     */
                    if (picturesize > 11000 || (startdata.get(0) == 0x14 && startdata.get(1) == 0x7d)
                            || (startdata.get(0) == 0xd7 && startdata.get(1) == 0x7c))
                        gfx_mode = L9GfxTypes.GFX_V3C;
                    else
                        gfx_mode = L9GfxTypes.GFX_V3B;
                }
            } else
                gfx_mode = L9GfxTypes.GFX_V3C;
        } else {
            gfx_mode = L9GfxTypes.GFX_V2;
        }

        int width, height;
        if (gameType == L9GameTypes.V4) {
            width = 0;
            height = 0;
        } else {
            width = (gfx_mode != L9GfxTypes.GFX_V3C) ? 160 : 320;
            height = (gfx_mode == L9GfxTypes.GFX_V2) ? 128 : 96;
            width = 160;
            height = 128;
        }

        callbackGraphics.setGraphicsSize(width, height);
    }

}
