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

import java.util.logging.Logger;

import uk.co.threeonefour.android.snowball.basics.lang.PrimitiveUtils;
import uk.co.threeonefour.android.snowball.level9j.ptr.IntPtr;
import uk.co.threeonefour.android.snowball.level9j.ptr.StaticIntPtr;
import uk.co.threeonefour.android.snowball.level9j.vm.Vm.L9GameTypes;

public class Scan {

    private static Logger LOG = Logger.getLogger(Scan.class.getName());

    static final class ScanContext {
        boolean JumpKill;
        int Size;
        int Min;
        int Max;
        boolean DriverV4;
    }

    private L9GameTypes L9GameType;

    public L9GameTypes getGameType() {
        return L9GameType;
    }

    public final int run(IntPtr StartFile) {

        int FileSize = StartFile.size();
        int[] Chk = new int[FileSize + 1];
        IntPtr Image = new StaticIntPtr("StartFile", new int[FileSize]);

        int i, num, MaxSize = 0;
        int j;
        int d0 = 0, l9, md, ml, dd, dl;
        int Offset = -1;

        Chk[0] = 0;
        for (i = 1; i <= FileSize; i++) {
            Chk[i] = PrimitiveUtils.asByte(Chk[i - 1] + StartFile.get(i - 1));
        }

        for (i = 0; i < FileSize - 33; i++) {

            num = IntPtr.L9WORD(StartFile, i) + 1;

            if (num > 0x2000 && i + num <= FileSize && Chk[i + num] == Chk[i]) {
                md = IntPtr.L9WORD(StartFile, i + 0x2);
                ml = IntPtr.L9WORD(StartFile, i + 0x4);
                dd = IntPtr.L9WORD(StartFile, i + 0xa);
                dl = IntPtr.L9WORD(StartFile, i + 0xc);

                if (ml > 0 && md > 0 && i + md + ml <= FileSize && dd > 0 && dl > 0 && i + dd + dl * 4 <= FileSize) {
                    /* v4 files may have acodeptr in 8000-9000, need to fix */
                    for (j = 0; j < 12; j++) {
                        d0 = IntPtr.L9WORD(StartFile, i + 0x12 + j * 2);
                        if (j != 11 && d0 >= 0x8000 && d0 < 0x9000) {
                            if (d0 >= 0x8000 + GameState.LISTAREASIZE)
                                break;
                        } else if (i + d0 > FileSize)
                            break;
                    }
                    /* list9 ptr must be in listarea, acode ptr in data */
                    if (j < 12 /* || (d0>=0x8000 && d0<0x9000) */)
                        continue;

                    l9 = IntPtr.L9WORD(StartFile, i + 0x12 + 10 * 2);
                    if (l9 < 0x8000 || l9 >= 0x8000 + GameState.LISTAREASIZE)
                        continue;

                    ScanContext scanCtx = new ScanContext();
                    scanCtx.Size = 0;
                    scanCtx.Min = i + d0;
                    scanCtx.Max = i + d0;
                    scanCtx.DriverV4 = false;
                    if (ValidateSequence(StartFile, Image, i + d0, i + d0, FileSize, false, scanCtx)) {
                        LOG.info("Found valid header at " + i + ", code size " + scanCtx.Size);
                        if (scanCtx.Size > MaxSize) {
                            Offset = i;
                            MaxSize = scanCtx.Size;
                            L9GameType = scanCtx.DriverV4 ? L9GameTypes.V4 : L9GameTypes.V3;
                        }
                    }
                }
            }
        }

        return Offset;
    }

    public final int ScanV1(IntPtr StartFile) {
        return -1;
    }

