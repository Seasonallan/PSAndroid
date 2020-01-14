package com.season.example;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.season.playball.mul.BallView;

import java.util.ArrayList;
import java.util.List;

public class BallMulActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private RelativeLayout mContaintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        mContaintView = new RelativeLayout(this);
        mContaintView.setOnClickListener(this);
        mContaintView.setOnLongClickListener(this);

        setContentView(mContaintView);
    }

    @Override
    public void onClick(View v) {
        addBall();
    }

    private void addBall() {
        BallView ballView = new BallView(this, ballViews.size(), mContaintView){
            public List<BallView> getRunningBalls(){
                return ballViews;
            };
        };
        ballView.start();

        ballViews.add(ballView);
        mContaintView.addView(ballView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (BallView ballView : ballViews) {
            ballView.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (BallView ballView : ballViews) {
            ballView.start();
        }
    }

    private List<BallView> ballViews = new ArrayList<>();
    @Override
    public boolean onLongClick(View v) {
        mContaintView.removeAllViews();
        for (BallView ballView : ballViews) {
            ballView.stop();
        }
        ballViews.clear();
        return true;
    }
}
