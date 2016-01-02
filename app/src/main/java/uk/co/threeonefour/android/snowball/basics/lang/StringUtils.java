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

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    /**
     * An empty String.
     */
    public static final String EMPTY = "";

    /**
     * An empty String array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Checks if a String is empty ("") or null.
     * 
     * @param str
     *            the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {

        return str == null || str.length() == 0;
    }

    /**
     * Checks if a String is not empty ("") and not null.
     * 
     * @param str
     *            the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty(String str) {

        return str != null && str.length() > 0;
    }

    /**
     * <p>
     * Removes control characters (char &lt;= 32) from both ends of this String, handling <code>null</code> by returning
     * <code>null</code>.
     * </p>
     * 
     * @param str
     *            the String to be trimmed, may be null
     * @return the trimmed string, <code>null</code> if null String input
     */
    public static String trim(String str) {

        return str == null ? null : str.trim();
    }

    /**
     * <p>
     * Removes control characters (char &lt;= 32) from both ends of this String returning <code>null</code> if the
     * String is empty ("") after the trim or if it is <code>null</code>.
     * 
     * <p>
     * The String is trimmed using {@link String#trim()}. Trim removes start and end characters &lt;= 32.
     * </p>
     * 
     * @param str
     *            the String to be trimmed, may be null
     * @return the trimmed String, <code>null</code> if only chars &lt;= 32, empty or null String input
     */
    public static String trimToNull(String str) {

        String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }

    /**
     * <p>
     * Removes control characters (char &lt;= 32) from both ends of this String returning an empty String ("") if the
     * String is empty ("") after the trim or if it is <code>null</code>.
     * 
     * <p>
     * The String is trimmed using {@link String#trim()}. Trim removes start and end characters &lt;= 32.
     * </p>
     * 
     * @param str
     *            the String to be trimmed, may be null
     * @return the trimmed String, or an empty String if <code>null</code> input
     */
    public static String trimToEmpty(String str) {

        return str == null ? EMPTY : str.trim();
    }

    /**
     * Null safe equals implementation
     * 
     * @param str1
     * @param str2
     * @return true if object1 == object2 or object1.equals(object2)
     */
    public static boolean equals(String str1, String str2) {

        if (str1 == str2) {
            return true;
        }
        if ((str1 == null) || (str2 == null)) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * Null safe equals implementation that compares strings after trumming. This means a null string is equals to an
     * empty string
     * 
     * @param str1
     * @param str2
     * @return true if the trimmed strings are equal
     */
    public static final boolean trimEqual(String str1, String str2) {

        return equals(trimToNull(str1), trimToNull(str2));
    }

    /**
     * If str is null returns an empty string, otherwise returns the string passed in.
     * 
     * @param str
     * @return empty string or string passed in, never null
     */
    public static String defaultString(String str) {

        return (str == null) ? EMPTY : str;
    }

    /**
     * If str is null returns the given string, otherwise returns the string passed in.
     * 
     * @param str
     * @return given string or string passed in
     */
    public static String defaultString(String str, String defaultStr) {

        return (str == null) ? defaultStr : str;
    }

    /**
     * Removes all whitespace from a string
     * 
     * @return A copy of this string with all white space removed.
     */
    public static final String removeWhitespace(String str) {

        if (str == null || str.length() == 0) {
            return str;
        }
        char[] chars = new char[str.length()];
        int i, j = 0;
        for (i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch > ' ') {
                chars[j] = ch;
                j++;
            }
        }
        if (i != j) {
            char[] tmp = new char[j];
            System.arraycopy(chars, 0, tmp, 0, j);
            chars = tmp;
        }
        return new String(chars);
    }

    /**
     * Checks if the String contains only certain characters.
     */
    public static boolean containsOnly(String str, char[] valid) {

        if ((valid == null) || (str == null)) {
            return false;
        }
        if (str.length() == 0) {
            return true;
        }
        if (valid.length == 0) {
            return false;
        }
        return indexOfAnyBut(str, valid) == -1;
    }

    /**
     * Search a String to find the first index of any character not in the given set of characters.
     */
    public static int indexOfAnyBut(String str, char[] searchChars) {

        if (isEmpty(str) || searchChars == null || searchChars.length == 0) {
            return -1;
        }
        outer: for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            for (int j = 0; j < searchChars.length; j++) {
                if (searchChars[j] == ch) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public static String[] split(String str, char separatorChar) {

        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> list = new ArrayList<String>();
        int i = 0, start = 0;
        boolean match = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

    public static String join(Object[] array, String separator) {

        StringBuilder b = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            b.append(array[i]);
            if (i < array.length - 1) {
                b.append(separator);
            }
        }

        return b.toString();
    }

    public static String capitalize(String str) {

        String capStr = "";

        String[] strItems = str.split(" ");

        for (String item : strItems) {
            String firstLetter = item.substring(0, 1);
            String remainder = item.substring(1);
            capStr = capStr + firstLetter.toUpperCase() + remainder.toLowerCase() + " ";
        }

        return capStr.trim();
    }

}