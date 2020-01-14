package com.season.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.season.myapplication.R;
import com.season.playball.WaterView;
import com.season.playball.sin.Ball;
import com.season.playball.sin.BallView;
import com.season.playball.sin.interpolator.BallInterpolatorFactory;
import com.season.playball.sin.interpolator.IInterpolator;

public class FunctionActivity extends Activity{

    private BallView mBallView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_function);

        mBallView = findViewById(R.id.top_view);

        mBallView.post(new Runnable() {
            @Override
            public void run() {
                Ball ballModel = new Ball.Builder()
                        .setId(System.currentTimeMillis())
                        .setEdge(mBallView.getWidth(), mBallView.getHeight())
                        .setInterpolator(BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.KEEP))
                        .setSpecial(20)
                        .build();
                ballModel.setParams(88, "PS");
                mBallView.add1Ball(ballModel);
            }
        });
        mBallView.setOnBallSeparateListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FunctionActivity.this, PsActivity.class));
            }
        });
        final WaterView waterView = findViewById(R.id.bottom_view);
        waterView.setOnBallAddListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ball ballModel = new Ball.Builder()
                        .setId(System.currentTimeMillis())
                        .setEdge(mBallView.getWidth(), mBallView.getHeight())
                        .setInterpolator(BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.KEEP))
                        .setSpecial(-20)
                        .build();
                ballModel.setParams(50, "T");
                ballModel.setSpeed(waterView.getSpeed());
                ballModel.setColor(waterView.getColor());
                ballModel.setPosition(mBallView.getWidth()/2, mBallView.getHeight());
                ballModel.setDegree(waterView.getDegree());
                mBallView.add1Ball(ballModel);
            }
        });
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