    public final int ScanV2(IntPtr StartFile) {

        int FileSize = StartFile.size();
        int[] Chk = new int[FileSize + 1];
        IntPtr Image = new StaticIntPtr("StartFile", new int[FileSize]);

        int i, MaxSize = 0, num;
        int j;
        int d0 = 0, l9;
        int Offset = -1;

        Chk[0] = 0;
        for (i = 1; i <= FileSize; i++) {
            Chk[i] = PrimitiveUtils.asByte(Chk[i - 1] + StartFile.get(i - 1));
        }

        for (i = 0; i < FileSize - 28; i++) {
            num = IntPtr.L9WORD(StartFile, i + 28) + 1;
            if (i + num <= FileSize && ((Chk[i + num] - Chk[i + 32]) & 0xff) == StartFile.get(i + 0x1e)) {
                for (j = 0; j < 14; j++) {
                    d0 = IntPtr.L9WORD(StartFile, i + j * 2);
                    if (j != 13 && d0 >= 0x8000 && d0 < 0x9000) {
                        if (d0 >= 0x8000 + GameState.LISTAREASIZE)
                            break;
                    } else if (i + d0 > FileSize)
                        break;
                }
                /* list9 ptr must be in listarea, acode ptr in data */
                if (j < 14 /* || (d0>=0x8000 && d0<0x9000) */)
                    continue;

                l9 = IntPtr.L9WORD(StartFile, i + 6 + 9 * 2);
                if (l9 < 0x8000 || l9 >= 0x8000 + GameState.LISTAREASIZE)
                    continue;

                ScanContext scanCtx = new ScanContext();
                scanCtx.Size = 0;
                scanCtx.Min = i + d0;
                scanCtx.Max = i + d0;
                scanCtx.DriverV4 = false;
                if (ValidateSequence(StartFile, Image, i + d0, i + d0, FileSize, false, scanCtx)) {
                    LOG.info("Found valid V2 header at " + i + ", code size " + scanCtx.Size);
                    if (scanCtx.Size > MaxSize && scanCtx.Size > 100) {
                        Offset = i;
                        MaxSize = scanCtx.Size;

                        // FIXME. added me pauli
                        break;
                    }
                }
            }
        }
        return Offset;
    }

    static final class ValidateSequenceContext {
        int Pos;
        int ScanCodeMask;
    }

    boolean ValidateSequence(IntPtr Base, IntPtr Image, int iPos, int acode, int FileSize, boolean Rts,
            ScanContext scanCtx) {
        ValidateSequenceContext vsCtx = new ValidateSequenceContext();

        boolean Finished = false;
        boolean Valid;
//        int Strange = 0;
        int Code;

        if (iPos >= FileSize) {
            return false;
        }
        vsCtx.Pos = iPos;
        if (vsCtx.Pos < scanCtx.Min) {
            scanCtx.Min = vsCtx.Pos;
        }

        if (Image.get(vsCtx.Pos) != 0) {
            return true; /* hit valid code */
        }

        do {
            Code = Base.get(vsCtx.Pos);
            Valid = true;
            if (Image.get(vsCtx.Pos) != 0) {
                break; /* converged to found code */
            }
            Image.put(vsCtx.Pos++, 2);
            if (vsCtx.Pos > scanCtx.Max) {
                scanCtx.Max = vsCtx.Pos;
            }

            vsCtx.ScanCodeMask = 0x9f;
            if ((Code & 0x80) != 0) {
                vsCtx.ScanCodeMask = 0xff;
                if ((Code & 0x1f) > 0xa)
                    Valid = false;
                vsCtx.Pos += 2;
            } else
                switch (Code & 0x1f) {
                case 0: /* goto */
                {
                    int Val = scangetaddr(Code, Base, acode, vsCtx);
                    Valid = ValidateSequence(Base, Image, Val, acode, FileSize, true, scanCtx);
                    Finished = true;
                    break;
                }
                case 1: /* intgosub */
                {
                    int Val = scangetaddr(Code, Base, acode, vsCtx);
                    Valid = ValidateSequence(Base, Image, Val, acode, FileSize, true, scanCtx);
                    break;
                }
                case 2: /* intreturn */
                    Valid = Rts;
                    Finished = true;
                    break;
                case 3: /* printnumber */
                    vsCtx.Pos++;
                    break;
                case 4: /* messagev */
                    vsCtx.Pos++;
                    break;
                case 5: /* messagec */
                    scangetcon(Code, vsCtx);
                    break;
                case 6: /* function */
                    switch (Base.get(vsCtx.Pos++)) {
                    case 2:/* random */
                        vsCtx.Pos++;
                        break;
                    case 1:/* calldriver */
                        if (scanCtx.DriverV4) {
                            if (CheckCallDriverV4(Base, vsCtx.Pos - 2))
                                scanCtx.DriverV4 = true;
                        }
                        break;
                    case 3:/* save */
                    case 4:/* restore */
                    case 5:/* clearworkspace */
                    case 6:/* clear stack */
                        break;
                    case 250: /* printstr */
                        while (Base.get(vsCtx.Pos++) != 0)
                            ;
                        break;

                    default:
                        Valid = false;
                        break;
                    }
                    break;
                case 7: /* input */
                    vsCtx.Pos += 4;
                    break;
                case 8: /* varcon */
                    scangetcon(Code, vsCtx);
                    vsCtx.Pos++;
                    break;
                case 9: /* varvar */
                    vsCtx.Pos += 2;
                    break;
                case 10: /* _add */
                    vsCtx.Pos += 2;
                    break;
                case 11: /* _sub */
                    vsCtx.Pos += 2;
                    break;
                case 14: /* jump */
                    scanCtx.JumpKill = true;
                    Finished = true;
                    break;
                case 15: /* exit */
                    vsCtx.Pos += 4;
                    break;
                case 16: /* ifeqvt */
                case 17: /* ifnevt */
                case 18: /* ifltvt */
                case 19: /* ifgtvt */
                {
                    int Val;
                    vsCtx.Pos += 2;
                    Val = scangetaddr(Code, Base, acode, vsCtx);
                    Valid = ValidateSequence(Base, Image, Val, acode, FileSize, Rts, scanCtx);
                    break;
                }
                case 20: /* screen */
                    if (Base.get(vsCtx.Pos++) != 0) {
                        vsCtx.Pos++;
                    }
                    break;
                case 21: /* cleartg */
                    vsCtx.Pos++;
                    break;
                case 22: /* picture */
                    vsCtx.Pos++;
                    break;
                case 23: /* getnextobject */
                    vsCtx.Pos += 6;
                    break;
                case 24: /* ifeqct */
                case 25: /* ifnect */
                case 26: /* ifltct */
                case 27: /* ifgtct */
                {
                    int Val;
                    vsCtx.Pos++;
                    scangetcon(Code, vsCtx);
                    Val = scangetaddr(Code, Base, acode, vsCtx);
                    Valid = ValidateSequence(Base, Image, Val, acode, FileSize, Rts, scanCtx);
                    break;
                }
                case 28: /* printinput */
                    break;
                case 12: /* ilins */
                case 13: /* ilins */
                case 29: /* ilins */
                case 30: /* ilins */
                case 31: /* ilins */
                    Valid = false;
                    break;
                }
//            if (Valid && ((Code & ~vsCtx.ScanCodeMask) != 0)) {
//                Strange++;
//            }
        } while (Valid && !Finished && vsCtx.Pos < FileSize); /* && Strange==0); */

        scanCtx.Size += vsCtx.Pos - iPos;
        return Valid; /* && Strange==0; */
    }

