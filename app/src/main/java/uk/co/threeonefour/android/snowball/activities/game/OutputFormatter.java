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
package uk.co.threeonefour.android.snowball.activities.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;

@SuppressLint("DefaultLocale")
public class OutputFormatter {

    private static final String WHAT_NOW = "What now?";

    private static final Set<String> directions;

    static {
        directions = new HashSet<String>();
        directions.addAll(Arrays.asList(new String[] { "north", "south", "east", "west", "up", "down", "out",
                "northeast", "northwest", "southeast", "southwest" }));
    }

    public interface WordClickedHandler {
        void wordClicked(String word);
    }

    private WordClickedHandler wordClickedHandler;
    private Integer textColor;
    private boolean highlightDirections;
    private boolean clickableText;

    public OutputFormatter() {
    }

    public void setWordClickedHandler(WordClickedHandler handler) {
        this.wordClickedHandler = handler;
    }

    public boolean isClickableText() {
        return clickableText;
    }

    public void setClickableText(boolean clickableText) {
        this.clickableText = clickableText;
    }

    public boolean isHighlightDirections() {
        return highlightDirections;
    }

    public void setHighlightDirections(boolean highlightDirections) {
        this.highlightDirections = highlightDirections;
    }

    public Integer getTextColor() {
        return textColor;
    }

    public void setTextColor(Integer textColor) {
        this.textColor = textColor;
    }

    public List<SpannableString> format(String str) {

        List<SpannableString> styledLines = new ArrayList<SpannableString>();

        String text = str.trim();

        /* split text on CR and or LF */
        String lines[] = text.split("\\r|\\n");
        for (String line : lines) {

            /* remove any offending whitespace from either end */
            line = line.trim();

            /* remove the "What now?" as this is just clutter */
            if (line.endsWith(WHAT_NOW)) {
                line = line.substring(0, line.length() - WHAT_NOW.length());
            }

            /* again, tidy up the whitespace */
            line = line.trim();

            /*
             * find the start of the last sentence by looking for the last full stop - this is not foolproof and will be
             * caught out by the use of punctuation
             */
            int lastFullStop = -1;
            if (line.length() > 1) {
                if (line.charAt(line.length() - 1) == '.') {
                    lastFullStop = line.lastIndexOf('.', line.length() - 2);
                } else {
                    lastFullStop = line.lastIndexOf('.', line.length() - 1);
                }
            }

            /*
             * split the line into words and directions those that we recognise as directions and are in the last
             * sentence
             */
            if (line.length() > 0) {
                SpannableString styledLine = new SpannableString(line);
                Pattern p = Pattern.compile(" (\\b[^\\s]+\\b)");
                Matcher m = p.matcher(line);
                while (m.find()) {
                    if (m.groupCount() > 0) {
                        String word = m.group(1).toLowerCase();
                        int start = m.start(1);
                        int end = start + word.length();

                        boolean wordClickable = false;
                        if (highlightDirections) {
                            if (directions.contains(word)) {
                                if (start > lastFullStop) {
                                    styledLine.setSpan(new StyleSpan(Typeface.BOLD), start, end,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    wordClickable = true;
                                }
                            }
                        }

                        if (clickableText || wordClickable) {
                            styledLine.setSpan(new ClickableWordSpan(word), start, end,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }

                styledLines.add(styledLine);
            }
        }

        return styledLines;
    }

    public final class ClickableWordSpan extends ClickableSpan {

        private final String word;

        public ClickableWordSpan(String word) {
            this.word = word;
        }

        @Override
        public void onClick(View view) {
            Log.i("outputformatter", "clicked on " + word);
            if (wordClickedHandler != null) {
                wordClickedHandler.wordClicked(word);
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            if (textColor != null) {
                ds.setColor(textColor);
            }
            ds.setUnderlineText(false);
        }

    }

    public boolean isDirection(String command) {
        return directions.contains(command.toLowerCase());
    }
}
