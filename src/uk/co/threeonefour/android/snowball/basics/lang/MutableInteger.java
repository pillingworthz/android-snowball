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

import java.io.Serializable;

/**
 * Wraps an integer and allows it to be passed by ref to functions.
 * 
 * @author pauli
 */
public final class MutableInteger implements Serializable {

    private static final long serialVersionUID = 1L;

    private int value;

    public MutableInteger() {

    }

    public MutableInteger(int value) {

        this.value = value;
    }

    public final int get() {

        return value;
    }

    public final void set(int value) {

        this.value = value;
    }

    public final void incr() {

        value++;
    }

    public final void incr(int work) {

        value += work;
    }

    @Override
    public final String toString() {

        return Integer.toString(value);
    }

    @Override
    public final int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    @Override
    public final boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MutableInteger other = (MutableInteger) obj;
        if (value != other.value)
            return false;
        return true;
    }
}