    void scangetcon(int Code, ValidateSequenceContext vsCtx) {
        vsCtx.Pos++;
        if ((Code & 64) == 0) {
            vsCtx.Pos++;
        }
        vsCtx.ScanCodeMask |= 0x40;
    }

    int scangetaddr(int Code, IntPtr Base, int acode, ValidateSequenceContext vsCtx) {
        vsCtx.ScanCodeMask |= 0x20;
        if ((Code & 0x20) != 0) {
            /* getaddrshort */
            int diff = PrimitiveUtils.asSignedByte(Base.get(vsCtx.Pos));
            vsCtx.Pos++;
            return vsCtx.Pos + diff - 1;
        } else {
            return acode + scanmovewa5d0(Base, vsCtx);
        }
    }

    int scanmovewa5d0(IntPtr Base, ValidateSequenceContext vsCtx) {
        int ret = IntPtr.L9WORD(Base, vsCtx.Pos);
        vsCtx.Pos += 2;
        return ret;
    }

    boolean CheckCallDriverV4(IntPtr Base, int Pos) {
        int i, j;

        /*
         * Look back for an assignment from a variable to list9[0], which is used to specify the driver call.
         */
        for (i = 0; i < 2; i++) {
            int x = Pos - ((i + 1) * 3);
            if ((Base.get(x) == 0x89) && (Base.get(x + 1) == 0x00)) {
                /* Get the variable being copied to list9[0] */
                int var = Base.get(x + 2);

                /* Look back for an assignment to the variable. */
                for (j = 0; j < 2; j++) {
                    int y = x - ((j + 1) * 3);
                    if ((Base.get(y) == 0x48) && (Base.get(y + 2) == var)) {
                        /* If this a V4 driver call? */
                        switch (Base.get(y + 1)) {
                        case 0x0E:
                        case 0x20:
                        case 0x22:
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

}
