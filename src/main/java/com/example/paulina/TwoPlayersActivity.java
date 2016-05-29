/*
* Copyright 2013 The Android Open Source Project
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


package com.example.paulina;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class TwoPlayersActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent incomingIntent = getIntent();
        Bundle e = incomingIntent.getExtras();
        Integer ROLE;

        if (e != null) {
            ROLE = (e.getString("role", "teacher").equals("teacher")) ? 1 : 2;
        } else {
            ROLE = 1;
        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            Bundle role = new Bundle();
            role.putInt("role", ROLE);
            fragment.setArguments(role);
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/ChickenButt.ttf");
        TextView roleView = (TextView) findViewById(R.id.role_text);
        TextView descView = (TextView) findViewById(R.id.role_description);
        roleView.setTypeface(font);
        descView.setTypeface(font);

        if (ROLE == 1) {
            roleView.setText(R.string.teacher);
            descView.setText(R.string.teacher_instr);
        } else {
            roleView.setText(R.string.student);
            descView.setText(R.string.student_instr);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
