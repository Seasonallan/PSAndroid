package com.season.example;

import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.season.book.R;
import com.season.book.bean.BookInfo;
import com.season.example.dragview.DragAdapter;
import com.season.example.dragview.DragController;
import com.season.example.dragview.DragGridView;
import com.season.example.dragview.DragScrollView;
import com.season.example.transfer.TransferController;
import com.season.lib.BaseStartPagerActivity;
import com.season.lib.RoutePath;
import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.bitmap.ImageMemoryCache;
import com.season.lib.dimen.ScreenUtils;
import com.season.lib.view.LoadingView;

@Route(path = RoutePath.BOOK)
public class BookShelfActivity extends BaseStartPagerActivity implements DragScrollView.ICallback<BookInfo> {
    private int NUM_COLUMNS = 3;
    private int NUM_LINES = 3;
    private DragScrollView mContainer;
    private TextView mPageView;
    private LoadingView mLoadingView;
    private TransferController transferController;
    private ImageView animationView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_shelf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //DragController.getInstance().disableDelFunction();
        transferController = new TransferController(this) {
            @Override
            protected void addFile(String filePath) {
                DragController.getInstance().cancelDragMode();
                mContainer.noItemAdd(BookShelfPreLoader.getInstance().decodeFile(filePath), BookShelfActivity.this);
            }
        };
        animationView = findViewById(R.id.ani);
        mPageView = findViewById(R.id.page);
        mContainer = findViewById(R.id.views);
        mLoadingView = findViewById(R.id.loadView);
        mContainer.setPageListener(new DragScrollView.PageListener() {
            @Override
            public void page(int page) {
                mPageView.setText("书架页码： " + (page + 1));
            }
        });
        findViewById(R.id.btn_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferController.switchStatus();
            }
        });

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mContainer.getLayoutParams();
        param.height = ScreenUtils.getScreenWidth() / 3 * 3 / 2 * 3;
        mContainer.requestLayout();

        BookShelfPreLoader.getInstance().getBookLists(new BookShelfPreLoader.ICallback() {
            @Override
            public void onBookLoaded(final List<BookInfo> bookLists) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingView.setVisibility(View.GONE);
                        mContainer.setAdapter(bookLists, BookShelfActivity.this);
                    }
                }, 600);
            }
        });
    }


    @Override
    public int getColumnNumber() {
        return NUM_COLUMNS;
    }

    @Override
    public DragAdapter<BookInfo> getAdapter(List<BookInfo> data) {
        return new BookShelfAdapter(BookShelfActivity.this, data);
    }

    private int dragOffsetX, dragOffsetY;
    private float width, height;
    private Bitmap bitmap;
    private int duration = 400;

    @Override
    protected void onResume() {
        super.onResume();
        width = ScreenUtils.getScreenWidth();
        height = ScreenUtils.getScreenHeight();
        if (!BitmapUtil.isBitmapAvaliable(bitmap)) {
            return;
        }
        final AnimatorSet animatorSet = new AnimatorSet();
        animationView.setPivotX(0);
        animationView.setPivotY(0);
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(animationView, "translationX", 0, dragOffsetX)
                        .setDuration(duration),
                ObjectAnimator.ofFloat(animationView, "translationY", 0, dragOffsetY)
                        .setDuration(duration),
                ObjectAnimator.ofFloat(animationView, "scaleX", width / bitmap.getWidth(), 1)
                        .setDuration(duration),
                ObjectAnimator.ofFloat(animationView, "scaleY", height / bitmap.getHeight(), 1)
                        .setDuration(duration)
        );
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animationView.setVisibility(View.GONE);
                animationView.setImageBitmap(null);
                BitmapUtil.recycleBitmaps(bitmap);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    @Override
    public DragGridView<BookInfo> getItemView() {
        final DragGridView<BookInfo> view = (DragGridView<BookInfo>) LayoutInflater.from(getApplicationContext()).inflate(R.layout.grid, null);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View itemView, final int position, long id) {

                int[] location = new int[2];
                itemView.getLocationOnScreen(location);
                dragOffsetY = location[1];
                dragOffsetX = location[0];
                bitmap = Bitmap.createBitmap(itemView.getWidth(), itemView.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                itemView.draw(canvas);

                animationView.setImageBitmap(bitmap);
                RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
                param.width = bitmap.getWidth();
                param.height = bitmap.getHeight();
                animationView.requestLayout();
                final AnimatorSet animatorSet = new AnimatorSet();
                animationView.setPivotX(0);
                animationView.setPivotY(0);
                animatorSet.playTogether(
                        ObjectAnimator.ofFloat(animationView, "translationX", dragOffsetX, 0)
                                .setDuration(duration),
                        ObjectAnimator.ofFloat(animationView, "translationY", dragOffsetY, 0)
                                .setDuration(duration),
                        ObjectAnimator.ofFloat(animationView, "scaleX", 1, width / bitmap.getWidth())
                                .setDuration(duration),
                        ObjectAnimator.ofFloat(animationView, "scaleY", 1, height / bitmap.getHeight())
                                .setDuration(duration)
                );
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        animationView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ImageMemoryCache.getInstance().put(bitmap);
                        BookInfo item = (BookInfo) view.getGridAdapter().getItem(position);
                        BaseBookActivity.open(BookShelfActivity.this, item);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animatorSet.start();
            }
        });
        return view;
    }

    @Override
    public int getLineNumber() {
        return NUM_LINES;
    }

    @Override
    public void onBackPressed() {
        if (transferController.onBackPressed()) {
            return;
        }
        if (DragController.getInstance().cancelDragMode()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BookShelfPreLoader.getInstance().saveLocal(mContainer.getFinalDatas());
        DragController.getInstance().clear();
        ImageMemoryCache.getInstance().clear();
    }


}



