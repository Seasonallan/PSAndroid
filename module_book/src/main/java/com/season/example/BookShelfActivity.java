package com.season.example;

import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
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
import com.season.example.dragview.IDragListener;
import com.season.example.transfer.TransferController;
import com.season.lib.BaseContext;
import com.season.lib.support.bitmap.BitmapUtil;
import com.season.lib.support.bitmap.ImageMemoryCache;
import com.season.lib.support.dimen.ScreenUtils;
import com.season.mvp.ui.PageTurningActivity;
import com.season.lib.ui.view.LoadingView;

@Route(path = "/book/shelf/home")
public class BookShelfActivity extends PageTurningActivity implements DragScrollView.ICallback<BookInfo> {
    private int NUM_COLUMNS = 3;
    private int NUM_LINES = 3;
    private DragScrollView mContainer;
    private TextView mPageView;
    private LoadingView mLoadingView;
    private TransferController transferController;
    private FileLayout fileLayout;
    private ImageView animationView;

    @Override
    protected boolean isTopTileEnable() {
        return false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_shelf;
    }

    private void saveLocal(){
        BookShelfPreLoader.getInstance().saveShelfBooks(mContainer.getFinalDatas());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //沉浸式状态栏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            //lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            //lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            getWindow().setAttributes(lp);
        }


        //DragController.getInstance().disableDelFunction();
        transferController = new TransferController((ViewStub) findViewById(R.id.shelf_transfer)) {
            @Override
            protected void addFile(String filePath) {
                BaseContext.showToast("success");
                DragController.getInstance().cancelDragMode();
                mContainer.noItemAdd(BookShelfPreLoader.getInstance().decodeFile(filePath), BookShelfActivity.this);
                saveLocal();
            }
        };
        fileLayout = new FileLayout((ViewStub) findViewById(R.id.shelf_file)){
            @Override
            protected void addFile(String filePath) {
                BaseContext.showToast("success");
                DragController.getInstance().cancelDragMode();
                mContainer.noItemAdd(BookShelfPreLoader.getInstance().decodeFile(getCacheDir() + "/"+filePath),
                        BookShelfActivity.this);
                saveLocal();
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
        mPageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileLayout.inflate(BookShelfActivity.this);
                fileLayout.switchStatus();
            }
        });
        findViewById(R.id.btn_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferController.inflate(BookShelfActivity.this);
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
        DragController.getInstance().registerDragListener(new IDragListener() {
            @Override
            public void onDragEnable() {

            }

            @Override
            public void onDragDisable() {
                saveLocal();
            }

            @Override
            public void onItemDelete(int page, int position) {

            }

            @Override
            public <T> void onItemDelete(int totalPage, int page, int removePage, int position, T object) {

            }

            @Override
            public void onDragViewCreate(int page, ViewGroup itemView, MotionEvent event) {

            }

            @Override
            public void onDragViewDestroy(int page, MotionEvent event) {

            }

            @Override
            public void onItemMove(int page, MotionEvent event) {

            }

            @Override
            public void onPageChange(int lastPage, int currentPage) {

            }

            @Override
            public <T> void onPageChangeRemoveDragItem(int lastPage, int currentPage, T object) {

            }

            @Override
            public <T> void onPageChangeReplaceFirstItem(int lastPage, int currentPage, T object) {

            }

            @Override
            public void onPageChangeFinish() {

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
        height = 78 + ScreenUtils.getScreenHeight(); //刘海屏测试
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
                if (DragController.getInstance().isDragOn()){
                    return;
                }
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
                        PageTurningActivity.putCacheBitmap(bitmap);
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
        if (fileLayout.onBackPressed()) {
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



