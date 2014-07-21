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
package uk.co.threeonefour.android.snowball.gamestate;

import java.util.Arrays;

import uk.co.threeonefour.android.snowball.level9j.vm.GameState;
import android.os.Parcel;
import android.os.Parcelable;

public class PersistableGameState extends GameState implements Parcelable {

    private static final long serialVersionUID = 1L;

    public static final Parcelable.Creator<PersistableGameState> CREATOR = new Parcelable.Creator<PersistableGameState>() {
        public PersistableGameState createFromParcel(Parcel in) {
            return new PersistableGameState(in);
        }

        public PersistableGameState[] newArray(int size) {
            return new PersistableGameState[size];
        }
    };

    PersistableGameState() {
    }

    public PersistableGameState(GameState orig) {
        this.codePtr = orig.getCodePtr();
        this.varTable = Arrays.copyOf(orig.getVarTable(), orig.getVarTable().length);
        this.listArea = Arrays.copyOf(orig.getListArea(), orig.getListArea().length);
        this.stackPtr = orig.getStackPtr();
        this.stack = Arrays.copyOf(orig.getStack(), orig.getStack().length);
    }

    private PersistableGameState(Parcel in) {
        codePtr = in.readInt();
        in.readIntArray(varTable);
        in.readIntArray(listArea);
        stackPtr = in.readInt();
        in.readIntArray(stack);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(codePtr);
        dest.writeIntArray(varTable);
        dest.writeIntArray(listArea);
        dest.writeInt(stackPtr);
        dest.writeIntArray(stack);
    }
}
