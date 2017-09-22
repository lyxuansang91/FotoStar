/**
 * PhotoSorterView.java
 * 
 * (c) Luke Hutchison (luke.hutch@mit.edu)
 * 
 * TODO: Add OpenGL acceleration.
 * 
 * Released under the Apache License v2.
 */
package adu.app.photobeauty.view;

import java.util.ArrayList;

import adu.app.photobeauty.activity.PhotoActivity;
import adu.app.photobeauty.activity.PhotoCollageActivity;
import adu.app.photobeauty.untils.Constant;
import adu.app.photobeauty.untils.MultiTouchController;
import adu.app.photobeauty.untils.MultiTouchController.MultiTouchObjectCanvas;
import adu.app.photobeauty.untils.MultiTouchController.PointInfo;
import adu.app.photobeauty.untils.MultiTouchController.PositionAndScale;
import adu.app.photobeauty.untils.SVG;
import adu.app.photobeauty.untils.SVGParser;
import adu.app.photobeauty.untils.Utils;
import adu.app.photobeautystar.full.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

public class PhotoSortView extends View implements
		MultiTouchObjectCanvas<MultiTouchEntity> {
	private String[] imagePaths = new String[0];
	// private static final int[] IMAGES = { R.drawable.m74hubble,
	// R.drawable.catarina, R.drawable.tahiti, R.drawable.sunset,
	// R.drawable.lake };
	// private static final int[] IMAGES = { R.drawable.tahiti };

	private ArrayList<MultiTouchEntity> mImages = new ArrayList<MultiTouchEntity>();
	private ArrayList<ImageTouchEntity> mSticker = new ArrayList<ImageTouchEntity>();
	private boolean mFreeLayout;
	// --

	private MultiTouchController<MultiTouchEntity> multiTouchController = new MultiTouchController<MultiTouchEntity>(
			this);

	// --

	private PointInfo currTouchPoint = new PointInfo();

	private boolean mShowDebugInfo = false;

	private static final int UI_MODE_ROTATE = 1, UI_MODE_ANISOTROPIC_SCALE = 2;

	private int mUIMode = UI_MODE_ROTATE;

	// --

	private Paint mLinePaintTouchPointCircle = new Paint();

	// private static final float SCREEN_MARGIN = 100;

	// private int width, height;

	// ---------------------------------------------------------------------------------------------------
	private PhotoActivity context;

	public PhotoSortView(Context context) {
		this(context, null);
	}

	public PhotoSortView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PhotoSortView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void init(PhotoActivity context, String[] data, int patternNum) {
		this.context = context;
		mFreeLayout = false;
		this.imagePaths = data;
		this.patternNum = patternNum;
		// TODO Auto-generated method stub
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				Constant.maskSize, Constant.maskSize);
		params.setMargins(Constant.maskMargin, Constant.maskMargin / 3,
				Constant.maskMargin, Constant.maskMargin);
		setLayoutParams(params);
		init(context);
	}

	public void initSingle(PhotoActivity context, String data) {
		this.context = context;
		mFreeLayout = true;
		mSingle = true;
		this.imagePaths = new String[] { data };
		// TODO Auto-generated method stub

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				Constant.maskSize, Constant.maskSize);
		params.setMargins(Constant.maskMargin, Constant.maskMargin / 3,
				Constant.maskMargin, Constant.maskMargin);
		setLayoutParams(params);
		mFreeLayout = true;
		init(context);
	}

	public ImageTouchEntity getSingleImage() {
		return (ImageTouchEntity) mImages.get(0);
	}

	public void initMaskMaker(Activity context, String imagePath,
			String maskResId) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				Constant.maskSize, Constant.maskSize);
		params.setMargins(Constant.maskMargin, Constant.maskMargin / 3,
				Constant.maskMargin, Constant.maskMargin);
		setLayoutParams(params);
		unloadImages(false);
		mImages.clear();
		Log.v("", "initMaskMaker: ");
		mImages.add(new ImageTouchEntity(context, imagePath, maskResId, null,
				false));
		loadImages(context);

	}

	public void initFreeLayout(final Activity context) {
		Utils.onWait(context);
		mFreeLayout = true;
		new Thread() {
			public void run() {

				init(context);
				context.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						updateNothingSelect();

						Utils.popWait();
						Utils.freeMem("initFreeLayout: ");
					}
				});

			}
		}.start();
	}

	public boolean isFreeLayout() {
		return mFreeLayout;
	}

	private int patternNum;
	// private Paint paint;

	private Bitmap handBmp;

	public void changePattern(final Activity context, int patternNum) {
		if (this.patternNum == patternNum && !mFreeLayout)
			return;
		Utils.onWait(context);
		mFreeLayout = false;
		this.patternNum = patternNum;
		new Thread() {
			public void run() {
				init(context);
				context.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						updateNothingSelect();
						Utils.popWait();
						Utils.freeMem("on change pattern: ");
					}
				});

			}
		}.start();

	}

	private int bmHeight;
	private int bmWidth;

	private boolean mSingle;

	public void changeBackgroundBitmap(String bitmapPath) {
		Log.v("", "changeBackgroundBitmap: " + bitmapPath);
		if (!bitmapPath.contains("pattern")) {
			mBackgroundBmp = ImageLoader.getInstance().loadImageSync(
					"assets://bg/" + bitmapPath,
					new ImageSize(Constant.maskSize, Constant.maskSize));
			if (mBackgroundBmp.getWidth() < Constant.maskSize
					|| mBackgroundBmp.getHeight() < Constant.maskSize) {
				mBackgroundBmp = Bitmap.createScaledBitmap(mBackgroundBmp,
						Constant.maskSize, Constant.maskSize, false);
			}
		} else {
			mBackgroundBmp = ImageLoader.getInstance().loadImageSync(
					"assets://bg/" + bitmapPath);

		}
		bmWidth = mBackgroundBmp.getWidth();
		bmHeight = mBackgroundBmp.getHeight();
		invalidate();
	}

	public void changeFrame(String bitmapPath) {
		Log.v("", "changeFrame: " + bitmapPath);
		if (bitmapPath.equals("")) {
			if (mFrameBmp != null)
				mFrameBmp.recycle();
			mFrameBmp = null;

		} else
		// mFrameBmp = Utils.decodeSampledBitmapFromAssets(getContext(), "fr/"
		// + bitmapPath, Constant.maskSize, Constant.maskSize);
		{
			mFrameBmp = ImageLoader.getInstance().loadImageSync(
					"assets://fr/" + bitmapPath,
					new ImageSize(Constant.maskSize, Constant.maskSize));
			mFrameBmp = Bitmap.createScaledBitmap(mFrameBmp, Constant.maskSize,
					Constant.maskSize, false);
		}

		invalidate();
	}

	public void changeBackgroundColor(int color) {
		if (mBackgroundBmp != null)
			mBackgroundBmp.recycle();
		mBackgroundBmp = null;
		Constant.strokeColor = color;
		invalidate();
	}

	private void init(Context context) {
		if (imagePaths.length == 0)
			return;

		unloadImages(false);
		mImages.clear();
		Log.v("", "init start: " + imagePaths.length);
		int mPatternType = imagePaths.length;
		if (mPatternType > 1) {
			mBorderId = "pattern/pattern" + mPatternType + "_" + patternNum
					+ "_stroke" + Constant.PATTERN_EXT;
			mBorderBmp = getBorderBitmap(context);
		}
		// mBackgroundBmp = ImageLoader.getInstance().loadImageSync(
		// "assets://bg/bg6.jpg",
		// new ImageSize(Constant.maskSize, Constant.maskSize));
		for (int i = 0; i < imagePaths.length; i++) {
			ImageTouchEntity entity = new ImageTouchEntity(context,
					imagePaths[i], "pattern/pattern" + mPatternType + "_"
							+ patternNum + "_" + i + Constant.PATTERN_EXT,
					mBorderBmp, mFreeLayout);
			entity.setSingle(mSingle);
			mImages.add(entity);
		}
		// mBorderId = Constants.getResId(context, "pattern" + mPatternNum + "_"
		// + testPatternId + "_stroke", "raw");

		// paint = new Paint();
		// paint.setAntiAlias(true);
		// paint.setFilterBitmap(true);
		// paint.setDither(true);
		// paint.setXfermode(new PorterDuffXfermode(
		// android.graphics.PorterDuff.Mode.DST_OUT));
		// setBackgroundColor(context.getResources().getColor(R.color.dark_gray));
		if (!mFreeLayout) {
			handBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_hand);
			handBmp = Bitmap.createScaledBitmap(handBmp, Constant.handW,
					Constant.handW, false);
		}
		loadImages(context);

	}

	/** Called by activity's onResume() method to load the images */
	public void loadImages(Context context) {

		Log.d("PhotoSortrView", "Width: " + this.getWidth());
		Log.d("PhotoSortrView", "Height: " + this.getHeight());
		// mImages.get(0).load(context);
		int n = mImages.size();
		for (int i = 0; i < n; i++) {
			mImages.get(i).load(context);
		}
		for (ImageTouchEntity entry : mSticker) {
			entry.load(context);
		}
	}

	/**
	 * Called by activity's onPause() method to free memory used for loading the
	 * images
	 */
	public void unloadImages(boolean isDestroy) {
		int n = mImages.size();
		for (int i = 0; i < n; i++)
			mImages.get(i).unload();
		for (ImageTouchEntity entry : mSticker) {
			entry.unload();
		}
		if (handBmp != null)
			handBmp.recycle();
		handBmp = null;
		if (mBorderBmp != null)
			mBorderBmp.recycle();
		mBorderBmp = null;
		if (isDestroy) {
			if (mBackgroundBmp != null)
				mBackgroundBmp.recycle();
			mBackgroundBmp = null;
			if (mFrameBmp != null)
				mFrameBmp.recycle();
			mFrameBmp = null;
			mSticker.clear();
		}
	}

	// ---------------------------------------------------------------------------------------------------

	// public Bitmap captureBitmap = null;
	// public boolean mCapturing;

	public void onDraw(Canvas canvas) {
		// canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(),
		// 255,
		// Constants.LAYER_FLAGS);

		super.onDraw(canvas);
		if (mBackgroundBmp != null) {
			// canvas.drawBitmap(mBackgroundBmp, 0, 0, null);
			for (int y = 0, height = canvas.getHeight(); y < height; y += bmHeight) {
				for (int x = 0, width = canvas.getWidth(); x < width; x += bmWidth) {
					canvas.drawBitmap(mBackgroundBmp, x, y, null);
				}
			}
		} else {
			canvas.drawColor(Constant.strokeColor);
		}

		for (int i = 0; i < mImages.size(); i++)
			mImages.get(i).draw(canvas);

		if (mShowDebugInfo)
			drawMultitouchDebugMarks(canvas);
		for (ImageTouchEntity entry : mSticker) {
			Log.v("", "ImageLayoutEntity draw");
			entry.draw(canvas);
		}
		if (mFrameBmp != null) {
			canvas.drawBitmap(mFrameBmp, 0, 0, null);

		}

	}

	public Bitmap onCaptureMask(int w, int h) {
		int preColor = Constant.strokeColor;
		Constant.strokeColor = Color.TRANSPARENT;
		float f = (float) w / (float) getWidth();
		float f1 = (float) h / (float) getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h,
				android.graphics.Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.scale(f, f1);
		draw(canvas);
		Constant.strokeColor = preColor;
		return bitmap;
	}

	public Bitmap onCapture(int w, int h) {
//		float f = (float) w / (float) getWidth();
//		float f1 = (float) h / (float) getHeight();
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				android.graphics.Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
//		canvas.scale(f, f1);
		draw(canvas);
		return bitmap;
	}

	// ---------------------------------------------------------------------------------------------------

	public void trackballClicked() {
		mUIMode = (mUIMode + 1) % 3;
		invalidate();
	}

	private void drawMultitouchDebugMarks(Canvas canvas) {
		if (currTouchPoint.isDown()) {
			float[] xs = currTouchPoint.getXs();
			float[] ys = currTouchPoint.getYs();
			float[] pressures = currTouchPoint.getPressures();
			int numPoints = Math.min(currTouchPoint.getNumTouchPoints(), 2);
			for (int i = 0; i < numPoints; i++)
				// canvas.drawCircle(xs[i], ys[i], 50 + pressures[i] * 80,
				// mLinePaintTouchPointCircle);
				canvas.drawBitmap(handBmp, xs[i] - Constant.handW, ys[i]
						- Constant.handW, null);
			if (numPoints == 2)
				canvas.drawLine(xs[0], ys[0], xs[1], ys[1],
						mLinePaintTouchPointCircle);
		}
	}

	// ---------------------------------------------------------------------------------------------------

	/** Pass touch events to the MT controller */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return multiTouchController.onTouchEvent(event);
	}

	/**
	 * Get the image that is under the single-touch point, or return null
	 * (canceling the drag op) if none
	 */
	public MultiTouchEntity getDraggableObjectAtPoint(PointInfo pt) {
		float x = pt.getX(), y = pt.getY();
		int n = mSticker.size();
		for (int i = n - 1; i >= 0; i--) {
			if (mSticker.get(i).containsPoint(x, y))
				return mSticker.get(i);
		}
		n = mImages.size();
		for (int i = n - 1; i >= 0; i--) {
			ImageTouchEntity im = (ImageTouchEntity) mImages.get(i);
			if (im.containsPoint(x, y))
				return im;
		}
		Log.v("", "getDraggableOnOtherObjectAtPoint null");
		return null;
	}

	public void updateNothingSelect() {
		// Log.v("", "updateNothingSelect");
		if (isFreeLayout())
			for (MultiTouchEntity entry : mImages) {
				entry.setIsFocus(false);
			}
		for (MultiTouchEntity entry : mSticker) {
			entry.setIsFocus(false);
		}
		invalidate();
	};

	public void updateCurrentSelect(MultiTouchEntity curr) {
		if (isFreeLayout())
			for (MultiTouchEntity entry : mImages) {
				if (curr != entry)
					entry.setIsFocus(false);
				else
					entry.setIsFocus(true);
			}
		for (MultiTouchEntity entry : mSticker) {
			if (curr != entry)
				entry.setIsFocus(false);
			else
				entry.setIsFocus(true);
		}
		invalidate();
	}

	public MultiTouchEntity getDraggableOnOtherObjectAtPoint(
			ImageTouchEntity currDrag, PointInfo pt) {
		float x = pt.getX(), y = pt.getY();
		int n = mImages.size();
		for (int i = n - 1; i >= 0; i--) {
			ImageTouchEntity im = (ImageTouchEntity) mImages.get(i);
			if (currDrag != im)
				if (im.containsPoint(x, y))
					return im;
		}

		return null;
	}

	/**
	 * Select an object for dragging. Called whenever an object is found to be
	 * under the point (non-null is returned by getDraggableObjectAtPoint()) and
	 * a drag operation is starting. Called with null when drag op ends.
	 */
	public void selectObject(MultiTouchEntity img, PointInfo touchPoint) {
		// Log.v("", "selectObject " + (img == null));
		if (img != null)
			updateCurrentSelect(img);
		// if (!touchPoint.isDown())
		// updateNothingSelect();
		if (touchPoint.isDown())
			mDraging = false;
		// swap 2 image
		if (!mFreeLayout) {

			if (!touchPoint.isDown() && mShowDebugInfo) {
				if (mCurrDragOver != null && mCurrDrag != null) {
					// Log.v("", "touchPoint: " + touchPoint.isDown());
					String mRes = ((ImageTouchEntity) mCurrDragOver)
							.getImageRes();
					((ImageTouchEntity) mCurrDragOver)
							.setImageRes(((ImageTouchEntity) mCurrDrag)
									.getImageRes());
					((ImageTouchEntity) mCurrDrag).setImageRes(mRes);
					mCurrDrag.setIsDragOverOther(false);
					mCurrDragOver.setIsDragOverOther(false);
					mCurrDrag.reload(getContext());
					mCurrDragOver.reload(getContext());
					mShowDebugInfo = false;
					mCurrDragOver = null;
					mCurrDrag = null;
					invalidate();

					return;
				}
			}
		}
		if (!touchPoint.isDown() && !mDraging && !mShowDebugInfo) {
			ImageTouchEntity currImageEntity = (ImageTouchEntity) getDraggableObjectAtPoint(touchPoint);
			if (currImageEntity == null)
				return;

			showStatusPopup(currImageEntity, (int) touchPoint.getX(),
					(int) touchPoint.getY());
			Log.v("", "selectObject");
		}
		currTouchPoint.set(touchPoint);
		// Log.v("", "select obj:" + ((ImageLayoutEntity) img).isSticker());
		if (img != null) {
			// Move image to the top of the stack when selected
			if (!img.isLock()) {
				if (mSticker.remove(img)) {
					mSticker.add((ImageTouchEntity) img);
				} else {
					mImages.remove(img);
					mImages.add(img);
				}
			}

		} else {
			// Called with img == null when drag stops.
		}

		// Toast.makeText(getContext(), "touch me!", Toast.LENGTH_LONG).show();
		invalidate();
	}

	/**
	 * Get the current position and scale of the selected image. Called whenever
	 * a drag starts or is reset.
	 */
	public void getPositionAndScale(MultiTouchEntity img,
			PositionAndScale objPosAndScaleOut) {
		// FIXME affine-izem (and fix the fact that the anisotropic_scale part
		// requires averaging the two scale factors)
		objPosAndScaleOut.set(img.getCenterX(), img.getCenterY(),
				(mUIMode & UI_MODE_ANISOTROPIC_SCALE) == 0,
				(img.getScaleX() + img.getScaleY()) / 2,
				(mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0, img.getScaleX(),
				img.getScaleY(), (mUIMode & UI_MODE_ROTATE) != 0,
				img.getAngle());
	}

	MultiTouchEntity mCurrDragOver = null;
	MultiTouchEntity mCurrDrag = null;

	private boolean mDraging;

	/** Set the position and scale of the dragged/stretched image. */
	public boolean setPositionAndScale(MultiTouchEntity img,
			PositionAndScale newImgPosAndScale, PointInfo touchPoint) {
		mDraging = true;
		// Log.v("", "touchPoint.getActionExtra()" +
		// touchPoint.getActionExtra());
		if (!mFreeLayout && !((ImageTouchEntity) img).isSticker())
			if (touchPoint.getActionExtra() == 1) {

				MultiTouchEntity mDragged = getDraggableOnOtherObjectAtPoint(
						(ImageTouchEntity) img, touchPoint);
				if (mDragged != null) {
					if (mCurrDragOver != null && mCurrDragOver != mDragged) {
						mCurrDragOver.setIsDragOverOther(false);
						mCurrDragOver = null;
					}
					mCurrDragOver = mDragged;
				} else {
					if (mCurrDragOver != null) {
						mCurrDragOver.setIsDragOverOther(false);
						mCurrDragOver = null;
					}
				}
				if (mCurrDragOver != null) {
					// Log.v("",
					// "getDraggableOnOtherObjectAtPoint: "
					// + touchPoint.getActionExtra());

					img.setIsDragOverOther(true);
					mCurrDragOver.setIsDragOverOther(true);
					mCurrDrag = img;
					mShowDebugInfo = true;

				} else {
					img.setIsDragOverOther(false);
					mShowDebugInfo = false;
				}
			} else {
				mShowDebugInfo = false;
				img.setIsDragOverOther(false);
				if (mCurrDragOver != null) {
					mCurrDragOver.setIsDragOverOther(false);
					mCurrDragOver = null;
				}
			}
		currTouchPoint.set(touchPoint);
		boolean ok = false;
		if (!img.isLock())
			ok = ((ImageTouchEntity) img).setPos(newImgPosAndScale);
		if (ok)
			invalidate();
		return ok;
	}

	public boolean pointInObjectGrabArea(PointInfo pt, MultiTouchEntity img) {
		return false;
	}

	private Bitmap mBackgroundBmp;
	private Bitmap mFrameBmp;
	private Bitmap mBorderBmp;
	private String mBorderId;

	private Bitmap getBorderBitmap(Context context) {
		// TODO Auto-generated method stub
		Log.v("", "getBorderBitmap: " + mBorderId);
		Bitmap bitmap = Bitmap.createBitmap(Constant.maskSize,
				Constant.maskSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		String svgBorderData = Utils.getPatternDataFromAsset(context, mBorderId);
		StringBuilder modStr = new StringBuilder();
		modStr.append("stroke-width=");
		modStr.append('"');
		modStr.append('2');
		modStr.append('"');
		String res = modStr.toString().replace("2",
				String.valueOf(Constant.strokeSize));
		svgBorderData = svgBorderData.replace(modStr.toString(), res);
		SVG svg = SVGParser.getSVGFromString(svgBorderData);

		Picture picture = Utils.resizePicture(svg.getPicture(),
				Constant.maskSize, Constant.maskSize);
		canvas.drawPicture(picture);
		Log.v("", "bitmap w:" + bitmap.getWidth());
		return bitmap;
	}

	public void updatePath(String oldPath, String mOutputFilePath) {
		// TODO Auto-generated method stub

		for (int i = 0; i < imagePaths.length; i++) {

			if (imagePaths[i].equals(oldPath)) {
				imagePaths[i] = mOutputFilePath;
				return;
			}
		}
	}

	private ImageTouchEntity currImageEntity;
	PopupWindow changeStatusPopUp;

	public void showStatusPopup(ImageTouchEntity currImageEntity, int x, int y) {
		this.currImageEntity = currImageEntity;
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.popup, null);
		if (currImageEntity.isSticker()) {
			layout = layoutInflater.inflate(R.layout.popup_delete, null);
			Button deleteBtn = (Button) layout
					.findViewById(R.id.popupDeleteBtn);
			TextView deleteTV = (TextView) layout
					.findViewById(R.id.popupDeleteTV);
			deleteBtn.setOnClickListener(deleteListener);
			deleteTV.setOnClickListener(deleteListener);
			Button lockBtn = (Button) layout.findViewById(R.id.popupLockBtn);
			TextView lockTV = (TextView) layout.findViewById(R.id.popupLockTV);
			if (currImageEntity.isLock()) {
				lockTV.setText(getContext().getString(R.string.tvunlock));
				lockBtn.setBackgroundResource(R.drawable.popup_unlock_selector);
			} else {
				lockTV.setText(getContext().getString(R.string.tvlock));
				lockBtn.setBackgroundResource(R.drawable.popup_lock_selector);

			}
			lockBtn.setOnClickListener(lockListener);
			lockTV.setOnClickListener(lockListener);
			changeStatusPopUp = new PopupWindow(context);
			changeStatusPopUp.setContentView(layout);
			changeStatusPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
			changeStatusPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
			changeStatusPopUp.setFocusable(true);
			changeStatusPopUp.showAtLocation(layout, Gravity.NO_GRAVITY, x, y);
			return;
		}
		if (!currImageEntity.isFreeLayout()) {
			layout.findViewById(R.id.lockGroup).setVisibility(View.GONE);
		}
		Button maskBtn = (Button) layout.findViewById(R.id.popupMaskBtn);
		TextView maskTV = (TextView) layout.findViewById(R.id.popupMaskTV);
		maskBtn.setOnClickListener(maskListener);
		maskTV.setOnClickListener(maskListener);
		Button editBtn = (Button) layout.findViewById(R.id.popupEditBtn);
		TextView editTV = (TextView) layout.findViewById(R.id.popupEditTV);
		editBtn.setOnClickListener(editListener);
		editTV.setOnClickListener(editListener);
		Button changeBtn = (Button) layout.findViewById(R.id.popupChangeBtn);
		TextView changeTV = (TextView) layout.findViewById(R.id.popupChangeTV);
		changeBtn.setOnClickListener(changeListener);
		changeTV.setOnClickListener(changeListener);
		Button deleteBtn = (Button) layout.findViewById(R.id.popupDeleteBtn);
		TextView deleteTV = (TextView) layout.findViewById(R.id.popupDeleteTV);
		deleteBtn.setOnClickListener(deleteListener);
		deleteTV.setOnClickListener(deleteListener);

		Button lockBtn = (Button) layout.findViewById(R.id.popupLockBtn);
		TextView lockTV = (TextView) layout.findViewById(R.id.popupLockTV);
		if (currImageEntity.isLock()) {
			lockTV.setText(getContext().getString(R.string.tvunlock));
			lockBtn.setBackgroundResource(R.drawable.popup_unlock_selector);
		} else {
			lockTV.setText(getContext().getString(R.string.tvlock));
			lockBtn.setBackgroundResource(R.drawable.popup_lock_selector);

		}
		lockBtn.setOnClickListener(lockListener);
		lockTV.setOnClickListener(lockListener);
		changeStatusPopUp = new PopupWindow(context);
		changeStatusPopUp.setContentView(layout);
		changeStatusPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
		changeStatusPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
		changeStatusPopUp.setFocusable(true);
		changeStatusPopUp.showAtLocation(layout, Gravity.NO_GRAVITY, x, y);

	}

	private OnClickListener editListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			changeStatusPopUp.dismiss();
			// TODO Auto-generated method stub
			if (currImageEntity.getImageRes().endsWith("_"))
				return;
//			context.startFeather(currImageEntity);

		}
	};

	private OnClickListener changeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			changeStatusPopUp.dismiss();

			context.pickPhoto(currImageEntity, Constant.ACTION_CHANGE);
		}
	};
	private OnClickListener maskListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			changeStatusPopUp.dismiss();
			if (currImageEntity.getImageRes().endsWith("_"))
				return;

			context.makeMask(currImageEntity);

		}
	};
	private OnClickListener lockListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			changeStatusPopUp.dismiss();
			boolean b = !currImageEntity.isLock();
			currImageEntity.setLock(b);
			invalidate();
		}
	};
	private OnClickListener deleteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			changeStatusPopUp.dismiss();
			confirmDelete();
		}
	};

	public void confirmDelete() {
		Context context = getContext();
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

		builder.setMessage(context.getString(R.string.msg_confirm_delete))
				.setCancelable(false)
				.setPositiveButton(context.getString(R.string.lbl_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								deleteImage(currImageEntity);
							}
						})
				.setNegativeButton(context.getString(R.string.lbl_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void deleteImage(ImageTouchEntity img) {
		if (img.getImageRes().endsWith("_"))
			return;

		updateData(img, img.getImageRes() + "_");
		img.setDeleted(true);
		// if (!mImages.remove(currImageEntity))
		if (img.isSticker())
			mSticker.remove(img);

	}

	public void updateData(ImageTouchEntity mImageEntity, String newPath) {
		// TODO Auto-generated method stub
		String oldPath = mImageEntity.getImageRes();
		mImageEntity.setImageRes(newPath);
		mImageEntity.reload(context);
		invalidate();
		updatePath(oldPath, newPath);
	}

	public void reLoadBorderBitmap(Context context) {
		if (Constant.strokeSize == 0)
			mBorderBmp = null;
		else
			mBorderBmp = getBorderBitmap(context);
		for (int i = 0; i < mImages.size(); i++) {
			((ImageTouchEntity) mImages.get(i)).setBorderBitmap(mBorderBmp);
		}
		invalidate();

	}

	public void addSticker(String imgPath) {
		ImageTouchEntity newEntity = new ImageTouchEntity(context, imgPath,
				null, null, true);
		newEntity.setIsSticker(true);
		mSticker.add(newEntity);
		newEntity.load(context);
		invalidate();

	}

	public void reLoadBorder() {
		// TODO Auto-generated method stub
		for (int i = 0; i < mImages.size(); i++) {
			((ImageTouchEntity) mImages.get(i)).reLoadBorder();
		}
		invalidate();
	}
}
