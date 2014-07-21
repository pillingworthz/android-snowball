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
package uk.co.threeonefour.android.snowball.basics.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * This utility class contains input/output functions.
 */
public class IOUtils {

    private static final int IO_BUFFER_SIZE = 4096;

    private IOUtils() {

        // utility class
    }

    /**
     * Copy all data from the input stream to the output stream. Both streams are kept open.
     * 
     * @param in
     *            the input stream
     * @param out
     *            the output stream (null if writing is not required)
     * @return the number of bytes copied
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {

        return copy(in, out, Long.MAX_VALUE);
    }

    /**
     * Copy all data from the input stream to the output stream. Both streams are kept open.
     * 
     * @param in
     *            the input stream
     * @param out
     *            the output stream (null if writing is not required)
     * @param length
     *            the maximum number of bytes to copy
     * @return the number of bytes copied
     */
    public static long copy(InputStream in, OutputStream out, long length) throws IOException {

        long copied = 0;
        int len = (int) Math.min(length, IO_BUFFER_SIZE);
        byte[] buffer = new byte[len];
        while (length > 0) {
            len = in.read(buffer, 0, len);
            if (len < 0) {
                break;
            }
            if (out != null) {
                out.write(buffer, 0, len);
            }
            copied += len;
            length -= len;
            len = (int) Math.min(length, IO_BUFFER_SIZE);
        }
        return copied;
    }

    /**
     * Close an input stream without throwing an exception.
     * 
     * @param in
     *            the input stream or null
     */
    public static void closeQuietly(InputStream in) {

        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Close a reader without throwing an exception.
     * 
     * @param reader
     *            the reader or null
     */
    public static void closeQuietly(Reader reader) {

        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Close an output stream without throwing an exception.
     * 
     * @param out
     *            the output stream or null
     */
    public static void closeQuietly(OutputStream out) {

        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Close a writer without throwing an exception.
     * 
     * @param writer
     *            the writer or null
     */
    public static void closeQuietly(Writer writer) {

        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Copy all data from the input stream to the output stream. Both streams are kept open.
     * 
     * @param in
     *            the reader
     * @param out
     *            the writer (null if writing is not required)
     * @return the number of bytes copied
     */
    public static long copy(Reader in, Writer out) throws IOException {

        return copy(in, out, Long.MAX_VALUE);
    }
    
    /**
     * Copy all data from the reader to the writer. Both are kept open.
     * 
     * @param in
     *            the reader
     * @param out
     *            the writer (null if writing is not required)
     * @param length
     *            the maximum number of bytes to copy
     * @return the number of bytes copied
     */
    public static long copy(Reader in, Writer out, long length) throws IOException {

        long copied = 0;
        int len = (int) Math.min(length, IO_BUFFER_SIZE);
        char[] buffer = new char[len];
        while (length > 0) {
            len = in.read(buffer, 0, len);
            if (len < 0) {
                break;
            }
            if (out != null) {
                out.write(buffer, 0, len);
            }
            copied += len;
            length -= len;
            len = (int) Math.min(length, IO_BUFFER_SIZE);
        }
        return copied;
    }

    
}
