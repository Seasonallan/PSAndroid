package com.example.lib.dimen;

import com.example.lib.BaseContext;


/**
 * 尺寸计算工具类
 */
public class DimenUtil {
	
	public static int DIPToPX(float dipValue){ 
        final float scale = BaseContext.getDisplayMetrics().density;
        return (int)(dipValue * scale);
	}


	/**
	 * 将dip或dp值转换为px值，保证尺寸大小不变
	 *
	 * @param dpValue
	 * @return
	 */
	public static int dip2px(float dpValue) {
		try {
			final float scale = BaseContext.getDisplayMetrics().density;
			return (int) (dpValue * scale + 0.5f);
		} catch (Exception e) {
			return 0;
		}
	}


	/**
	 * 将px值转换为dip或dp值，保证尺寸大小不变
	 *
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(float pxValue) {
		try {
			final float scale = BaseContext.getDisplayMetrics().density;
			return (int) (pxValue / scale + 0.5f);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 将px值转换为sp值，保证文字大小不变
	 *
	 * @param pxValue
	 * @return
	 */
	public static int px2sp( float pxValue) {
		final float fontScale = BaseContext.getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 *
	 * @param spValue
	 * @return
	 */
	public static int sp2px(float spValue) {
		final float fontScale = BaseContext.getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	/**
	 * 根据图片原始宽度和调整后的宽度得到调整后的高度
	 *
	 * @param originalWidth  原始宽度
	 * @param originalHeight 原始高度
	 * @param adjustWidth    调整后的宽度
	 * @return 调整后的高度
	 */
	public static int getAdjustHeight(int originalWidth, int originalHeight,
									  int adjustWidth) {
		float temp = (float) originalWidth / (float) adjustWidth;
		int adjustHeight = (int) ((float) originalHeight / temp);
		return adjustHeight;
	}

	/**
	 * 按比例获取长度
	 * <p/>
	 * refSize /result == refRatio/resultRadio
	 *
	 * @param refSize     参考长度
	 * @param refRatio    参考比例
	 * @param resultRadio 结果值比例
	 * @return
	 */
	public static int getSizeByScale(int refSize, int refRatio, int resultRadio) {
		return (int) ((float) refSize * resultRadio / refRatio);
	}

	public static float getDimension(int resId) {
		return BaseContext.getInstance().getResources().getDimension(resId);
	}

	public static int PXToDIP(float pxValue){ 
        final float scale = BaseContext.getDisplayMetrics().density;
        return (int)(pxValue / scale); 
	}

}
