package adu.app.photobeauty.view;

import com.nostra13.universalimageloader.core.ImageLoader;

import adu.app.photobeauty.untils.Constant;
import adu.app.photobeautystar.full.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/**
 * A custom View to draw a transparent box with a border, and a semi transparent
 * color outside the border.
 */
public class CropOverlay extends View {
	private Paint borderPaint, exteriorPaint, selectPaint;
	private Rect zoneRect;
	private int left, top, right, bottom;
	private boolean mEnableBorder = false;
	private boolean mLock;
	private Bitmap lockBmp;

	public void setEnableBorder(boolean b) {
		mEnableBorder = b;
	}

	public void setLock(boolean b) {
		mLock = b;
		if (!mLock) {
			if (lockBmp != null)
				lockBmp.recycle();
			lockBmp = null;
		}
	}

	public CropOverlay(Context context) {
		super(context);
		init();
	}

	public CropOverlay(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CropOverlay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		zoneRect = new Rect();
		// Resources resources = getContext().getResources();
		borderPaint = new Paint();
		borderPaint.setColor(Constant.cropOverLayColor);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setAntiAlias(true);
		borderPaint.setStrokeWidth(Constant.cropOverLayStroke);

		exteriorPaint = new Paint();
		exteriorPaint.setColor(Color.GRAY);
		exteriorPaint.setStyle(Paint.Style.STROKE);
		exteriorPaint.setStrokeWidth(Constant.cropOverLayStroke);
		exteriorPaint.setAlpha(200);
		exteriorPaint.setAntiAlias(true);
		// select
		selectPaint = new Paint();
		selectPaint.setColor(Constant.cropOverLayColorSelect);
		selectPaint.setStyle(Paint.Style.STROKE);
		selectPaint.setStrokeWidth(Constant.cropOverLayStrokeSelect);
		selectPaint.setAntiAlias(true);
		// selectPaint.setPathEffect(new DashPathEffect(new float[] {
		// Constant.cropOverLayStrokeSelect,
		// 2 * Constant.cropOverLayStrokeSelect }, 0));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mEnableBorder) {
			if (Constant.cropOverLayStroke == 0)
				return;
			zoneRect.left = left - 1;
			zoneRect.top = top;
			zoneRect.right = right + 1;
			zoneRect.bottom = bottom + 2;

			canvas.drawRect(zoneRect, exteriorPaint);
			zoneRect.left = left;
			zoneRect.top = top;
			zoneRect.right = right;
			zoneRect.bottom = bottom;
			canvas.drawRect(zoneRect, borderPaint);
		} else {
			zoneRect.left = left;
			zoneRect.top = top;
			zoneRect.right = right;
			zoneRect.bottom = bottom;
			canvas.drawRect(zoneRect, selectPaint);
			if (mLock) {
				if (lockBmp == null) {
					lockBmp = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_lock);
					lockBmp = Bitmap.createScaledBitmap(lockBmp,
							(int) (Constant.density * 30),
							(int) (Constant.density * 30), false);
				}

				canvas.drawBitmap(lockBmp, left, top, null);
			}

		}
		// for (Rect r : exterior)
		// canvas.drawRect(r, exteriorPaint);
	}

	public void setBounds(int mMinX, int mMinY, int mMaxX, int mMaxY) {
		// TODO Auto-generated method stub
		left = mMinX;
		top = mMinY;
		right = mMaxX;
		bottom = mMaxY;

	}

	public void reLoadStroker() {
		borderPaint.setStrokeWidth(Constant.cropOverLayStroke);
		exteriorPaint.setStrokeWidth(Constant.cropOverLayStroke);
		borderPaint.setColor(Constant.cropOverLayColor);

	}
}
