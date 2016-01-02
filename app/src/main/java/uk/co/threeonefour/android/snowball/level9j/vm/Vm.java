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

import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

import uk.co.threeonefour.android.snowball.basics.lang.CharacterUtils;
import uk.co.threeonefour.android.snowball.basics.lang.MutableDouble;
import uk.co.threeonefour.android.snowball.basics.lang.MutableInteger;
import uk.co.threeonefour.android.snowball.basics.lang.MutableLong;
import uk.co.threeonefour.android.snowball.basics.lang.PrimitiveUtils;
import uk.co.threeonefour.android.snowball.level9j.icy.Icy.BreakType;
import uk.co.threeonefour.android.snowball.level9j.ptr.IntPtr;

public class Vm {

    private static Logger LOG = Logger.getLogger(Vm.class.getName());

    // int exitreversaltable[] =
    // {0x00,0x04,0x06,0x07,0x01,0x08,0x02,0x03,0x05,0x0a,0x09,0x0c,0x0b,0xff,0xff,0x0f};
    // put some extra 0's on here to prevent AIOB exceptions (SNOWBALL loops
    // over 20 times)
    private static final int exitreversaltable[] = { 0x00, 0x04, 0x06, 0x07, 0x01, 0x08, 0x02, 0x03, 0x05, 0x0a, 0x09,
            0x0c, 0x0b, 0xff, 0xff, 0x0f, 0, 0, 0, 0 };

    enum L9GameTypes {
        V1, V2, V3, V4
    };

    enum L9MsgTypes {
        V1, V2
    };

    public static final int MAX_PATH = 256;

    private final TextHandler osText;
    private final FileHandler osFile;
    private final GraphicsHandler osGraphics;
    private final CodeFollow codeFollow;
    private final GameStateFactory gameStateFactory;

    private boolean running;
    private boolean showtitle = true;
    private boolean l9textmode;
    private boolean screencalled;

    private L9GameTypes L9GameType;
    private L9MsgTypes L9MsgType;
    private IntPtr[] L9Pointers;
    private IntPtr absdatablock, list2ptr, list3ptr, list9startptr, acodeptr;
    private IntPtr startmd, endmd, endwdp5, wordtable, dictdata, defdict;
    private int dictdatalen;
    private IntPtr startmdV2;
    private IntPtr startdata;

    private IntPtr list9ptr;
    private IntPtr codeptr; /* instruction codes */
    private int code; /* instruction codes */
//    private int step;
    private int randomseed = 45004;

    private GameState workspace;

    private boolean wordcase;
    private int unpackcount;
    private int[] unpackbuf = new int[8];
    private int unpackd3;
    private IntPtr dictptr;
    private IntPtr threechars = new IntPtr("threechars", new int[34]);
    private char lastchar = '.';
    private char lastactualchar;
    private int mdtmode;

    private IntPtr ibuffptr = null;
    private IntPtr obuffptr = new IntPtr("obuff", new int[34]);

    private static final int RAMSAVESLOTS = 10;
    private String LastGame;

    private GraphicsVm graphicsVm;
    StringBuilder firstLine = new StringBuilder();

    public Vm(TextHandler osText, FileHandler osFile, GraphicsHandler osGraphics, CodeFollow codeFollow, GameStateFactory gameStateFactory) {

        this.codeFollow = codeFollow;
        this.osText = osText;
        this.osFile = osFile;
        this.osGraphics = osGraphics;
        this.gameStateFactory = gameStateFactory;
    }

    boolean LoadGame2(IntPtr StartFile) {

        /* intstart */
        if (!intinitialise(StartFile))
            return false;
        /* if (!checksumgamedata()) return FALSE; */

        codeptr = acodeptr.copy();
        return true;
    }

    boolean intinitialise(IntPtr StartFile) {

        /* init */
        /* driverclg */

        int i;
        int hdoffset;
        int Offset;

        screencalled = false;
        l9textmode = false;

        Scan level9Scanner = new Scan();

        Offset = level9Scanner.run(StartFile);

        L9GameType = level9Scanner.getGameType();

        if (Offset < 0) {
            Offset = level9Scanner.ScanV2(StartFile);
            L9GameType = L9GameTypes.V2;
            if (Offset < 0) {
                Offset = level9Scanner.ScanV1(StartFile);
                L9GameType = L9GameTypes.V1;
                if (Offset < 0) {
                    LOG.warning("Unable to locate valid header in file");
                    return false;
                }
            }
        }

        startdata = StartFile.copy().incr(Offset);

        // FileSize-=Offset;

        /* setup pointers */
        if (L9GameType != L9GameTypes.V1) {
            /* V2,V3,V4 */

            hdoffset = (L9GameType == L9GameTypes.V2) ? 4 : 0x12;

            L9Pointers = new IntPtr[12];
            for (i = 0; i < 12; i++) {
                int d0 = IntPtr.L9WORD(startdata, hdoffset + i * 2);
                if ((i != 11 && d0 >= 0x8000 && d0 <= 0x9000)) {
                    L9Pointers[i] = workspace.getListAreaPtr(d0 - 0x8000);
                } else {
                    L9Pointers[i] = startdata.copy().incr(d0);
                }

                LOG.info("L9 Pointers " + i + ", " + L9Pointers[i]);
            }

            absdatablock = L9Pointers[0].copy();
            list2ptr = L9Pointers[3].copy();
            list3ptr = L9Pointers[4].copy();
            /* list9startptr */
            /*
             * if ((((L9UINT32) L9Pointers[10])&1)==0) L9Pointers[10]++; amiga word access hack
             */
            list9startptr = L9Pointers[10].copy();
            acodeptr = L9Pointers[11].copy();
        }

        switch (L9GameType) {
        case V1:
            break;
        case V2: {
            MutableDouble a2 = new MutableDouble();
            MutableDouble a1 = new MutableDouble();
            startmd = startdata.copy().incr(IntPtr.L9WORD(startdata, 0x0));
            startmdV2 = startdata.copy().incr(IntPtr.L9WORD(startdata, 0x2));

            /* determine message type */
            if (analyseV2(a2) && a2.get() > 2 && a2.get() < 10) {
                L9MsgType = L9MsgTypes.V2;
            } else if (analyseV1(a1) && a1.get() > 2 && a1.get() < 10) {
                L9MsgType = L9MsgTypes.V1;
            } else {
                LOG.severe("Unable to identify V2 message table in file");
                return false;
            }
            break;
        }
        case V3:
        case V4:
            startmd = startdata.copy().incr(IntPtr.L9WORD(startdata, 0x2));
            endmd = startmd.copy().incr(IntPtr.L9WORD(startdata, 0x4));
            defdict = startdata.copy().incr(IntPtr.L9WORD(startdata, 6));
            endwdp5 = defdict.copy().incr(5 + IntPtr.L9WORD(startdata, 0x8));
            dictdata = startdata.copy().incr(IntPtr.L9WORD(startdata, 0x0a));
            dictdatalen = IntPtr.L9WORD(startdata, 0x0c);
            wordtable = startdata.copy().incr(IntPtr.L9WORD(startdata, 0xe));
            break;
        }

        graphicsVm = new GraphicsVm(osGraphics, L9GameType);
        graphicsVm.scanPicture(StartFile, startdata);

        return true;
    }

