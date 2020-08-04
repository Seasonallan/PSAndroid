package com.season.nouse.test.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.season.myapplication.R;


public class CalendarSelActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_sel);

        findViewById(R.id.text1).setOnClickListener(this);
        findViewById(R.id.text2).setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text1:
                startActivity(new Intent(CalendarSelActivity.this, CalendarMiActivity.class));
                break;
            case R.id.text2:
                startActivity(new Intent(CalendarSelActivity.this, CalendarDingActivity.class));
                break;
        }
    }
}
