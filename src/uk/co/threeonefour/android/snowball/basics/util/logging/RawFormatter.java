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
package uk.co.threeonefour.android.snowball.basics.util.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class RawFormatter extends Formatter {
    int count;

    @Override
    public synchronized String format(LogRecord record) {

        count++;
        return count + ":" + formatMessage(record) + "\n";
    }
}