    boolean analyseV2(MutableDouble wl) {
        long words = 0, chars = 0;
        int i;
        for (i = 1; i < 256; i++) {
            MutableLong w = new MutableLong(0);
            MutableLong c = new MutableLong(0);
            if (amessageV2(startmd, i, w, c)) {
                words += w.get();
                chars += c.get();
            } else
                return false;
        }
        wl.set(words != 0 ? (double) chars / words : 0.0);
        return true;
    }

    static int depth = 0;

    boolean amessageV2(IntPtr ptr, int msg, MutableLong w, MutableLong c) {
        int n;
        int a;
        if (msg == 0) {
            return false;
        }
        while (--msg != 0) {
            ptr.incr(msglenV2(ptr));
        }
        if (!ptr.inRange())
            return false;
        n = msglenV2(ptr);

        while (--n > 0) {
            ptr.incr();
            a = ptr.get();
            if (a < 3)
                return true;

            if (a >= 0x5e) {
                if (++depth > 10 || !amessageV2(startmdV2.copy().decr(), a - 0x5d, w, c)) {
                    depth--;
                    return false;
                }
                depth--;
            } else {
                char ch = (char) (a + 0x1d);
                if (ch == 0x5f || ch == ' ') {
                    w.incr();
                } else {
                    c.incr();
                }
            }
        }
        return true;
    }

    /* v2 message stuff */

    int msglenV2(IntPtr ptr) {

        /* catch berzerking code */
        if (!ptr.inRange()) {
            return 0;
        }

        int a, i = 0;
        while ((a = ptr.get()) == 0) {
            ptr.incr();

            if (!ptr.inRange()) {
                return 0;
            }

            i += 255;
        }
        i += a;

        return i;
    }

    boolean analyseV1(MutableDouble wl) {
        long words = 0, chars = 0;
        int i;
        for (i = 0; i < 256; i++) {
            MutableLong w = new MutableLong();
            MutableLong c = new MutableLong();
            if (amessageV1(startmd, i, w, c)) {
                words += w.get();
                chars += c.get();
            } else
                return false;
        }

        wl.set(words != 0 ? (double) chars / words : 0.0);
        return true;
    }

    boolean amessageV1(IntPtr ptr, int msg, MutableLong w, MutableLong c) {
        int n;
        int a;

        while (msg-- != 0) {
            ptr.incr(msglenV1(ptr));
        }
        if (!ptr.inRange())
            return false;
        n = msglenV1(ptr);

        while (--n > 0) {
            a = ptr.getPostIncr();
            if (a < 3)
                return true;

            if (a >= 0x5e) {
                if (++depth > 10 || !amessageV1(startmdV2, a - 0x5e, w, c)) {
                    depth--;
                    return false;
                }
                depth--;
            } else {
                char ch = (char) (a + 0x1d);
                if (ch == 0x5f || ch == ' ') {
                    w.incr();
                } else {
                    c.incr();
                }
            }
        }
        return true;
    }

    int msglenV1(IntPtr ptr) {
        IntPtr ptr2 = ptr.copy();
        while (ptr2.inRange() && ptr2.getPostIncr() != 1)
            ;
        return IntPtr.diff(ptr2, ptr);
    }

    void listhandler() {

        IntPtr a4;
        int val;
        IntPtr varPtr;
        int offset;

        if ((code & 0x1f) > 0xa) {
            // codeFollow.info("illegal list access" + (code & 0x1f));
            running = false;
            return;
        }
        a4 = L9Pointers[1 + code & 0x1f].copy();

        if (code >= 0xe0) {
            offset = getVar();
            a4.incr(offset);
            varPtr = getVarPtr();
            val = varPtr.get();
            // codeFollow.info(String.format(" list %d [%d]=Var[%d] (=%d)", code
            // & 0x1f, offset, varPtr.getIndex(), val));

            if (a4.inRange()) {
                a4.put(val);
            } else {
                // codeFollow.warn(" Out of range list access");
            }
        } else if (code >= 0xc0) {
            offset = codeptr.getPostIncr();
            a4.incr(offset);
            varPtr = getVarPtr();
            // codeFollow.info(String.format(" Var[%d] =list %d [%d]",
            // varPtr.getIndex(), code & 0x1f, offset));
            if (a4.inRange()) {
                // codeFollow.info(String.format(" (=%d)", a4.get()));
                varPtr.put(a4.get());
            } else {
                // codeFollow.warn(" Out of range list access");
                varPtr.put(0);
            }
        } else if (code >= 0xa0) {
            /* listv1v */
            offset = getVar();
            a4.incr(offset);
            varPtr = getVarPtr();

            // codeFollow.info(String.format(" Var[%d] =list %d [%d]",
            // varPtr.getIndex(), code & 0x1f, offset));
            if (a4.inRange()) {
                // codeFollow.info(String.format(" (=%d)", a4.get()));
                varPtr.put(a4.get());
            } else {
                // codeFollow.warn(" Out of range list access");
                varPtr.put(0);
            }
        } else {
            offset = codeptr.get();
            codeptr.incr();
            a4.incr(offset);
            varPtr = getVarPtr();
            val = varPtr.get();
            // codeFollow.info(String.format(" list %d [%d]=Var[%d] (=%d)", code
            // & 0x1f, offset, varPtr.getIndex(), val));

            if (a4.inRange()) {
                a4.put(val);
            } else {
                // codeFollow.warn(" Out of range list access");
            }
        }
    }

    public BreakType icyBreak() {

        int code = codeptr.get();
        boolean isInputInstruction = ((code & 0x80) == 0) && ((code & 0x1f) == 7);
        boolean input = isInputInstruction && (ibuffptr == null);
        if (input) {
            return BreakType.INPUT;
        }

        boolean isFunctionInstruction = ((code & 0x80) == 0) && ((code & 0x1f) == 6);
        if (isFunctionInstruction) {
            int d0 = PrimitiveUtils.asByte(codeptr.get(1));
            boolean isDriverCall = (d0 == 1);
            boolean readCh = isDriverCall && (list9startptr.get() == 3);
            if (readCh) {
                return BreakType.READCHAR;
            }
        }

        return BreakType.NONE;
    }

