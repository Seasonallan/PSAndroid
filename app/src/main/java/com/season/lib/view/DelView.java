package com.season.lib.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.season.myapplication.R;


/**
 * Created by Administrator on 2017/10/31.
 */

public class DelView {
    ImageView del_iv;
    TextView del_tv;
    LinearLayout del_container;

    View view1, view2, view3;

    public DelView(LinearLayout linearLayout, ImageView imageView, TextView textView, View backView, View shareView, View nextView) {
        this.del_container = linearLayout;
        this.del_iv = imageView;
        this.del_tv = textView;
        this.view1 = backView;
        this.view2 = shareView;
        this.view3 = nextView;
    }


    boolean isAnimating = false;

    public boolean checkPosition(MotionEvent event) {
        float currentX = event.getX();
        float currentY = event.getY();

        boolean res = false;
        if (currentY <= del_container.getHeight() * 4 / 5) {
            del_tv.setTextColor(0xffff5d7c);
            del_iv.setImageResource(R.mipmap.crop_shangchu2);
            res = true;
        } else {
            del_tv.setTextColor(0xff7b7b7b);
            del_iv.setImageResource(R.mipmap.crop_shanchu);
            res = false;
        }
        if (del_container.getVisibility() == View.VISIBLE || isAnimating) {
            return res;
        }
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(del_container, "translationY", -del_container.getHeight(), 0);
        fadeAnim.setInterpolator(new DecelerateInterpolator());
        fadeAnim.setDuration(300);
        fadeAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                del_container.setVisibility(View.VISIBLE);
                isAnimating = true;
                if (view1!=null)
                view1.setVisibility(View.INVISIBLE);
                if (view2!=null)
                view2.setVisibility(View.INVISIBLE);
                if (view3!=null)
                view3.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        fadeAnim.start();
        return res;
    }

    void show(View view) {
        if (view==null){
            return;
        }
        view.setVisibility(View.VISIBLE);
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        fadeAnim.setInterpolator(new DecelerateInterpolator());
        fadeAnim.setDuration(300);
        fadeAnim.start();
    }

    public void hide() {
        if (del_container.getVisibility() == View.INVISIBLE) {
            return;
        }
        show(view1);
        show(view2);
        show(view3);
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(del_container, "translationY", 0, -del_container.getHeight());
        fadeAnim.setInterpolator(new DecelerateInterpolator());
        fadeAnim.setDuration(300);
        fadeAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                if (del_container != null) del_container.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        fadeAnim.start();
    }
}
