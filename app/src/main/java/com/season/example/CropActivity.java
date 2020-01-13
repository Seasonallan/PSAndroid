package com.season.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.season.example.support.TextModeUtil;
import com.season.lib.crop.CropView;
import com.season.lib.util.Constant;
import com.season.lib.view.ps.CustomGifFrame;
import com.season.lib.view.ps.CustomGifMovie;
import com.season.myapplication.R;

import java.util.List;

/**
 * Created by Administrator on 2017/11/1.
 */
public class CropActivity extends Activity {

    public static void start(Context context, String filePath){
        Intent intent = new Intent(context, CropActivity.class);
        intent.putExtra(Constant.IntentTag.Diy.CropActivity_File, filePath);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    String filePath;
    List<Integer> mData;
    CropView cropView;
    RecyclerView mRv;
    ImageCropAdapter mAdapter;
    ImageView preView, proView, backView, completeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        initView(savedInstanceState);
    }


    public void initView(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        filePath = getIntent().getStringExtra(Constant.IntentTag.Diy.CropActivity_File);

        if (filePath.endsWith("gif")){
            findViewById(R.id.gif_cot).setVisibility(View.VISIBLE);
            CustomGifMovie customGifMovie = findViewById(R.id.gifview);
            customGifMovie.autoPlay = true;
            boolean isSuccess = customGifMovie.setMovieResource(filePath);
            if (isSuccess) {
                customGifMovie.setVisibility(View.VISIBLE);
            } else {
                CustomGifFrame customGifFrameview = findViewById(R.id.gifFrameview);
                customGifFrameview.autoPlay = true;
                customGifFrameview.setVisibility(View.VISIBLE);
                customGifFrameview.setMovieResource(filePath);
            }
            return;
        }
        findViewById(R.id.gif_cot).setVisibility(View.GONE);

        preView = (ImageView) findViewById(R.id.iv_pre);
        proView = (ImageView) findViewById(R.id.iv_pro);
        mRv= (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRv.setLayoutManager(layoutManager);
        mData = TextModeUtil.getCropListBig();
        mAdapter =new ImageCropAdapter(this, TextModeUtil.getCropList(), TextModeUtil.getCropListSel()){
            @Override
            public void onItemClick(int position) {
                super.onItemClick(position);
                addResView(position);
            }
        };
        mRv.setAdapter(mAdapter);

        backView = (ImageView) findViewById(R.id.iv_close);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cropView.canBack()){
                    finish();
                }else{
                    cropView.clearTool();
                    resetStatus();
                    mAdapter.clearPosition();
                }
            }
        });

        completeView = (ImageView) findViewById(R.id.iv_crop);

        completeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用该方法得到剪裁好的图片
                if (!cropView.canComplete()){
                    return;
                }
                String path = cropView.getCropImage();
                Toast.makeText(CropActivity.this, path , Toast.LENGTH_SHORT).show();
            }
        });
        cropView = (CropView) findViewById(R.id.mask_view);
        boolean res = cropView.setBitmap(BitmapFactory.decodeFile(filePath));
        if (!res){
            finish();
        }

        preView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropView.undo();
                resetStatus();
            }
        });
        proView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cropView.redo();
                resetStatus();
            }
        });
        resetStatus();
        cropView.setOnActionListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStatus();
            }
        });
    }

    private void resetStatus(){
        preView.setImageResource(cropView.canPre()? R.mipmap.icon_op_pre: R.mipmap.icon_op_pre_sel);
        proView.setImageResource(cropView.canPro()? R.mipmap.icon_op_pro: R.mipmap.icon_op_pro_sel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cropView != null)cropView.release();
    }

    public void addResView(int choosePosition) {
        if (choosePosition==0){
            //多点边框裁剪
            cropView.startPathCrop();
        }else if (choosePosition==1){
            //路径裁剪
            cropView.startPathFreeCrop();
        }else {
            //图片裁剪
            cropView.startImageCrop(mData.get(choosePosition));
        }
        resetStatus();
    }


    /**
     * 裁剪 框
     * author：Create linmd on 17/3/2 16:05
     */
    public class ImageCropAdapter extends RecyclerView.Adapter<ImageCropAdapter.CropViewHolder> {
        Context mContext;

        private List<Integer> mData;
        private List<Integer> mDataSel;
        public ImageCropAdapter(Context context, List<Integer> data, List<Integer> dataSel) {
            mContext = context;
            mData = data;
            mDataSel = dataSel;
        }

        private int position = -1;
        public void onItemClick(int position){
            this.position = position;
            notifyDataSetChanged();
        }

        @Override
        public ImageCropAdapter.CropViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_crop, parent, false);
            return new ImageCropAdapter.CropViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CropViewHolder holder, final int position) {
            if (position == this.position){
                holder.mIvPic.setImageResource(mDataSel.get(position));
            }else{
                holder.mIvPic.setImageResource(mData.get(position));
            }
            holder.mIvPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void clearPosition() {
            position = -1;
            notifyDataSetChanged();
        }

        public class CropViewHolder extends RecyclerView.ViewHolder {
            ImageView mIvPic;

            public CropViewHolder(View itemView) {
                super(itemView);
                mIvPic = itemView.findViewById(R.id.iv_source);
            }

        }
    }


}