    void executeInstruction() {

        code = codeptr.get();
        codeptr.incr();
//        step++;

        // codeFollow.info(String.format("%d %d (s:%d) %x", step, addr,
        // workspace.getStackPtr(), code));
        if ((code & 0x80) == 0) {
            // codeFollow.info(" = " + codes[code & 0x1f]);
        }

        if ((code & 0x80) != 0)
            listhandler();
        else
            switch (code & 0x1f) {
            case 0:
                Goto();
                break;
            case 1:
                intgosub();
                break;
            case 2:
                intreturn();
                break;
            case 3:
                printnumber();
                break;
            case 4:
                messagev();
                break;
            case 5:
                messagec();
                break;
            case 6:
                function();
                break;
            case 7:
                input();
                break;
            case 8:
                varcon();
                break;
            case 9:
                varvar();
                break;
            case 10:
                _add();
                break;
            case 11:
                _sub();
                break;
            case 12:
                ilins(code & 0x1f);
                break;
            case 13:
                ilins(code & 0x1f);
                break;
            case 14:
                jump();
                break;
            case 15:
                Exit();
                break;
            case 16:
                ifeqvt();
                break;
            case 17:
                ifnevt();
                break;
            case 18:
                ifltvt();
                break;
            case 19:
                ifgtvt();
                break;
            case 20:
                _screen();
                break;
            case 21:
                cleartg();
                break;
            case 22:
                picture();
                break;
            case 23:
                getnextobject();
                break;
            case 24:
                ifeqct();
                break;
            case 25:
                ifnect();
                break;
            case 26:
                ifltct();
                break;
            case 27:
                ifgtct();
                break;
            case 28:
                printinput();
                break;
            case 29:
                ilins(code & 0x1f);
                break;
            case 30:
                ilins(code & 0x1f);
                break;
            case 31:
                ilins(code & 0x1f);
                break;
            }

        codeFollow.flush();
    }

    private void ifgtct() {

        IntPtr x = getVarPtr();
        int d0 = x.get();
        int d1 = getcon();
        IntPtr a0 = getaddr();
        if (d0 > d1) {
            codeptr = a0.copy();
        }
        // codeFollow.info(String.format(" if Var[%d]>%d goto %d (%s)",
        // x.getIndex(), d1,
        // a0.getIndex() - acodeptr.getIndex(), (d0 > d1) ? "Yes" : "No"));
    }

    private void ifltct() {

        IntPtr x = getVarPtr();
        int d0 = x.get();
        int d1 = getcon();
        IntPtr a0 = getaddr();
        if (d0 < d1) {
            codeptr = a0.copy();
        }
        // codeFollow.info(String.format(" if Var[%d]<%d goto %d (%s)",
        // x.getIndex(), d1,
        // a0.getIndex() - acodeptr.getIndex(), (d0 < d1) ? "Yes" : "No"));
    }

    private void ifnect() {

        IntPtr x = getVarPtr();
        int d0 = x.get();
        int d1 = getcon();
        IntPtr a0 = getaddr();
        if (d0 != d1) {
            codeptr = a0.copy();
        }
        // codeFollow.info(String.format(" if Var[%d]!=%d goto %d (%s)",
        // x.getIndex(), d1,
        // a0.getIndex() - acodeptr.getIndex(), (d0 != d1) ? "Yes" : "No"));
    }

    private void ifeqct() {

        IntPtr x = getVarPtr();
        int d0 = x.get();
        int d1 = getcon();
        IntPtr a0 = getaddr();
        if (d0 == d1) {
            codeptr = a0.copy();
        }
        // codeFollow.info(String.format(" if Var[%d]==%d goto %d (%s)",
        // x.getIndex(), d1,
        // a0.getIndex() - acodeptr.getIndex(), (d0 == d1) ? "Yes" : "No"));
    }

    private void ifgtvt() {

        IntPtr x = getVarPtr();
        int d0 = x.get();
        IntPtr y = getVarPtr();
        int d1 = y.get();
        IntPtr a0 = getaddr();
        if (d0 > d1) {
            codeptr = a0.copy();
        }

        // codeFollow.info(String.format(" if Var[%d]>Var[%d] goto %d (%s)",
        // x.getIndex(), y.getIndex(), a0.getIndex()
        // - acodeptr.getIndex(), (d0 > d1) ? "Yes" : "No"));
    }

    private void ifltvt() {

        IntPtr x = getVarPtr();
        int d0 = x.get();
        IntPtr y = getVarPtr();
        int d1 = y.get();
        IntPtr a0 = getaddr();
        if (d0 < d1) {
            codeptr = a0.copy();
        }

        // codeFollow.info(String.format(" if Var[%d]<Var[%d] goto %d (%s)",
        // x.getIndex(), y.getIndex(), a0.getIndex()
        // - acodeptr.getIndex(), (d0 < d1) ? "Yes" : "No"));
    }

    private void ifnevt() {

        IntPtr x = getVarPtr();
        int d0 = x.get();
        IntPtr y = getVarPtr();
        int d1 = y.get();
        IntPtr a0 = getaddr();
        if (d0 != d1) {
            codeptr = a0.copy();
        }

        // codeFollow.info(String.format(" if Var[%d]!=Var[%d] goto %d (%s)",
        // x.getIndex(), y.getIndex(), IntPtr.diff(a0, acodeptr),
        // (d0 != d1) ? "Yes" : "No"));
    }

    private void ifeqvt() {

        IntPtr x = getVarPtr();
        int d0 = x.get();
        IntPtr y = getVarPtr();
        int d1 = y.get();
        IntPtr a0 = getaddr();
        if (d0 == d1) {
            codeptr = a0.copy();
        }

        // codeFollow.info(String.format(" if Var[%d]=Var[%d] goto %d (%s)",
        // x.getIndex(), y.getIndex(), IntPtr.diff(a0, acodeptr),
        // (d0 == d1) ? "Yes" : "No"));
    }

    int[] gnostack = new int[128];
    int[] gnoscratch = new int[32];
    int object, gnosp, numobjectfound, searchdepth, inithisearchpos;

    void initgetobj() {
        int i;
        numobjectfound = 0;
        object = 0;
        for (i = 0; i < 32; i++) {
            gnoscratch[i] = 0;
        }
    }

    private void getnextobject() {

        int d2, d3, d4;
        IntPtr hisearchposvar;
        IntPtr searchposvar;

        d2 = getVar();
        hisearchposvar = getVarPtr();
        searchposvar = getVarPtr();
        d3 = hisearchposvar.get();
        d4 = searchposvar.get();

        /* gnoabs */
        do {
            if ((d3 | d4) == 0) {
                /* initgetobjsp */
                gnosp = 128;
                searchdepth = 0;
                initgetobj();
                break;
            }

            if (numobjectfound == 0)
                inithisearchpos = d3;

            /* gnonext */
            do {
                if (d4 == list2ptr.get(++object)) {
                    /* gnomaybefound */
                    int d6 = list3ptr.get(object) & 0x1f;
                    if (d6 != d3) {
                        if (d6 == 0 || d3 == 0) {
                            continue;
                        }
                        if (d3 != 0x1f) {
                            gnoscratch[d6] = d6;
                            continue;
                        }
                        d3 = d6;
                    }
                    /* gnofound */
                    numobjectfound++;
                    gnostack[--gnosp] = object;
                    gnostack[--gnosp] = 0x1f;

                    hisearchposvar.put(d3);
                    searchposvar.put(d4);
                    getVarPtr().put(object);
                    getVarPtr().put(numobjectfound);
                    getVarPtr().put(searchdepth);
                    return;
                }
            } while (object <= d2);

            if (inithisearchpos == 0x1f) {
                gnoscratch[d3] = 0;
                d3 = 0;

                /* gnoloop */
                do {
                    if (gnoscratch[d3] != 0) {
                        gnostack[--gnosp] = d4;
                        gnostack[--gnosp] = d3;
                    }
                } while (++d3 < 0x1f);
            }
            /* gnonewlevel */
            if (gnosp != 128) {
                d3 = gnostack[gnosp++];
                d4 = gnostack[gnosp++];
            } else
                d3 = d4 = 0;

            numobjectfound = 0;
            if (d3 == 0x1f)
                searchdepth++;

            initgetobj();
        } while (d4 != 0);

        /* gnofinish */
        /* gnoreturnargs */
        hisearchposvar.put(0);
        searchposvar.put(0);
        getVarPtr().put(0);
        object = 0;
        getVarPtr().put(numobjectfound);
        getVarPtr().put(searchdepth);
    }

