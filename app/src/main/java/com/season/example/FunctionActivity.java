package com.season.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.season.lib.RoutePath;
import com.season.myapplication.R;
import com.season.playball.Ball;
import com.season.playball.BallView;
import com.season.playball.interpolator.BallInterpolatorFactory;

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
                        .setEdge(mBallView.getWidth(), mBallView.getTopHeight())
                        .setInterpolator(BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.KEEP))
                        .setSpecial(1)
                        .build();
                ballModel.setParams(88, "PS");
                mBallView.add1Ball(ballModel);
                Ball ballModel2 = new Ball.Builder()
                        .setId(System.currentTimeMillis())
                        .setEdge(mBallView.getWidth(), mBallView.getTopHeight())
                        .setInterpolator(BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.KEEP))
                        .setSpecial(2)
                        .build();
                ballModel2.setParams(88, "BOOK");
                mBallView.add1Ball(ballModel2);
                mBallView.setProgress(0,0 , false);
            }
        });
        mBallView.setOnBallSeparateListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int) v.getTag();
                if (tag == 1){
                    ARouter.getInstance().build(RoutePath.PS).navigation();
                }else if (tag ==2 ){
                    ARouter.getInstance().build(RoutePath.BOOK).navigation();
                }
            }
        });
        mBallView.setOnBallAddListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ball ballModel = new Ball.Builder()
                        .setId(System.currentTimeMillis())
                        .setEdge(mBallView.getWidth(), mBallView.getTopHeight())
                        .setInterpolator(BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.KEEP))
                        .setSpecial(-20)
                        .build();
                ballModel.setParams(mBallView.getRadius(), "T");
                ballModel.setSpeed(mBallView.getSpeed());
                ballModel.setColor(mBallView.getColor());
                ballModel.setPosition(mBallView.getWidth()/2, mBallView.getTopHeight());
                ballModel.setDegree(mBallView.getDegree());
                mBallView.add1Ball(ballModel);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBallView.destroy();
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
