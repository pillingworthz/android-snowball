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
 * Wraps a double and allows it to be passed by ref to functions.
 * 
 * @author pauli
 */
public final class MutableDouble implements Serializable {

    private static final long serialVersionUID = 1L;

    private double value;

    public MutableDouble() {

    }

    public MutableDouble(int value) {

        this.value = value;
    }

    public final double get() {

        return value;
    }

    public final void set(double value) {

        this.value = value;
    }

    public final void incr() {

        value++;
    }

    @Override
    public final String toString() {

        return Double.toString(value);
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        MutableDouble other = (MutableDouble) obj;
        if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
            return false;
        return true;
    }

}