    private void picture() {

        show_picture(getVar());
    }

    private void show_picture(int pic) {

        /*
         * Some games don't call the screen() opcode before drawing graphics, so here graphics are enabled if necessary.
         */
        if (!screencalled && !l9textmode) {
            graphicsVm.detect_gfx_mode(startdata, firstLine.toString());
            l9textmode = true;
            osGraphics.setGraphicsMode(1);
        }
        graphicsVm.show_picture(pic);
    }

    private void _screen() {

        if (L9GameType == L9GameTypes.V3 && firstLine.length() == 0) {
            if (codeptr.getPostIncr() != 0)
                codeptr.incr();
            return;
        }

        int mode = 0;

        graphicsVm.detect_gfx_mode(startdata, firstLine.toString());
        l9textmode = PrimitiveUtils.asBoolean(codeptr.get());
        codeptr.incr();

        if (l9textmode) {
            if (L9GameType == L9GameTypes.V4)
                mode = 2;
        }
        osGraphics.setGraphicsMode(mode);

        screencalled = true;

        // codeFollow.print(String.format(" (%s)", (l9textmode ? "graphics" :
        // "text")));

        if (l9textmode) {
            codeptr.incr();
            /* clearg */
            /* gintclearg */
            osGraphics.clear();

            /* title pic */
            if (showtitle && mode == 2) {
                showtitle = false;
                osGraphics.showBitmap(0, 0, 0);
            }
        }
        /* screent */
    }

    /* bug */
    void exit1(MutableInteger d4, MutableInteger d5, int d6, int d7) {

        IntPtr a0 = absdatablock.copy();
        int d1 = d7, d0;

        boolean notfn4 = false;
        if (--d1 != 0) {
            do {
                d0 = a0.get();
                if (L9GameType == L9GameTypes.V4) {
                    if ((d0 == 0) && (a0.get(1) == 0)) {
                        notfn4 = true;
                        break;
                    }
                }
                a0.incr(2);
            } while (((d0 & 0x80) == 0) || (--d1 != 0));
        }

        if (!notfn4) {
            do {
                d4.set(a0.getPostIncr());
                if ((d4.get() & 0xf) == d6) {
                    d5.set(a0.get());
                    return;
                }
                a0.incr();
            } while ((d4.get() & 0x80) == 0);
        }

        /* notfn4 */
        d6 = exitreversaltable[d6];
        a0 = absdatablock.copy();
        d5.set(1);

        do {
            d4.set(a0.getPostIncr());
            if ((d4.get() & 0x10) == 0 || (d4.get() & 0xf) != d6) {
                a0.incr();
            } else if (a0.getPostIncr() == d7) {
                return;
            }
            /* exit6noinc */
            if ((d4.get() & 0x80) != 0) {
                d5.incr();
            }
        } while (d4.get() != 0);
        d5.set(0);

        return;
    }

    private void Exit() {

        MutableInteger d4 = new MutableInteger();
        MutableInteger d5 = new MutableInteger();
        IntPtr x = getVarPtr();
        int d7 = x.get();
        IntPtr y = getVarPtr();
        int d6 = y.get();

        // codeFollow.info(String.format(" d7=%d d6=%d", d7, d6));

        exit1(d4, d5, d6, d7);

        x = getVarPtr();
        x.put((d4.get() & 0x70) >> 4);
        y = getVarPtr();
        y.put(d5.get());

        // codeFollow.info(String.format(" Var[%d]=%d(d4=%s) Var[%d]=%s",
        // x.getIndex(), (d4.get() & 0x70) >> 4, d4,
        // y.getIndex(), d5));

    }

    private void jump() {

        int d0 = IntPtr.L9WORD(codeptr, 0);
        IntPtr a0;
        codeptr.incr(2);

        a0 = acodeptr.copy().incr(((d0 + ((getVar()) << 1)) & 0xffff));
        codeptr = acodeptr.copy().incr(IntPtr.L9WORD(a0, 0));
    }

    private void ilins(int i) {

        // codeFollow.info(" ilins(" + i + ")");
    }

    private void _sub() {

        IntPtr x = getVarPtr();
        int d0 = x.get();
        IntPtr y = getVarPtr();
        y.put(y.get() - d0);

        // codeFollow.info(String.format(" Var[%d]-=Var[%d] (-=%d)",
        // y.getIndex(), x.getIndex(), d0));
    }

    private void _add() {

        IntPtr x = getVarPtr();
        int d0 = x.get();
        IntPtr y = getVarPtr();
        y.put(y.get() + d0);

        // codeFollow.info(String.format(" Var[%d]+=Var[%d] (+=%d)",
        // y.getIndex(), x.getIndex(), d0));
    }

    private void varvar() {

        IntPtr x = getVarPtr();
        int d6 = x.get();
        IntPtr y = getVarPtr();
        y.put(d6);

        // codeFollow.info(String.format(" Var[%d]=Var[%d] (=%s)", y.getIndex(),
        // x.getIndex(), d6));
    }

    private void varcon() {

        int d6 = getcon();
        setVar(d6);
    }

    private IntPtr getVarPtr() {

        return workspace.getVarPtr(codeptr.getPostIncr());
    }

    private int getVar() {

        return workspace.getVar(codeptr.getPostIncr());
    }

    private void setVar(int value) {

        int offset = codeptr.getPostIncr();
        // codeFollow.info(" Var[" + offset + "]=" + value);
        workspace.setVar(offset, value);
    }

    private void cleartg() {

        int d0 = codeptr.get();
        codeptr.incr();

        // codeFollow.print(String.format(" (%s)", (d0 != 0) ? "graphics" :
        // "text"));

        if (d0 != 0) {
            /* clearg */
            if (l9textmode)
                /* gintclearg */
                osGraphics.clear();
        }
        /* cleart */
        /* oswrch(0x0c) */
    }

