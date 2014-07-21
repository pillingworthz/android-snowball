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

import uk.co.threeonefour.android.snowball.R;
import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TextInputView extends LinearLayout {

    private enum ExpandCollapse {
        EXPAND, COLLAPSE;
    }

    public interface TextInputHandler {
        void processCommand(String command);
    }

    private final EditText textInput;
    private TextInputHandler textInputHandler;

    private ExpandCollapse expandCollapse = ExpandCollapse.EXPAND;

    public TextInputView(Context context) {
        this(context, null);
    }

    public TextInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_textinput, this, true);

        textInput = (EditText) findViewById(R.id.gameTextInput);
        textInput.setOnEditorActionListener(new OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    processCommandCallback();
                    return true;
                } else {
                    return false;
                }
            }
        });

        ImageButton lookButton = (ImageButton) findViewById(R.id.button_look);
        lookButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickVerb("look", false, true);
            }
        });
        ImageButton getButton = (ImageButton) findViewById(R.id.button_get);
        getButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickVerb("get", true, false);
            }
        });
        ImageButton dropButton = (ImageButton) findViewById(R.id.button_drop);
        dropButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickVerb("drop", true, false);
            }
        });
        ImageButton examineButton = (ImageButton) findViewById(R.id.button_examine);
        examineButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickVerb("examine", true, false);
            }
        });
        ImageButton openButton = (ImageButton) findViewById(R.id.button_open);
        openButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickVerb("open", true, false);
            }
        });
        ImageButton closeButton = (ImageButton) findViewById(R.id.button_close);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickVerb("close", true, false);
            }
        });
        ImageButton invButton = (ImageButton) findViewById(R.id.button_inv);
        invButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickVerb("inventory", false, true);
            }
        });
        ImageButton pressButton = (ImageButton) findViewById(R.id.button_press);
        pressButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickVerb("press", true, false);
            }
        });
        ImageButton readButton = (ImageButton) findViewById(R.id.button_read);
        readButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickVerb("read", true, false);
            }
        });
        ImageButton waitButton = (ImageButton) findViewById(R.id.button_wait);
        waitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                quickVerb("wait", false, true);
            }
        });
        ImageButton clearTextButton = (ImageButton) findViewById(R.id.button_clear);
        clearTextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                textInput.setText("");
            }
        });
        ImageButton enterButton = (ImageButton) findViewById(R.id.button_enter);
        enterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                processCommandCallback();
            }
        });
        ImageButton expandCollapseButton = (ImageButton) findViewById(R.id.button_expandCollapseVerbBar);
        expandCollapseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expandCollapseVerbBar();
            }
        });

        /* sync button state with variable */
        setCollapseButtonImage();
    }

    public void setTextInputHandler(TextInputHandler textInputHandler) {
        this.textInputHandler = textInputHandler;
    }

    private void quickVerb(String verb, boolean appendable, boolean execute) {
        if (appendable) {
            textInput.setText(verb + " ");
        } else {
            textInput.setText(verb);
        }
        textInput.setSelection(textInput.getText().length());
        if (execute) {
            processCommandCallback();
        }
    }

    private void expandCollapseVerbBar() {
        // toggle
        if (expandCollapse == ExpandCollapse.EXPAND) {
            expandCollapse = ExpandCollapse.COLLAPSE;
        } else {
            expandCollapse = ExpandCollapse.EXPAND;
        }
        setCollapseButtonImage();
    }

    public void setCollapseButtonImage() {
        ImageButton collapse = (ImageButton) findViewById(R.id.button_expandCollapseVerbBar);
        View verbBar = findViewById(R.id.verbBar);
        if (expandCollapse == ExpandCollapse.EXPAND) {
            collapse.setImageResource(R.drawable.custom_ic_collapse);
            verbBar.setVisibility(View.GONE);
        } else {
            collapse.setImageResource(R.drawable.custom_ic_expand);
            verbBar.setVisibility(View.VISIBLE);
        }
    }

    private void processCommandCallback() {
        if (textInputHandler != null) {
            Editable editable = textInput.getText();
            if (editable.length() > 0) {
                textInputHandler.processCommand(editable.toString());
                editable.clear();
            }
        }
    }

    public String getCommand() {
        return textInput.getText().toString();
    }

    public void setCommand(String text) {
        textInput.setText(text);
        textInput.setSelection(textInput.getText().length());
    }

}