/*
 * Code based off the PhotoSortrView from Luke Hutchinson's MTPhotoSortr
 * example (http://code.google.com/p/android-multitouch-controller/)
 *
 * License:
 *   Dual-licensed under the Apache License v2 and the GPL v2.
 */
package adu.app.photobeauty.view;

import java.util.ArrayList;

import adu.app.photobeauty.untils.Constant;
import adu.app.photobeauty.untils.SVG;
import adu.app.photobeauty.untils.SVGParser;
import adu.app.photobeauty.untils.Utils;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

public class ImageTouchEntity extends MultiTouchEntity {

	private static final double INITIAL_SCALE_FACTOR = 0.50;

	private transient Drawable mDrawable;

	private String mResourcePath;
	private String mMaskResId;
	private Bitmap mMaskBmp;
	private Bitmap mBorderBmp;
	// private Bitmap mDragBmp;

	// private int mStrokeId;
	private ArrayList<Point> mBound;
	private boolean mFreeLayout;
	private boolean mDeleted;
	private CropOverlay mCropOverlayBorder;
	private CropOverlay mCropOverlaySelect;

	public void setImageRes(String res) {
		mResourcePath = res;
	}

	public String getImageRes() {
		return mResourcePath;
	}

	public ImageTouchEntity(Context context, String resourceId,
			String maskResId, Bitmap mBorderBmp2, boolean isFreeLayout) {
		super(context.getResources());
		this.mMaskResId = maskResId;
		this.mFreeLayout = isFreeLayout;
		this.mResourcePath = resourceId;
		this.mBorderBmp = mBorderBmp2;
	}

	Paint paint;

	private boolean mSingle;

	public void setSingle(boolean b) {
		mSingle = b;
	}

	public boolean isSingle() {
		return mSingle;
	}

	public void setBorderBitmap(Bitmap b) {
		this.mBorderBmp = b;
	}

	public ImageTouchEntity(ImageTouchEntity e, Resources res) {

		super(res);

		mDrawable = e.mDrawable;
		mResourcePath = e.mResourcePath;
		mScaleX = e.mScaleX;
		mScaleY = e.mScaleY;
		mCenterX = e.mCenterX;
		mCenterY = e.mCenterY;
		mAngle = e.mAngle;
	}

	private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG
			| Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
			| Canvas.FULL_COLOR_LAYER_SAVE_FLAG
			| Canvas.CLIP_TO_LAYER_SAVE_FLAG;