    private int getcon() {

        if ((code & 64) != 0) {
            int ret = PrimitiveUtils.asByte(codeptr.get());
            codeptr.incr();
            return ret;
        } else
            return movewa5d0();
    }

    private void input() {

        /*
         * if corruptinginput() returns false then, input will be called again next time around instructionloop, this is
         * used when save() and restore() are called out of line
         */

        codeptr.decr();
        if (L9GameType == L9GameTypes.V2) {
            // PassByRefInteger wordcount = new PassByRefInteger();
            // if (inputV2(wordcount))
            // {
            // L9BYTE *obuffptr=(L9BYTE*) obuff;
            // codeptr.incr();
            // getvar().put(*obuffptr++);
            // getvar().put(*obuffptr++);
            // getvar().put(*obuffptr);
            // getvar().put(wordcount.get());
            // }
            throw new UnsupportedOperationException("input");
        } else if (corruptinginput()) {
            codeptr.incr(5);
        }
    }

    boolean checkHash(String inputStr) {
        if ("#save".equalsIgnoreCase(inputStr)) {
            save();
            return true;
        }
        if ("#restore".equalsIgnoreCase(inputStr)) {
            restore();
            return true;
        }
        if ("#quit".equalsIgnoreCase(inputStr)) {
            running = false;
            printstring("\rGame Terminated\r");
            return true;
        }
        return false;
    }

    boolean corruptinginput() {

        IntPtr a0, a2, a6;
        int d0, d1, d2, abrevword;

        list9ptr = list9startptr.copy();

        if (ibuffptr == null) {
            /* flush */
            osText.flush();
            lastchar = '.';
            /* get input */
            String inputStr = osText.input();
            if (inputStr == null || inputStr.length() == 0) {
                return false; /* fall through */
            }
            if (checkHash(inputStr)) {
                return false;
            }

            /* check for invalid chars */
            int[] ibuff = new int[inputStr.length() + 1];
            int i;
            for (i = 0; i < inputStr.length(); i++) {
                char c = inputStr.charAt(i);
                if (CharacterUtils.isAsciiLetterOrDigit(c)) {
                    ibuff[i] = c;
                } else {
                    ibuff[i] = ' ';
                }
            }
            ibuff[i] = 0;

            /* force CR but prevent others */
            osText.printChar(lastactualchar = '\r');

            ibuffptr = new IntPtr("ibuff", ibuff);
        }

        a2 = obuffptr.copy();
        a6 = ibuffptr.copy();

        /* ip05 */
        // LOG.debug("  ip05");
        while (true) {
            d0 = a6.getPostIncr();
            if (d0 == 0) {
                ibuffptr = null;
                IntPtr.L9SETWORD(list9ptr, 0, 0);
                return true;
            }
            if (!partword((char) d0)) {
                break;
            }
            if (d0 != 0x20) {
                ibuffptr = a6.copy();
                IntPtr.L9SETWORD(list9ptr, 0, d0);
                IntPtr.L9SETWORD(list9ptr, 2, 0);
                a2.put(0x20);
                return true;
            }
        }

        a6.decr();
        /* ip06loop */
        // LOG.debug("  ip06loop");
        do {
            d0 = a6.getPostIncr();
            if (partword((char) d0)) {
                break;
            }
            d0 = Character.toLowerCase((char) d0);
            a2.putPostIncr(d0);
            // TODO don't want to copy
        } while (IntPtr.lt(a2, obuffptr.copy().incr(0x1f)));
        /* ip06a */
        // LOG.debug("  ip06a");
        a2.put(0x20);
        a6.decr();
        ibuffptr = a6.copy();
        abrevword = -1;

        list9ptr = list9startptr.copy();
        /* setindex */
        // LOG.debug("  setindex");
        a0 = dictdata.copy();
        d2 = dictdatalen;
        d0 = obuffptr.get() - 0x61;
        if (d0 < 0) {
            a6 = defdict.copy();
            d1 = 0;
        } else {
            /* ip10 */
            // LOG.debug("  ip10");
            d1 = 0x67;
            if (d0 < 0x1a) {
                d1 = d0 << 2;
                d0 = obuffptr.get(1);
                if (d0 != 0x20) {
                    d1 += ((d0 - 0x61) >> 3) & 3;
                }
            }
            /* ip13 */
            // LOG.debug("  ip13");
            if (d1 >= d2) {
                checknumber();
                return true;
            }
            a0.incr(d1 << 2);
            a6 = startdata.copy().incr(IntPtr.L9WORD(a0, 0));
            d1 = IntPtr.L9WORD(a0, 2);
        }
        /* ip13gotwordnumber */
        // LOG.debug("  ip13gotwrodnumber");

        initunpack(a6);
        /* ip14 */
        // LOG.debug("  ip14");
        d1--;
        do {
            d1++;
            if (unpackword()) {
                /* ip21b */
                // LOG.debug("  ip21b");
                if (abrevword == -1) {
                    break; /* goto ip22 */
                } else {
                    d0 = abrevword; /* goto ip18b */
                }
            } else {
                IntPtr a1 = threechars.copy();
                int d6 = -1;

                a0 = obuffptr.copy();
                /* ip15 */
                // LOG.debug("  ip15");
                do {
                    d6++;
                    d0 = Character.toLowerCase((char) (a1.getPostIncr() & 0x7f));
                    d2 = a0.getPostIncr();
                } while (d0 == d2);

                if (d2 != 0x20) {
                    /* ip17 */
                    // LOG.debug("  ip17");
                    if (abrevword == -1) {
                        continue;
                    } else {
                        d0 = -1;
                    }
                } else if (d0 == 0) {
                    d0 = d1;
                } else if (abrevword != -1) {
                    break;
                } else if (d6 >= 4) {
                    d0 = d1;
                } else {
                    abrevword = d1;
                    continue;
                }
            }
            /* ip18b */
            // LOG.debug("  ip18b");
            findmsgequiv(d1);

            abrevword = -1;
            if (IntPtr.neq(list9ptr, list9startptr)) {
                IntPtr.L9SETWORD(list9ptr, 0, 0);
                return true;
            }
        } while (true);
        /* ip22 */
        // LOG.debug("  ip22");
        checknumber();
        return true;
    }

    private void printinput() {

        IntPtr ptr = obuffptr.copy();
        int c;
        while ((c = ptr.getPostIncr()) != ' ') {
            printchar(c);
        }
    }

    void checknumber() {

        if (obuffptr.get(0) >= 0x30 && obuffptr.get(0) < 0x3a) {
            if (L9GameType == L9GameTypes.V4) {
                list9ptr.put(1);
                IntPtr.L9SETWORD(list9ptr, 1, readdecimal(obuffptr));
                IntPtr.L9SETWORD(list9ptr, 3, 0);
            } else {
                IntPtr.L9SETDWORD(list9ptr, 0, readdecimal(obuffptr));
                IntPtr.L9SETWORD(list9ptr, 4, 0);
            }
        } else {
            IntPtr.L9SETWORD(list9ptr, 0, 0x8000);
            IntPtr.L9SETWORD(list9ptr, 2, 0);
        }
    }

