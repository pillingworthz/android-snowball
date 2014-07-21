package uk.co.threeonefour.android.snowball.activities.game;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class RamSave implements Serializable, Parcelable {

    private static final long serialVersionUID = 1L;

    public static final Parcelable.Creator<RamSave> CREATOR = new Parcelable.Creator<RamSave>() {
        public RamSave createFromParcel(Parcel in) {
            return new RamSave(in);
        }

        public RamSave[] newArray(int size) {
            return new RamSave[size];
        }
    };

    private final int[] varTable;

    public RamSave(int[] varTable) {
        this.varTable = new int[varTable.length];
        System.arraycopy(varTable, 0, this.varTable, 0, varTable.length);
    }

    private RamSave(Parcel in) {
        int length = in.readInt();
        this.varTable = new int[length];
        in.readIntArray(varTable);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(varTable.length);
        dest.writeIntArray(varTable);
    }

    public int[] getVarTable() {

        int[] copy = new int[varTable.length];
        System.arraycopy(varTable, 0, copy, 0, varTable.length);
        return copy;
    }
}