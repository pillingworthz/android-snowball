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
package uk.co.threeonefour.android.snowball.activities.intro;

import uk.co.threeonefour.android.snowball.R;
import uk.co.threeonefour.android.snowball.activities.about.AboutActivity;
import uk.co.threeonefour.android.snowball.activities.game.GameActivity;
import uk.co.threeonefour.android.snowball.activities.loadgame.LoadGameActivity;
import uk.co.threeonefour.android.snowball.activities.manual.ManualActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        if (savedInstanceState == null) {
        }

        View container = (View) findViewById(R.id.intro_root_layout);
        container.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                loadGame();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.intro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case R.id.action_intro_load:
            loadGame();
            return true;
        case R.id.action_intro_about:
            about();
            return true;
        case R.id.action_intro_manual:
            manual();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadGame() {
        Intent intent = new Intent(this, LoadGameActivity.class);
        startActivity(intent);
    }

    public void about() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void manual() {
        Intent intent = new Intent(this, ManualActivity.class);
        startActivity(intent);
    }

    public void playGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}