    int readdecimal(IntPtr buff) {

        char[] cbuff = new char[buff.size()];
        for (int i = 0; i < buff.size(); i++) {
            cbuff[i] = (char) buff.get();
        }
        try {
            return Integer.parseInt(new String(cbuff));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    boolean partword(char c) {

        c = Character.toLowerCase(c);

        if (c == 0x27 || c == 0x2d) {
            return false;
        }
        if (c < 0x30) {
            return true;
        }
        if (c < 0x3a) {
            return false;
        }
        if (c < 0x61) {
            return true;
        }
        if (c < 0x7b) {
            return false;
        }
        return true;
    }

    boolean initunpack(IntPtr ptr) {

        initdict(ptr);
        unpackd3 = 0x1c;
        return unpackword();
    }

    boolean unpackword() {

        IntPtr a3;

        if (unpackd3 == 0x1b) {
            return true;
        }

        a3 = threechars.copy().incr(unpackd3 & 3);

        /* uw01 */
        while (true) {
            int d0 = getdictionarycode();
            if (IntPtr.gteq(dictptr, endwdp5)) {
                return true;
            }
            if (d0 >= 0x1b) {
                a3.put(0);
                unpackd3 = d0;
                return false;
            }
            a3.putPostIncr(getdictionary(d0));
        }
    }

    void displaywordref(int Off) {

        // codeFollow.print(" " + Off + " ");

        wordcase = false;
        int d5 = (Off >> 12) & 7;
        Off &= 0xfff;
        if (Off < 0xf80) {
            /* dwr01 */
            IntPtr a0, oPtr, a3;
            int d0, d2, i;

            if (mdtmode == 1) {
                printchar(0x20);
            }
            mdtmode = 1;

            /* setindex */
            a0 = dictdata.copy();
            d2 = dictdatalen;

            /* dwr02 */
            oPtr = a0.copy();
            while (d2 != 0 && Off >= IntPtr.L9WORD(a0, 2)) {
                a0.incr(4);
                d2--;
            }
            /* dwr04 */
            if (IntPtr.eq(a0, oPtr)) {
                a0 = defdict.copy();
            } else {
                a0.decr(4);
                Off -= IntPtr.L9WORD(a0, 2);
                a0 = startdata.copy().incr(IntPtr.L9WORD(a0, 0));
            }
            /* dwr04b */
            Off++;
            initdict(a0);
            a3 = threechars.copy(); /*
                                     * a3 not set in original, prevent possible spam
                                     */

            /* dwr05 */
            while (true) {
                d0 = getdictionarycode();
                if (d0 < 0x1c) {
                    /* dwr06 */
                    if (d0 >= 0x1a) {
                        d0 = getlongcode();
                    } else
                        d0 += 0x61;
                    a3.putPostIncr(d0);
                } else {
                    d0 &= 3;
                    a3 = threechars.copy().incr(d0);
                    if (--Off == 0) {
                        break;
                    }
                }
            }
            for (i = 0; i < d0; i++) {
                printautocase(threechars.get(i), d5);
            }

            /* dwr10 */
            while (true) {
                d0 = getdictionarycode();
                if (d0 >= 0x1b) {
                    return;
                }
                printautocase(getdictionary(d0), d5);
            }
        }

        else {
            if ((d5 & 2) != 0) {
                printchar(0x20); /* prespace */
            }
            mdtmode = 2;
            Off &= 0x7f;
            if (Off != 0x7e) {
                printchar((char) Off);
            }
            if ((d5 & 1) != 0) {
                printchar(0x20); /* postspace */
            }
        }
    }

    void initdict(IntPtr ptr) {

        dictptr = ptr.copy();
        unpackcount = 8;
    }

    int getdictionarycode() {

        if (unpackcount != 8) {
            return unpackbuf[unpackcount++];
        } else {
            /* unpackbytes */
            int d1, d2;
            d1 = dictptr.getPostIncr();
            unpackbuf[0] = d1 >> 3;
            d2 = dictptr.getPostIncr();
            unpackbuf[1] = ((d2 >> 6) + (d1 << 2)) & 0x1f;
            d1 = dictptr.getPostIncr();
            unpackbuf[2] = (d2 >> 1) & 0x1f;
            unpackbuf[3] = ((d1 >> 4) + (d2 << 4)) & 0x1f;
            d2 = dictptr.getPostIncr();
            unpackbuf[4] = ((d1 << 1) + (d2 >> 7)) & 0x1f;
            d1 = dictptr.getPostIncr();
            unpackbuf[5] = (d2 >> 2) & 0x1f;
            unpackbuf[6] = ((d2 << 3) + (d1 >> 5)) & 0x1f;
            unpackbuf[7] = d1 & 0x1f;
            unpackcount = 1;
            return unpackbuf[0];
        }
    }

    int getlongcode() {

        int d0, d1;
        d0 = getdictionarycode();
        if (d0 == 0x10) {
            wordcase = true;
            d0 = getdictionarycode();
            return getdictionary(d0); /* reentrant? */
        }
        d1 = getdictionarycode();
        return 0x80 | ((d0 << 5) & 0xe0) | (d1 & 0x1f);
    }

    int getdictionary(int d0) {

        if (d0 >= 0x1a)
            return getlongcode();
        else
            return d0 + 0x61;
    }

    void printautocase(int d0, int d5) {

        if ((d0 & 128) != 0) {
            printchar(d0);
        } else {
            if (wordcase) {
                printchar(Character.toUpperCase((char) d0));
            } else if (d5 < 6) {
                printchar((char) d0);
            } else {
                wordcase = false;
                printchar(Character.toUpperCase((char) d0));
            }
        }
    }

    void printchar(int c) {

        char ch = (char) c;

        if ((ch & 128) != 0) {
            lastchar = ch &= 0x7f;
        } else if (ch != 0x20 && ch != 0x0d && (ch < '\"' || ch >= '.')) {
            if (lastchar == '!' || lastchar == '?' || lastchar == '.') {
                ch = Character.toUpperCase(ch);
            }
            lastchar = ch;
        }
        /* eat multiple CRs */
        if (ch != 0x0d || lastactualchar != 0x0d) {

            osText.printChar(ch);
            if (firstLine.length() < 96) {
                firstLine.append(Character.toLowerCase(ch));
            }
        }
        lastactualchar = ch;
    }

    private void printnumber() {

        printdecimald0(getVar());
    }

    void printdecimald0(int d0) {
        printstring(Integer.toString(d0));
    }

    void findmsgequiv(int d7) {

        // LOG.debug("findmsgequiv(" + d7 + ")");

        int d4 = -1, d0, atmp;
        IntPtr a2 = startmd.copy();

        do {
            d4++;
            if (IntPtr.gt(a2, endmd)) {
                // LOG.debug("a2 > endmd");
                return;
            }
            d0 = a2.get();
            if ((d0 & 0x80) != 0) {
                // LOG.debug("(d0 & 0x80) != 0");
                a2.incr();
                d4 += d0 & 0x7f;
            } else if ((d0 & 0x40) != 0) {
                // LOG.debug("(d0 & 0x40) != 0");
                int d6 = getmdlength(a2);
                // LOG.debug("getmdlength = " + d6);

                do {
                    int d1;
                    if (d6 == 0) {
                        // LOG.debug("d6 == 0");
                        break;
                    }

                    d1 = a2.getPostIncr();
                    d6--;
                    if ((d1 & 0x80) != 0) {
                        // LOG.debug("(d1 & 0x80) != 0");
                        if (d1 < 0x90) {
                            // LOG.debug("d1 < 0x90");
                            a2.incr();
                            d6--;
                        } else {
                            // LOG.debug("d1 >= 0x90");
                            d0 = (d1 << 8) + a2.getPostIncr();
                            d6--;
                            if (d7 == (d0 & 0xfff)) {
                                // LOG.debug("d7 == (d0 & 0xfff)");
                                d0 = ((d0 << 1) & 0xe000) | d4;
                                // LOG.debug("Putting " + (d0 & 0xff) + ", " +
                                // (d0 >> 8));
                                list9ptr.put(1, d0 & 0xff);
                                list9ptr.put(0, d0 >> 8);
                                list9ptr.incr(2);
                                // TODO don't want to copy
                                if (IntPtr.gteq(list9ptr, list9startptr.copy().incr(0x20))) {
                                    // LOG.debug("list9ptr >= list9startptr + 0x20");
                                    return;
                                }
                            }
                        }
                    }
                } while (true);
            } else {
                atmp = getmdlength(a2);
                a2.incr(atmp);
            }
        } while (true);
    }

    private void function() {

        int d0 = PrimitiveUtils.asByte(codeptr.get());
        codeptr.incr();
        // codeFollow.info(String.format(" %s", (d0 == 250) ? "printstr" :
        // functions[d0 - 1]));

        switch (d0) {
        case 1:
            calldriver();
            break;
        case 2:
            L9Random();
            break;
        case 3:
            save();
            break;
        case 4:
            NormalRestore();
            break;
        case 5:
            workspace.clearWorkspace();
            break;
        case 6:
            workspace.resetStackPtr();
            break;
        case 250:
            printstring(codeptr);
            while (codeptr.get() != 0) {
                codeptr.incr();
            }
            break;

        default:
            ilins(d0);
        }

    }

    private void printstring(IntPtr base) {

        StringBuilder sb = new StringBuilder();
        IntPtr ptr = base.copy();
        char ch = (char) PrimitiveUtils.asByte(ptr.get());
        while (ch != 0) {
            ptr.incr();
            sb.append(ch);
            ch = (char) PrimitiveUtils.asByte(ptr.get());
        }

    }

    private void NormalRestore() {

        throw new UnsupportedOperationException("NormalRestore");
    }

    private void save() {

        workspace.setCodePtr(IntPtr.diff(codeptr, acodeptr));

        try {
            osFile.saveFile(workspace);
            printstring("\rGame saved.\r");
        } catch (IOException e) {
            printstring("\rUnable to save game.\r");
        }
    }

    void restore() {
        try {
            GameState temp = osFile.loadFile();
            printstring("\rGame restored.\r");

            // TODO handle V1 formats only do partial restore of workspace
            // TODO need to include game id in save so only load game state into
            // right game type

            /* full restore */
            workspace.updateFrom(temp);
            codeptr = acodeptr.copy().incr(workspace.getCodePtr());
        } catch (IOException e) {
            printstring("\rUnable to restore game.\r");
        }
    }

    private void L9Random() {

        // codeFollow.info(" " + randomseed);
        randomseed = ((((randomseed << 8) + 0x0a - randomseed) << 2) + randomseed + 1) & 0xffff;
        getVarPtr().put(randomseed & 0xff);
        // codeFollow.info(" " + randomseed);
    }

    private void calldriver() {

        IntPtr a6 = list9startptr.copy();
        int d0 = a6.get();
        a6.incr();

        // codeFollow.info(String.format(" %s", drivercalls[d0]));

        if (d0 == 0x16 || d0 == 0x17) {
            int d1 = a6.get();
            if (d1 > 0xfa) {
                a6.put(1);
            } else if (d1 + 1 >= RAMSAVESLOTS) {
                a6.put(255);
            } else {
                a6.put(0);
                if (d0 == 0x16) {
                    ramsave(d1 + 1);
                } else {
                    ramload(d1 + 1);
                }
            }
            list9startptr.put(a6.get());
        } else if (d0 == 0x0b) {
            String NewName = LastGame;

            if (a6.get() == 0) {
                printstring("\rSearching for next sub-game file.\r");
                if (!osFile.getGameFile(NewName, MAX_PATH)) {
                    printstring("\rFailed to load game.\r");
                    return;
                }
            } else {
                osFile.setFileNumber(NewName, MAX_PATH, a6.get());
            }
            LoadGame2(NewName, null);
        } else {
            driver(d0, a6);
        }
    }

    private void driver(int d0, IntPtr a6) {

        switch (d0) {
        case 0:
            init(a6);
            break;
        case 0x0c:
            randomnumber(a6);
            break;
        case 0x10:
            driverclg(a6);
            break;
        case 0x11:
            _line(a6);
            break;
        case 0x12:
            fill(a6);
            break;
        case 0x13:
            driverchgcol(a6);
            break;
        case 0x01:
            drivercalcchecksum(a6);
            break;
        case 0x02:
            driveroswrch(a6);
            break;
        case 0x03:
            driverosrdch(a6);
            break;
        case 0x05:
            driversavefile(a6);
            break;
        case 0x06:
            driverloadfile(a6);
            break;
        case 0x07:
            settext(a6);
            break;
        case 0x08:
            resettask(a6);
            break;
        case 0x04:
            driverinputline(a6);
            break;
        case 0x09:
            returntogem(a6);
            break;
        /*
         * case 0x16: ramsave(a6); break; case 0x17: ramload(a6); break;
         */
        case 0x19:
            lensdisplay(a6);
            break;
        case 0x1e:
            allocspace(a6);
            break;
        /* v4 */
        case 0x0e:
            driver14(a6);
            break;
        case 0x20:
            showbitmap(a6);
            break;
        case 0x22:
            checkfordisc(a6);
            break;
        }
    }

    private void init(IntPtr a6) {

        // no implementation in level9.c
    }

    private void randomnumber(IntPtr a6) {

        IntPtr.L9SETWORD(a6, 0, new Random().nextInt());
    }

    private void driverclg(IntPtr a6) {

        // no implementation in level9.c
    }

    private void _line(IntPtr a6) {

        // no implementation in level9.c
    }

    private void fill(IntPtr a6) {

        // no implementation in level9.c
    }

    private void checkfordisc(IntPtr a6) {

        a6.put(0);
        list9startptr.put(2, 0);
    }

    private void showbitmap(IntPtr a6) {

        // TODO
        // os_show_bitmap(a6.get(1),a6.get(3),a6.get(5));
    }

    private void driver14(IntPtr a6) {

        a6.put(0);
    }

    private void allocspace(IntPtr a6) {

        // no implementation in level9.c
    }

    private void lensdisplay(IntPtr a6) {

        // codeFollow.info(" lensdisplay");

        printstring("\rLenslok code is ");
        printchar(a6.get());
        printchar(a6.get(1));
        printchar('\r');

        osText.flush();
    }

    private void returntogem(IntPtr a6) {

        // no implementation in level9.c
    }

    private void driverinputline(IntPtr a6) {

        // no implementation in level9.c
    }

    private void resettask(IntPtr a6) {

        // no implementation in level9.c
    }

    private void settext(IntPtr a6) {

        // no implementation in level9.c
    }

    private void driverloadfile(IntPtr a6) {

        // no implementation in level9.c
    }

    private void driversavefile(IntPtr a6) {

        // no implementation in level9.c
    }

    private void driveroswrch(IntPtr a6) {

        // no implementation in level9.c
    }

    private void driverosrdch(IntPtr a6) {

        a6.put(osText.readChar(20));
    }

    private void drivercalcchecksum(IntPtr a6) {

        // no implementation in level9.c
    }

    private void driverchgcol(IntPtr a6) {

        // no implementation in level9.c
    }

    private void LoadGame2(String newName, Object object) {

        // codeFollow.info(String.format("LoadGame2(%s)", newName));
    }

    public void printstring(String str) {

        for (char c : str.toCharArray()) {
            printchar(c);
        }
    }

    public void ramsave(int x) {

        osFile.ramSave(x, RAMSAVESLOTS, workspace.varTable);
    }

    public void ramload(int x) {

        workspace.updateVarTable(osFile.ramLoad(x));
    }

    void messagev() {

        if (L9GameType == L9GameTypes.V2) {
            printmessageV2(getVar());
        } else {
            printmessage(getVar());
        }
    }

    void messagec() {

        if (L9GameType == L9GameTypes.V1 || L9GameType == L9GameTypes.V2) {
            printmessageV2(getcon());
        } else {
            printmessage(getcon());
        }
    }

    private void printmessageV2(int msg) {

        if (L9MsgType == L9MsgTypes.V2) {
            displaywordV2(startmd, msg);
        } else {
            displaywordV1(startmd, msg);
        }
    }

    void displaywordV2(IntPtr ptr, int msg) {
        int n;
        if (msg == 0) {
            return;
        }
        while (--msg != 0) {
            ptr.incr(msglenV2(ptr));
        }
        n = msglenV2(ptr);

        int a = 0;
        while (--n > 0) {
            ptr.incr();
            a = ptr.get();
            if (a < 3) {
                return;
            }

            if (a >= 0x5e) {
                displaywordV2(startmdV2.copy().decr(), a - 0x5d);
            } else {
                printcharV2((char) (a + 0x1d));
            }
        }
    }

    void printcharV2(char c) {
        if (c == 0x25) {
            c = 0xd;
        } else if (c == 0x5f) {
            c = 0x20;
        }

        // FIXME passing in random d5
        printautocase(c, 0);
    }

    void displaywordV1(IntPtr ptr, int msg) {

        throw new UnsupportedOperationException("displaywordV1");
        // int n;
        // L9BYTE a;
        // while (msg--)
        // {
        // ptr+=msglenV1(&ptr);
        // }
        // n=msglenV1(&ptr);
        //
        // while (--n>0)
        // {
        // a=*ptr++;
        // if (a<3) return;
        //
        // if (a>=0x5e) displaywordV1(startmdV2,a-0x5e);
        // else printcharV2((char)(a+0x1d));
        // }
    }

    private void printmessage(int msg) {

        IntPtr Msgptr = startmd.copy();
        int Data;

        int len, msgtmp;
        int Off;

        while (msg > 0 && IntPtr.lt(Msgptr, endmd)) {
            Data = Msgptr.get();
            if ((Data & 128) != 0) {
                Msgptr.incr();
                msg -= Data & 0x7f;
            } else {
                msgtmp = getmdlength(Msgptr);
                Msgptr.incr(msgtmp);
            }
            msg--;
        }
        if (msg < 0 || ((Msgptr.get() & 128) != 0)) {
            return;
        }

        len = getmdlength(Msgptr);
        if (len == 0)
            return;

        while (len != 0) {
            Data = Msgptr.getPostIncr();
            len--;
            if ((Data & 128) != 0) {
                /* long form (reverse word) */
                Off = (Data << 8) + Msgptr.getPostIncr();
                len--;
            } else {
                Off = (wordtable.get(Data * 2) << 8) + wordtable.get(Data * 2 + 1);
            }
            if (Off == 0x8f80) {
                break;
            }
            displaywordref(Off);
        }
    }

    int getmdlength(IntPtr Ptr) {

        int tot = 0, len;
        do {

            len = (Ptr.getPostIncr() - 1) & 0x3f;
            tot += len;
        } while (len == 0x3f);
        return tot;
    }

    private void intreturn() {

        try {
            int delta = workspace.popStack();
            codeptr = acodeptr.copy().incr(delta);
        } catch (StackUnderflowException e) {
            // codeFollow.warn("\rStack underflow error\r");
            running = false;
        }
    }

    private void intgosub() {

        try {
            IntPtr newcodeptr = getaddr();
            int delta = (IntPtr.diff(codeptr, acodeptr));
            workspace.pushStack(delta);
            codeptr = newcodeptr;
        } catch (StackOverflowException e) {
            // codeFollow.warn("\rStack overflow error\r");
            running = false;
        }
    }

    private void Goto() {

        codeptr = getaddr();
    }

    IntPtr getaddr() {

        if ((code & 0x20) != 0) {
            /* getaddrshort */
            int diff = PrimitiveUtils.asSignedByte(codeptr.get());
            codeptr.incr();

            return codeptr.copy().incr(diff - 1);
        } else {
            return acodeptr.copy().incr(movewa5d0());
        }
    }

    int movewa5d0() {

        int ret = IntPtr.L9WORD(codeptr, 0);
        codeptr.incr(2);
        return ret;
    }

    public boolean loadGame(IntPtr StartFile) {

        workspace = gameStateFactory.create();
        boolean ret = LoadGame2(StartFile);
        showtitle = true;
        workspace.clearWorkspace();
        workspace.resetStackPtr();
        workspace.clearListArea();
        return ret;
    }

    public void startGame() {
        running = true;
    }

    public void runGame() {

        executeInstruction();
    }

    public boolean isRunning() {
        return running;
    }

    public void stopGame() {
        running = false;
    }

    public GameState getGameState() {
        return workspace;
    }

    public void setGameState(GameState gameState) {
        workspace.updateFrom(gameState);
    }
}
