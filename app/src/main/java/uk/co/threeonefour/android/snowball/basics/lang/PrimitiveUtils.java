/**
 * Copyright 2013 Paul Illingworth
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
package uk.co.threeonefour.android.snowball.basics.lang;

public final class PrimitiveUtils {

    public static final int asByte(int i) {

        return i & 0xFF;
    }

    public static final int asUnsignedShort(int i) {

        return i & 0xFFFF;
    }

    public static final int fromByte(byte i) {

        int r = (i < 0) ? i + 256 : i;
        return r & 0xFF;
    }

    public static final boolean asBoolean(int i) {

        return i != 0;
    }

    public static final int asSignedByte(int i) {

        return (i > 127) ? i - 256 : i;
    }
}