	public void draw(Canvas canvas) {
		// Log.v("", "on draw");
		try {
			canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(),
					255, LAYER_FLAGS);
			canvas.save();

			// RectF rectf = new RectF(0.0F, 0.0F, getWidth(), getHeight());
			float dx = (mMaxX + mMinX) / 2;
			float dy = (mMaxY + mMinY) / 2;
			mDrawable.setBounds((int) mMinX, (int) mMinY, (int) mMaxX,
					(int) mMaxY);
			if (!mDeleted && mFreeLayout && !mIsSticker)
				mCropOverlayBorder.setBounds((int) mMinX, (int) mMinY,
						(int) mMaxX, (int) mMaxY);
			if (!mDeleted && mFreeLayout && mFocus)
				mCropOverlaySelect.setBounds((int) mMinX, (int) mMinY,
						(int) mMaxX, (int) mMaxY);
			canvas.translate(dx, dy);
			canvas.rotate(mAngle * 180.0f / (float) Math.PI);
			canvas.translate(-dx, -dy);
			mDrawable.draw(canvas);
			if (!mDeleted && mFreeLayout && !mIsSticker)
				mCropOverlayBorder.draw(canvas);
			if (!mDeleted && mFreeLayout && mFocus)
				mCropOverlaySelect.draw(canvas);
			// canvas.restoreToCount(i);
			canvas.restore();
			if (!mFreeLayout) {

				if (mIsDragOverOther) {
					// if (mDragBmp == null)
					// mDragBmp = getDragBitmap();
					// canvas.drawBitmap(mDragBmp, 0, 0, null);
					mDrawable.setAlpha(150);
				} else {
					mDrawable.setAlpha(255);

				}
				paint.setAntiAlias(true);
				paint.setXfermode(new PorterDuffXfermode(
						android.graphics.PorterDuff.Mode.DST_IN));
				paint.setFilterBitmap(true);
				paint.setDither(true);

				canvas.drawBitmap(mMaskBmp, 0, 0, paint);
				// paint.setXfermode(null);
				if (mBorderBmp != null) {
					paint.setXfermode(new PorterDuffXfermode(
							android.graphics.PorterDuff.Mode.DST_OUT));
					// paint.setColor(Color.TRANSPARENT);
					paint.setFilterBitmap(true);
					paint.setDither(true);
					canvas.drawBitmap(mBorderBmp, 0, 0, paint);
				}
				paint.setXfermode(null);
			}
			canvas.restore();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * Called by activity's onPause() method to free memory used for loading the
	 * images
	 */
	@Override
	public void unload() {
		this.mDrawable = null;
		if (mMaskBmp != null)
			mMaskBmp.recycle();
		mMaskBmp = null;
		// mBorderBmp.recycle();
		// mBorderBmp = null;
	}

	public boolean isFreeLayout() {
		return mFreeLayout;
	}

	public boolean isDeleted() {
		return mDeleted;
	}

	public void setDeleted(boolean b) {
		mDeleted = b;
	}

	/** Called by activity's onResume() method to load the images */
	@Override
	public void load(Context context) {
		mCropOverlayBorder = new CropOverlay(context);
		mCropOverlayBorder.setEnableBorder(true);
		mCropOverlaySelect = new CropOverlay(context);
		mCropOverlaySelect.setEnableBorder(false);
		mCropOverlaySelect.setLock(isLock());
		Resources res = context.getResources();
		getMetrics(res);
		paint = new Paint();
		if (mMaskBmp != null)
			mMaskBmp.recycle();
		if (!mFreeLayout)
			mMaskBmp = getMaskBitmap(context, mMaskResId);
		String uri = "";
		if (mResourcePath.startsWith("content://")
				|| mResourcePath.startsWith("file://")
				|| mResourcePath.startsWith("assets://"))
			uri = mResourcePath;
		else
			uri = "file:///" + mResourcePath;
		Bitmap bitmap = ImageLoader.getInstance().loadImageSync(uri,
				new ImageSize(Constant.displayWidth, Constant.displayWidth));
		if (!isSticker())
			bitmap = Utils.reCreateBitmap(bitmap);
		// Log.v("",
		// "load bitmap: " + bitmap.getWidth() + " " + bitmap.getHeight());
		mDrawable = new BitmapDrawable(context.getResources(), bitmap);
		mDrawable.setAlpha(255);
		mWidth = mDrawable.getIntrinsicWidth();
		mHeight = mDrawable.getIntrinsicHeight();
		float centerX;
		float centerY;
		float scaleX;
		float scaleY;
		float angle;
		if (mFreeLayout && !mSingle) {
			mStartMidX = Constant.SCREEN_MARGIN
					+ (float) (Math.random() * (Constant.maskSize - 2 * Constant.SCREEN_MARGIN));
			mStartMidY = Constant.SCREEN_MARGIN
					+ (float) (Math.random() * (Constant.maskSize - 2 * Constant.SCREEN_MARGIN));
			if (mIsSticker)
				mStartMidY = Constant.maskSize / 3;
		} else {
			mStartMidX = Constant.maskSize / 2;
			mStartMidY = Constant.maskSize / 2;
		}
		if (mFirstLoad) {
			centerX = mStartMidX;
			centerY = mStartMidY;

			angle = 0.0f;

		} else {
			centerX = mCenterX;
			centerY = mCenterY;

			angle = mAngle;
		}

		float scaleFactor = (float) Constant.maskSize
				/ (float) (Math.min(mDrawable.getIntrinsicWidth(),
						mDrawable.getIntrinsicHeight()));
		if (mFreeLayout && !mSingle)
			scaleFactor = (float) (Constant.maskSize * INITIAL_SCALE_FACTOR)
					/ (float) (Math.min(mDrawable.getIntrinsicWidth(),
							mDrawable.getIntrinsicHeight()));
		if ((mIsSticker && !mFirstLoad)) {
			scaleX = mScaleX;
			scaleY = mScaleY;
		} else
			scaleX = scaleY = scaleFactor;
		setPos(centerX, centerY, scaleX, scaleY, angle);
		mFirstLoad = false;
	}

	public void reload(Context context) {
		if (mFreeLayout) {
			super.reload(context);
		} else {
			mFirstLoad = true;
			load(context);
		}
	}

	boolean mLastPointIsTransparent;
	boolean mLastPointIsColor;
	int mLastColorPointX, mLastColorPointY;

	private boolean mIsSticker;

	// public Bitmap loadMaskInfo(Bitmap src, int replaceColor) {
	//
	// int[] allpixels = new int[src.getHeight() * src.getWidth()];
	//
	// src.getPixels(allpixels, 0, src.getWidth(), 0, 0, src.getWidth(),
	// src.getHeight());
	//
	// // for (int i = 0; i < src.getHeight() * src.getWidth(); i++) {
	// //
	// // if (allpixels[i] != replaceColor) {
	// // allpixels[i] = Color.TRANSPARENT;
	// // // Log.v("", "color: " + allpixels[i]);
	// // }
	// // }
	// // src.setPixels(allpixels, 0, src.getWidth(), 0, 0, src.getWidth(),
	// // src.getHeight());
	//
	// // Bitmap res = Bitmap.createBitmap(allpixels, src.getWidth(),
	// // src.getHeight(), Config.ARGB_8888);
	// Bitmap res = src;
	// // res = Bitmap.createScaledBitmap(res, 420, 420, true);
	//
	// int w = res.getWidth();
	// int h = res.getHeight();
	// mBound = new ArrayList<Point>();
	// for (int x = 0; x < w; x++)
	// for (int y = 0; y < h; y++) {
	// if (x == 0 || x == w - 1 || y == 0 || y == h - 1) {
	// // if (res.getPixel(x, y) != Color.TRANSPARENT)
	// mLastPointIsTransparent = true;
	//
	// }
	// if (mLastPointIsTransparent
	// && res.getPixel(x, y) != Color.TRANSPARENT) {
	// int mX = x, mY = y;
	// if (mX == 0)
	// mX = Constant.strokeSize / 2;
	// if (mY == 0)
	// mY = Constant.strokeSize / 2;
	// if (mX > w - Constant.strokeSize / 2)
	// mX = w - Constant.strokeSize / 2;
	// if (mY > h - Constant.strokeSize / 2)
	// mY = h - Constant.strokeSize / 2;
	// mBound.add(new Point(mX, mY));
	// mLastPointIsTransparent = false;
	//
	// } else {
	// if (res.getPixel(x, y) == Color.TRANSPARENT) {
	// mLastPointIsTransparent = true;
	// } else {
	// mLastPointIsTransparent = false;
	//
	// }
	// }
	//
	// if (mLastPointIsColor
	// && res.getPixel(x, y) == Color.TRANSPARENT) {
	// if (mLastColorPointX != w - 1 && mLastColorPointY != h - 1) {
	// int mX = x, mY = y;
	// if (mX == 0)
	// mX = Constant.strokeSize / 2;
	// if (mY == 0)
	// mY = Constant.strokeSize / 2;
	// if (mX > w - Constant.strokeSize / 2)
	// mX = w - Constant.strokeSize / 2;
	// if (mY > h - Constant.strokeSize / 2)
	// mY = h - Constant.strokeSize / 2;
	// mBound.add(new Point(mX, mY));
	// if (y == 0)
	// Log.v("", "add bound1" + mLastPointIsColor);
	// mLastPointIsColor = false;
	// }
	//
	// // Log.v("", "add bound");
	// } else {
	// if (res.getPixel(x, y) != Color.TRANSPARENT) {
	//
	// mLastPointIsColor = true;
	// mLastColorPointX = x;
	// mLastColorPointY = y;
	// } else {
	// mLastPointIsColor = false;
	// }
	// }
	//
	// }
	// for (int y = 0; y < h; y++)
	// for (int x = 0; x < w; x++) {
	// if (x == 0 || x == w - 1 || y == 0 || y == h - 1) {
	// // if (res.getPixel(x, y) != Color.TRANSPARENT)
	// mLastPointIsTransparent = true;
	//
	// }
	// if (mLastPointIsTransparent
	// && res.getPixel(x, y) != Color.TRANSPARENT) {
	// int mX = x, mY = y;
	// if (mX == 0)
	// mX = Constant.strokeSize / 2;
	// if (mY == 0)
	// mY = Constant.strokeSize / 2;
	// if (mX > w - Constant.strokeSize / 2)
	// mX = w - Constant.strokeSize / 2;
	// if (mY > h - Constant.strokeSize / 2)
	// mY = h - Constant.strokeSize / 2;
	//
	// mBound.add(new Point(mX, mY));
	// mLastPointIsTransparent = false;
	//
	// } else {
	// if (res.getPixel(x, y) == Color.TRANSPARENT) {
	// mLastPointIsTransparent = true;
	// } else {
	// mLastPointIsTransparent = false;
	//
	// }
	// }
	//
	// if (mLastPointIsColor
	// && res.getPixel(x, y) == Color.TRANSPARENT) {
	// if (mLastColorPointX != w - 1 && mLastColorPointY != h - 1) {
	// int mX = x, mY = y;
	// if (mX == 0)
	// mX = Constant.strokeSize / 2;
	// if (mY == 0)
	// mY = Constant.strokeSize / 2;
	// if (mX > w - Constant.strokeSize / 2)
	// mX = w - Constant.strokeSize / 2;
	// if (mY > h - Constant.strokeSize / 2)
	// mY = h - Constant.strokeSize / 2;
	// mBound.add(new Point(mX, mY));
	// if (y == 0)
	// Log.v("", "add bound1" + mLastPointIsColor);
	// mLastPointIsColor = false;
	// }
	//
	// // Log.v("", "add bound");
	// } else {
	// if (res.getPixel(x, y) != Color.TRANSPARENT) {
	//
	// mLastPointIsColor = true;
	// mLastColorPointX = x;
	// mLastColorPointY = y;
	// } else {
	// mLastPointIsColor = false;
	// }
	// }
	//
	// }
	// // src.recycle();
	// return res;
	// }

	public Bitmap getMaskBitmap(Context context, String svgRawResourceId) {
		// Rect rect = new Rect(0, 0, Constants.maskSize, Constants.maskSize);
		Bitmap bitmap = Bitmap.createBitmap(Constant.maskSize,
				Constant.maskSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		paint.setColor(Color.WHITE);
		// Log.v("", "getSVGFromResource: " + mResourcePath);
		SVG svg = SVGParser.getSVGFromString(Utils.getPatternDataFromAsset(context,
				svgRawResourceId));
		Picture picture = Utils.resizePicture(svg.getPicture(),
				Constant.maskSize, Constant.maskSize);
		// Log.v("", "svg w:" + picture.getWidth() + " h:" +
		// picture.getHeight());
		canvas.drawPicture(picture);
		return bitmap;
	}

	public boolean containsPoint(float touchX, float touchY) {
		if (!mFreeLayout) {
			if (touchX < 0 || touchX >= Constant.maskSize || touchY < 0
					|| touchY >= Constant.maskSize)
				return false;
			return mMaskBmp.getPixel((int) touchX, (int) touchY) != Color.TRANSPARENT;
			// return true;
		} else {
			return (touchX >= mMinX && touchX <= mMaxX && touchY >= mMinY && touchY <= mMaxY);
		}
	}

	public void setIsDragOverOther(boolean selected) {
		super.setIsDragOverOther(selected);
	}

	public void setIsSticker(boolean b) {
		// TODO Auto-generated method stub
		mIsSticker = b;

	}

	public boolean isSticker() {
		// TODO Auto-generated method stub
		return mIsSticker;

	}

	public void reLoadBorder() {
		// TODO Auto-generated method stub
		mCropOverlayBorder.reLoadStroker();
	}

	public void setLock(boolean b) {
		super.setLock(b);
		mCropOverlaySelect.setLock(b);
	}
}
