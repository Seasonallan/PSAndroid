package com.season.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.season.playball.sin.BallView;

public class BallSinActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    private RelativeLayout mContainerView;
    private BallView mBallView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        mContainerView = new RelativeLayout(this);
        mContainerView.setOnClickListener(this);
        mContainerView.setOnLongClickListener(this);

        TextView textView = new TextView(this);
        textView.setText("Touch the Screen");
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.addRule(RelativeLayout.CENTER_IN_PARENT);
        mContainerView.addView(textView, param);

        mBallView = new BallView(this);
        mContainerView.addView(mBallView);

        setContentView(mContainerView);
    }

    @Override
    public void onClick(View v) {
        mBallView.add1RandomBall();
    }

    @Override
    public boolean onLongClick(View v) {
        mBallView.clear();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBallView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBallView.start();
    }

}
