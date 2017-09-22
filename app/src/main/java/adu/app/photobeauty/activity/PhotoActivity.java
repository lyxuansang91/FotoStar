/**
 * PhotoSorterActivity.java
 * 
 * (c) Luke Hutchison (luke.hutch@mit.edu)
 * 
 * Released under the Apache License v2.
 */
package adu.app.photobeauty.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import adu.app.photobeauty.adapter.AnimateFirstDisplayListener;
import adu.app.photobeauty.adapter.StickerAdapter;
import adu.app.photobeauty.entity.GalleryRootObject;
import adu.app.photobeauty.untils.Constant;
import adu.app.photobeauty.untils.Utils;
import adu.app.photobeauty.view.AmbilWarnaDialog;
import adu.app.photobeauty.view.AmbilWarnaDialog.OnAmbilWarnaListener;
import adu.app.photobeauty.view.ImageTouchEntity;
import adu.app.photobeauty.view.PhotoSortView;
import adu.app.photobeautystar.full.R;

public class PhotoActivity extends BaseActivity implements OnAmbilWarnaListener {

	protected PhotoSortView photoSorter;
	protected TextView titleTV;
	protected SeekBar borderSeekBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		new ApiKeyReader().execute();
		mGalleryFolder = Utils.createAppFolder();
		// Config.maybeShowClickAdWall(this);
	}

	protected ViewGroup patternGroup;
	protected ViewGroup extendGroup;
	protected ViewGroup toolGroup;
	protected boolean mShowingExtendTool;

	public void updateSelectedSticker(int index, int normalColor) {
		for (int i = 0; i < patternGroup.getChildCount(); i++) {
			ViewGroup v = (ViewGroup) ((ViewGroup) (patternGroup.getChildAt(i)))
					.getChildAt(0);
			if (i != index)
				v.getChildAt(0).setBackgroundColor(normalColor);
			else
				v.getChildAt(0).setBackgroundColor(
						Constant.patternBorderColorSelected);
		}
		patternGroup.invalidate();

	}

	public void updateSelectedPattern(int index, int normalColor) {
		for (int i = 0; i < patternGroup.getChildCount(); i++) {
			if (i != index)
				patternGroup.getChildAt(i).setBackgroundColor(normalColor);
			else
				patternGroup.getChildAt(i).setBackgroundColor(
						Constant.patternBorderColorSelected);
		}
		patternGroup.invalidate();

	}

	private File target = null;
	boolean mSuccess;

	private void captureScreen() {
		photoSorter.updateNothingSelect();
		Utils.onWait(this);
		new Thread() {
			public void run() {
				Bitmap bitmap = photoSorter.onCapture(1024, 1024);
				if (target == null)
					target = new File(mGalleryFolder,
							System.currentTimeMillis() + ".jpg");
				Log.d("", "Target path: " + target.getPath());

				try {
					FileOutputStream out = new FileOutputStream(target);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
					mSuccess = true;

				} catch (Exception e) {
					mSuccess = false;
					e.printStackTrace();
				}
				bitmap.recycle();
				bitmap = null;
				runOnUiThread(new Runnable() {
					public void run() {

						if (mSuccess) {
							Toast.makeText(
									PhotoActivity.this,
									getString(R.string.msg_save_complete) + " "
											+ target.getAbsolutePath(),
									Toast.LENGTH_LONG).show();
							showFinishMenu();
							updateMedia(target.getAbsolutePath());
						} else {
							Toast.makeText(PhotoActivity.this,
									getString(R.string.msg_save_err),
									Toast.LENGTH_LONG).show();
						}
						Utils.popWait();
					}
				});

			}

		}.start();

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Config.onResumeService(this);
		// photoSorter.loadImages(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Config.onPauseService(this);
		// photoSorter.unloadImages();
	}

	public void onCheckBtn(View v) {
		if (mShowingExtendTool) {
			onBackPressed();
			return;
		}
		captureScreen();
	}

	public void onBackBtn(View v) {
		onBackPressed();
	}

	public void updateTitle() {
		titleTV.setText(getString(R.string.tv_title_collage));
	}

	public void onBackPressed() {
		if (pickPopup != null && pickPopup.isShowing()) {
			pickPopup.dismiss();
			return;
		}
		if (mShowingExtendTool) {
			mShowingExtendTool = false;
			Utils.doAnimation(this, extendGroup, toolGroup);
			updateTitle();
			if (patternGroup != null) {
				patternGroup.removeAllViews();
				patternGroup = null;
			}
			findViewById(R.id.borderGroup).setVisibility(View.GONE);
			if (stickerAdapter != null)
				stickerAdapter.clear();
			findViewById(R.id.gridSticker).setVisibility(View.GONE);

			return;
		}

		confirmBack();
	}

	public void confirmBack() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.lbl_confirm_back));
		builder.setMessage(getString(R.string.msg_confirm_back))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.lbl_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								photoSorter.unloadImages(true);
								ImageLoader.getInstance().clearMemoryCache();
								new Thread() {
									public void run() {
										Utils.deleteTempFiles();
									}
								}.start();
								// Config.maybeShowAdWall(PhotoActivity.this);
								Utils.freeMem("Photo Sort onbackpressed: ");
								finish();
							}
						})
				.setNegativeButton(getString(R.string.lbl_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

//	class ApiKeyReader extends AsyncTask<Void, Void, String> {
//
//		@Override
//		protected String doInBackground(Void... params) {
//			return SDKUtils.getApiKey(getBaseContext());
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			super.onPostExecute(result);
//			setApiKey(result);
//		}
//
//	}
//
//	private void setApiKey(String value) {
//
//		Constant.mApiKey = value;
//
//		if (null == value) {
//			String message = SDKUtils.MISSING_APIKEY_MESSAGE;
//			new AlertDialog.Builder(this).setTitle("API-KEY Missing!")
//					.setMessage(message).show();
//		}
//	}

	/**
	 * Check the external storage status
	 * 
	 * @return
	 */
	private boolean isExternalStorageAvilable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	private static final int EXTERNAL_STORAGE_UNAVAILABLE = 1;
	private static final String LOG_TAG = null;
	String mOutputFilePath;
	private String mSessionId;
	// private PhotoSortView mPhotoSortrView;
	protected ImageTouchEntity mImageEntity;
	protected PopupWindow pickPopup;

	// private String oldPath;

	private void pickFromGalleryForChange(ImageTouchEntity currImageEntity) {
		// String uri = currImageEntity.getImageRes();
		mImageEntity = currImageEntity;
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		Intent chooser = Intent.createChooser(intent, "Choose a Picture");
		startActivityForResult(chooser,
				Constant.ACTION_REQUEST_PICK_GALLERY_CHANGE);
	}

	private void pickFromGalleryForMask() {

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		Intent chooser = Intent.createChooser(intent, "Choose a Picture");
		startActivityForResult(chooser,
				Constant.ACTION_REQUEST_PICK_GALLERY_MASK);
	}

	private void pickFromCameraForChange(ImageTouchEntity currImageEntity) {
		// String uri = currImageEntity.getImageRes();
		mImageEntity = currImageEntity;
		Uri imageUri = Uri.fromFile(getCameraTempFile(false));
		Intent intent = createIntentForCamera(imageUri);
		startActivityForResult(intent,
				Constant.ACTION_REQUEST_PICK_CAMERA_CHANGE);
	}

	private void pickFromCameraForMask() {
		Uri imageUri = Uri.fromFile(getCameraTempFile(false));
		Intent intent = createIntentForCamera(imageUri);
		startActivityForResult(intent, Constant.ACTION_REQUEST_PICK_CAMERA_MASK);

	}

	private File mCameraTmp;

	private File getCameraTempFile(boolean forResult) {
		if (forResult)
			return mCameraTmp;
		String fileName = "camera_" + System.currentTimeMillis() + ".tmp";
		mCameraTmp = new File(mGalleryFolder, fileName);
		return mCameraTmp;
	}

	private Intent createIntentForCamera(Uri imageUri) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		return intent;
	}

	public void pickPhoto(final ImageTouchEntity img, final int action) {
		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.pick, null);
		Button cameraBtn = (Button) layout.findViewById(R.id.camerabtn);
		// TextView cameraTV = (TextView) layout.findViewById(R.id.cameratv);
		cameraBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pickPopup.dismiss();
				// TODO Auto-generated method stub
				switch (action) {
				case Constant.ACTION_CHANGE:
					pickFromCameraForChange(img);
					break;
				case Constant.ACTION_MASK:
					pickFromCameraForMask();
					break;
				default:
					break;
				}
			}
		});
		// cameraTV.setOnClickListener(cameraListener);
		Button galleryBtn = (Button) layout.findViewById(R.id.gallerybtn);
		// TextView galleryTV = (TextView) layout.findViewById(R.id.gallerytv);
		galleryBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pickPopup.dismiss();
				// TODO Auto-generated method stub
				switch (action) {
				case Constant.ACTION_CHANGE:
					pickFromGalleryForChange(img);
					break;
				case Constant.ACTION_MASK:
					pickFromGalleryForMask();
					break;
				default:
					break;
				}
			}
		});
		// galleryTV.setOnClickListener(galleryListener);
		pickPopup = new PopupWindow(this);
		pickPopup.setContentView(layout);
		pickPopup.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
		pickPopup.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
		pickPopup.setFocusable(true);
		pickPopup.showAtLocation(layout, Gravity.NO_GRAVITY,
				Constant.displayWidth / 2 - (int) (90 * Constant.density),
				Constant.displayHeight / 2 - (int) (55 * Constant.density));
	}

//	public void startFeather(ImageTouchEntity currImageEntity) {
//		// Config.maybeShowClickAdWall(this);
//		String uri = currImageEntity.getImageRes();
//		mImageEntity = currImageEntity;
//		// first check the external storage availability
//		if (!isExternalStorageAvilable()) {
//			showDialog(EXTERNAL_STORAGE_UNAVAILABLE);
//			return;
//		}
//
//		// create a temporary file where to store the resulting image
//		File file = getNextFileName();
//
//		if (null != file) {
//			mOutputFilePath = file.getAbsolutePath();
//		} else {
//			new AlertDialog.Builder(this)
//					.setTitle(android.R.string.dialog_alert_title)
//					.setMessage("Failed to create a new File").show();
//			return;
//		}
//
//		// Create the intent needed to start feather
//		Intent newIntent = new Intent(this, FeatherActivity.class);
//
//		// === INPUT IMAGE URI (MANDATORY) ===
//		// Set the source image uri
//		newIntent.setData(Uri.parse(uri));
//
//		// === API KEY SECRET (MANDATORY) ====
//		// You must pass your Aviary key secret
//		newIntent.putExtra(Constants.EXTRA_IN_API_KEY_SECRET,
//				Constant.API_SECRET);
//
//		// === OUTPUT (OPTIONAL/RECOMMENDED)====
//		// Pass the uri of the destination image file.
//		// This will be the same uri you will receive in the onActivityResult
//		newIntent.putExtra(Constants.EXTRA_OUTPUT,
//				Uri.parse("file://" + mOutputFilePath));
//
//		// === OUTPUT FORMAT (OPTIONAL) ===
//		// Format of the destination image
//		newIntent.putExtra(Constants.EXTRA_OUTPUT_FORMAT,
//				Bitmap.CompressFormat.JPEG.name());
//
//		// === OUTPUT QUALITY (OPTIONAL) ===
//		// Output format quality (jpeg only)
//		newIntent.putExtra(Constants.EXTRA_OUTPUT_QUALITY, 90);
//
//		// === WHITE LABEL (OPTIONAL/PREMIUM ONLY) ===
//		// If you want to hide the 'feedback' button and the 'aviary' logo
//		// pass this intent-extra
//		// Note that you need to have the 'whitelabel' permissions enabled in
//		// order
//		// to use this extra
//		newIntent.putExtra(Constants.EXTRA_WHITELABEL, true);
//
//		// == TOOLS LIST ===
//		// Optional
//		// You can force feather to display only some tools ( see
//		// FilterLoaderFactory#Filters )
//		// you can omit this if you just want to display the default tools
//
//		newIntent.putExtra("tools-list", new String[] {
//				FilterLoaderFactory.Filters.CROP.name(),
//				FilterLoaderFactory.Filters.ENHANCE.name(),
//				FilterLoaderFactory.Filters.EFFECTS.name(),
//				FilterLoaderFactory.Filters.BRIGHTNESS.name(),
//				FilterLoaderFactory.Filters.CONTRAST.name(),
//				FilterLoaderFactory.Filters.SATURATION.name(),
//				FilterLoaderFactory.Filters.BORDERS.name(),
//				FilterLoaderFactory.Filters.STICKERS.name(),
//				FilterLoaderFactory.Filters.COLOR_SPLASH.name(),
//				FilterLoaderFactory.Filters.TILT_SHIFT.name(),
//				FilterLoaderFactory.Filters.DRAWING.name(),
//				FilterLoaderFactory.Filters.TEXT.name(),
//				FilterLoaderFactory.Filters.SHARPNESS.name(),
//				FilterLoaderFactory.Filters.ADJUST.name(),
//				FilterLoaderFactory.Filters.COLORTEMP.name(),
//				FilterLoaderFactory.Filters.RED_EYE.name(),
//				FilterLoaderFactory.Filters.WHITEN.name(),
//				FilterLoaderFactory.Filters.BLEMISH.name(),
//				FilterLoaderFactory.Filters.MEME.name(), });
//
//		// === EXIT ALERT (OPTIONAL) ===
//		// Uou want to hide the exit alert dialog shown when back is pressed
//		// without saving image first
//		// newIntent.putExtra( Constants.EXTRA_HIDE_EXIT_UNSAVE_CONFIRMATION,
//		// true );
//
//		// === VIBRATION (OPTIONAL) ===
//		// Some aviary tools use the device vibration in order to give a better
//		// experience
//		// to the final user. But if you want to disable this feature, just pass
//		// any value with the key "tools-vibration-disabled" in the calling
//		// intent.
//		// This option has been added to version 2.1.5 of the Aviary SDK
//		// newIntent.putExtra( Constants.EXTRA_TOOLS_DISABLE_VIBRATION, true );
//
//		// === MAX SIZE (OPTIONAL) ===
//		// you can pass the maximum allowed image size (for the preview),
//		// otherwise feather will determine
//		// the max size based on the device informations.
//		// This will not affect the hi-res image size.
//		// Here we're passing the current display size as max image size because
//		// after
//		// the execution of Aviary we're saving the HI-RES image so we don't
//		// need a big
//		// image for the preview
//
//		int max_size = Math.max(Constant.displayWidth, Constant.displayHeight);
//		max_size = (int) ((float) max_size / 1.2f);
//		newIntent.putExtra(Constants.EXTRA_MAX_IMAGE_SIZE, max_size);
//
//		// === HI-RES (OPTIONAL) ===
//		// You need to generate a new session id key to pass to Aviary feather
//		// this is the key used to operate with the hi-res image ( and must be
//		// unique for every new instance of Feather )
//		// The session-id key must be 64 char length.
//		// In your "onActivityResult" method, if the resultCode is RESULT_OK,
//		// the returned
//		// bundle data will also contain the "session" key/value you are passing
//		// here.
//		mSessionId = StringUtils.getSha256(System.currentTimeMillis()
//				+ Constant.mApiKey);
//		Log.d("", "session: " + mSessionId + ", size: " + mSessionId.length());
//		newIntent.putExtra(Constants.EXTRA_OUTPUT_HIRES_SESSION_ID, mSessionId);
//
//		// === NO CHANGES (OPTIONAL) ==
//		// With this extra param you can tell to FeatherActivity how to manage
//		// the press on the Done button even when no real changes were made to
//		// the image.
//		// If the value is true then the image will be always saved, a RESULT_OK
//		// will be returned to your onActivityResult and the result Bundle will
//		// contain an extra value "EXTRA_OUT_BITMAP_CHANGED" indicating if the
//		// image was changed during the session.
//		// If "false" is passed then a RESULT_CANCEL wsill be sent when an user
//		// will
//		// hit the 'Done' button without any modifications ( also the
//		// EXTRA_OUT_BITMAP_CHANGED
//		// extra will be sent back to the onActivityResult.
//		// By default this value is true ( even if you omit it )
//		newIntent.putExtra(Constants.EXTRA_IN_SAVE_ON_NO_CHANGES, true);
//
//		// ..and start feather
//		startActivityForResult(newIntent, Constant.ACTION_REQUEST_FEATHER);
//	}

	private File mGalleryFolder;

	private File getNextFileName() {
		if (mGalleryFolder != null) {
			if (mGalleryFolder.exists()) {
				File file = new File(mGalleryFolder, "feather"
						+ System.currentTimeMillis() + ".tmp");
				return file;
			}
		}
		return null;
	}

	/**
	 * We need to notify the MediaScanner when a new file is created. In this
	 * way all the gallery applications will be notified too.
	 * 
	 * @param file
	 */
	private void updateMedia(String filepath) {
		Log.i(LOG_TAG, "updateMedia: " + filepath);
		MediaScannerConnection.scanFile(getApplicationContext(),
				new String[] { filepath }, null, null);

	}

	/**
	 * Delete the session and all it's actions. We do not need it anymore.<br />
	 * Note that this is optional. All old sessions are automatically removed in
	 * Feather.
	 * 
	 * @param session_id
	 */
//	private void deleteSession(final String session_id) {
//		Uri uri = FeatherContentProvider.SessionsDbColumns.getContentUri(this,
//				session_id);
//		getContentResolver().delete(uri, null, null);
//	}

	/**
	 * Delete a file without throwing any exception
	 * 
	 * @param path
	 * @return
	 */

	protected ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private LayoutInflater infalter;
	private View convertView;

	public void initBorderGroup() {
		if (patternGroup == null)
			patternGroup = (ViewGroup) findViewById(R.id.patternGroup);
		findViewById(R.id.borderGroup).setVisibility(View.VISIBLE);
		if (photoSorter.isFreeLayout()) {
			findViewById(R.id.borderColorBtn).setVisibility(View.VISIBLE);
			findViewById(R.id.borderColorBtn).setBackgroundColor(
					Constant.cropOverLayColor);
		} else {
			findViewById(R.id.borderColorBtn).setVisibility(View.GONE);
		}
		// if (borderSeekBar == null) {
		borderSeekBar = (SeekBar) findViewById(R.id.borderSlider);
		borderSeekBar.setMax(Constant.MAX_BORDER_SIZE_LAYOUT);
		borderSeekBar.setProgress(Constant.strokeSize);
		// Log.v("", "update seekbar:" + photoSorter.isFreeLayout()
		// + Constant.strokeSize);
		if (photoSorter.isFreeLayout()) {
			borderSeekBar.setProgress(Constant.cropOverLayStroke);
			borderSeekBar.setMax(Constant.MAX_BORDER_SIZE_FREE);
		}
		borderSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekbar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekbar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekbar, int i, boolean flag) {
				// TODO Auto-generated method stub
				if (flag) {
					photoSorter.updateNothingSelect();
					if (photoSorter.isFreeLayout()) {
						Constant.cropOverLayStroke = i;
						photoSorter.reLoadBorder();
					} else {
						Constant.strokeSize = i;
						photoSorter.reLoadBorderBitmap(PhotoActivity.this);
					}
				}
			}
		});
		// }

		if (infalter == null)
			infalter = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		patternGroup.removeAllViews();
		LinearLayout.LayoutParams paramsGroup = new LinearLayout.LayoutParams(
				Constant.patternSize + (int) (15 * Constant.density),
				Constant.patternSize + (int) (15 * Constant.density));
		paramsGroup.setMargins((int) (Constant.density * 12),
				(int) (Constant.density * 2), (int) (Constant.density * 12),
				(int) (Constant.density * 2));

		final String[] pathListString = Utils.listMaskFile(this, "bg");
		for (int i = 0; i < pathListString.length; i++) {
			// String patternId = Constant.getResId(this, patternName, "raw");
			final int index = i;
			ImageView itemImage = new ImageView(this);
			itemImage.setPadding(Constant.patternBorderSize,
					Constant.patternBorderSize, Constant.patternBorderSize,
					Constant.patternBorderSize);
			itemImage.setBackgroundColor(Constant.patternBorderColor);
			int bitmapResId = Constant.getResId(
					this,
					pathListString[i].substring(0,
							pathListString[i].indexOf('.')), "drawable");
			if (i == 0)
				bitmapResId = R.drawable.ic_pickcolor;
			if (bitmapResId > 0) {
				// Drawable d = getResources().getDrawable(bitmapResId);
				// d.setBounds(0, 0, 10, 10);
				itemImage.setImageDrawable(getResources().getDrawable(
						bitmapResId));

			}
			itemImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					photoSorter.updateNothingSelect();
					if (index == 0)
						new AmbilWarnaDialog(PhotoActivity.this,
								Constant.strokeColor, PhotoActivity.this,
								Constant.PICK_COLOR_ACTION_BG).show();
					else
						photoSorter
								.changeBackgroundBitmap(pathListString[index]);
					updateSelectedPattern(index, Constant.patternBorderColor);
				}
			});
			patternGroup.addView(itemImage, paramsGroup);
		}

	}

	public void initFrameGroup() {
		if (patternGroup == null)
			patternGroup = (ViewGroup) findViewById(R.id.patternGroup);
		findViewById(R.id.borderGroup).setVisibility(View.GONE);

		if (infalter == null)
			infalter = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		patternGroup.removeAllViews();
		LinearLayout.LayoutParams paramsGroup = new LinearLayout.LayoutParams(
				Constant.patternSize + (int) (15 * Constant.density),
				Constant.patternSize + (int) (15 * Constant.density));
		paramsGroup.setMargins((int) (Constant.density * 12),
				(int) (Constant.density * 2), (int) (Constant.density * 12),
				(int) (Constant.density * 2));

		final String[] pathListString = Utils.listMaskFile(this, "fr");
		for (int i = 0; i < pathListString.length; i++) {
			final int index = i;
			ImageView itemImage = new ImageView(this);
			itemImage.setPadding(Constant.patternBorderSize,
					Constant.patternBorderSize, Constant.patternBorderSize,
					Constant.patternBorderSize);
			itemImage.setBackgroundColor(Constant.patternBorderColor);

			int bitmapResId = Constant.getResId(
					this,
					pathListString[i].substring(0,
							pathListString[i].indexOf('.')), "drawable");
			if (i == 0)
				bitmapResId = R.drawable.ic_no_frame;
			if (bitmapResId > 0) {
				itemImage.setImageDrawable(getResources().getDrawable(
						bitmapResId));

			}
			itemImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					photoSorter.updateNothingSelect();
					if (index == 0)
						photoSorter.changeFrame("");
					else
						photoSorter.changeFrame(pathListString[index]);
					updateSelectedPattern(index, Constant.patternBorderColor);
				}
			});
			patternGroup.addView(itemImage, paramsGroup);
		}

	}

	public void initStickerGroup() {
		if (patternGroup == null)
			patternGroup = (ViewGroup) findViewById(R.id.patternGroup);
		patternGroup.removeAllViews();
		if (infalter == null)
			infalter = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		int i = 0;
		String stickerName = "sticker_lbl_" + i;

		while (Constant.getResId(this, stickerName, "string") > 0) {
			final int index = i;
			String firstPatternPath = "sticker/"
					+ String.valueOf(i)
					+ "/"
					+ Utils.listMaskFile(this, "sticker/" + String.valueOf(i))[0];
			final String maskToolName = getString(Constant.getResId(this,
					stickerName, "string"));
			convertView = infalter.inflate(R.layout.mask_item, null);

			ImageView img = (ImageView) convertView.findViewById(R.id.maskImg);
			img.setBackgroundColor(Color.TRANSPARENT);
			// img.setPadding(Constant.patternBorderSize,
			// Constant.patternBorderSize, Constant.patternBorderSize,
			// Constant.patternBorderSize);
			img.setImageBitmap(ImageLoader.getInstance().loadImageSync(
					"assets://" + firstPatternPath,
					new ImageSize(Constant.patternSize, Constant.patternSize)));
			img.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					titleTV.setText(maskToolName);
					updateSelectedSticker(index, Color.TRANSPARENT);
					onStickerToolClick(index);
					Utils.freeMem("on change pattern: ");
					// Utils.popWait();
				}

			});
			TextView tv = (TextView) convertView.findViewById(R.id.maskTV);
			tv.setText(maskToolName);
			LinearLayout.LayoutParams paramsGroup = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsGroup
					.setMargins((int) (Constant.density * 12),
							(int) (Constant.density * 2),
							(int) (Constant.density * 12),
							(int) (Constant.density * 2));
			patternGroup.addView(convertView, paramsGroup);
			i++;
			stickerName = "sticker_lbl_" + i;
		}
	}

	protected GridView stickerGrid;
	protected StickerAdapter stickerAdapter;

	public void onStickerToolClick(int index) {
		photoSorter.updateNothingSelect();
		if (index == 3) {
			if (stickerAdapter != null)
				stickerAdapter.clear();
			if (stickerGrid != null)
				stickerGrid.setVisibility(View.GONE);
			pickPhoto(null, Constant.ACTION_MASK);
			return;
		}
		if (stickerGrid == null)
			stickerGrid = (GridView) findViewById(R.id.gridSticker);
		stickerGrid.setVisibility(View.VISIBLE);
		if (stickerAdapter != null)
			stickerAdapter.clear();
		stickerAdapter = new StickerAdapter(this, ImageLoader.getInstance());
		String dataPath[] = Utils.listMaskFile(this,
				"sticker/" + String.valueOf(index));
		String procol = "assets://sticker/" + String.valueOf(index) + "/";
		ArrayList<GalleryRootObject> list = new ArrayList<GalleryRootObject>();
		for (int i = 0; i < dataPath.length; i++) {
			GalleryRootObject data = new GalleryRootObject();
			data.setSdCardPath(procol + dataPath[i]);
			list.add(data);
		}
		stickerAdapter.addData(list);
		stickerGrid.setAdapter(stickerAdapter);
		stickerGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				photoSorter.updateNothingSelect();
				// TODO Auto-generated method stub
				photoSorter.addSticker(stickerAdapter.getItemAt(arg2)
						.getSdCardPath());
				stickerGrid.setVisibility(View.GONE);
			}
		});
		// TODO Auto-generated method stub

	}

	public void onBorderClick(View v) {
		photoSorter.updateNothingSelect();
		mShowingExtendTool = true;
		initBorderGroup();
		titleTV.setText(getString(R.string.tvborder));
		Utils.doAnimation(this, toolGroup, extendGroup);

	}

	public void onFrameClick(View v) {
		photoSorter.updateNothingSelect();
		mShowingExtendTool = true;
		initFrameGroup();
		titleTV.setText(getString(R.string.tvframe));
		Utils.doAnimation(this, toolGroup, extendGroup);

	}

	public void onStickerClick(View v) {
		photoSorter.updateNothingSelect();
		mShowingExtendTool = true;
		initStickerGroup();
		Utils.doAnimation(this, toolGroup, extendGroup);
		titleTV.setText(getString(R.string.tvsticker));
	}

	public void onTextClick(View v) {
		photoSorter.updateNothingSelect();
		Utils.onWait(this);
		Intent intent = new Intent();
		intent.setClass(this, TextMakerActivity.class);
		// intent.putExtra("img_path", uri);
		startActivityForResult(intent, Constant.ACTION_REQUEST_TEXT);
	}

	public void onPaintClick(View v) {
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case Constant.ACTION_REQUEST_PICK_GALLERY_CHANGE:
				// user chose an image from the gallery
				// loadAsync(data.getData());
				photoSorter.updateData(mImageEntity, data.getData().toString());
				break;
			case Constant.ACTION_REQUEST_PICK_CAMERA_CHANGE:
				// user chose an image from the gallery
				// loadAsync(data.getData());
				photoSorter.updateData(mImageEntity,
						Uri.fromFile(getCameraTempFile(true)).toString());
				break;
			case Constant.ACTION_REQUEST_PICK_GALLERY_MASK:
				makeMaskSticker(data.getData().toString());
				break;
			case Constant.ACTION_REQUEST_PICK_CAMERA_MASK:
				makeMaskSticker(Uri.fromFile(getCameraTempFile(true))
						.toString());
				break;
			case Constant.ACTION_REQUEST_MASK:
				// user chose an image from the gallery
				// loadAsync(data.getData());
				Bundle mBuddle = data.getExtras();
				String newPath = mBuddle.getString("new_path");
				Log.v("", "newPath" + newPath);
				// photoSorter.deleteImage(mImageEntity);
				photoSorter.addSticker(newPath);
				break;
			case Constant.ACTION_REQUEST_MASK_STICKER:
				// user chose an image from the gallery
				// loadAsync(data.getData());
				Bundle bundle = data.getExtras();
				String path = bundle.getString("new_path");
				Log.v("", "path" + path);
				photoSorter.addSticker(path);
				break;
			case Constant.ACTION_REQUEST_TEXT:
				// user chose an image from the gallery
				// loadAsync(data.getData());
				Bundle mBuddle2 = data.getExtras();
				String newPath2 = mBuddle2.getString("new_path");
				Log.v("", "newPath" + newPath2);
				photoSorter.addSticker(newPath2);
				break;
			case Constant.ACTION_REQUEST_FEATHER:

//				boolean changed = true;
//
//				if (null != data) {
//					Bundle extra = data.getExtras();
//					if (null != extra) {
//						// image was changed by the user?
//						changed = extra
//								.getBoolean(Constants.EXTRA_OUT_BITMAP_CHANGED);
//					}
//				}
//
//				if (!changed) {
//					Log.w(LOG_TAG,
//							"User did not modify the image, but just clicked on 'Done' button");
//				}
//
//				// send a notification to the media scanner
//				// updateMedia(mOutputFilePath);
//				photoSorter.updateData(mImageEntity, mOutputFilePath);
//
//				mOutputFilePath = null;
				break;
			}
		} else if (resultCode == RESULT_CANCELED) {
			switch (requestCode) {
			case Constant.ACTION_REQUEST_FEATHER:

//				// feather was cancelled without saving.
//				// we need to delete the entire session
//				if (null != mSessionId)
//					deleteSession(mSessionId);
//
//				// delete the result file, if exists
//				if (mOutputFilePath != null) {
//					// deleteFileNoThrow(mOutputFilePath);
//					mOutputFilePath = null;
//				}
				break;
			}
		}
	}

	public void onChangeBorderColorBtn(View v) {
		new AmbilWarnaDialog(this, Constant.cropOverLayColor, this,
				Constant.PICK_COLOR_ACTION_BORDER).show();
	}

	public void makeMask(ImageTouchEntity currImageEntity) {
		// TODO Auto-generated method stub
		String uri = currImageEntity.getImageRes();
		mImageEntity = currImageEntity;
		Intent intent = new Intent();
		intent.setClass(this, MaskMakerActivity.class);
		intent.putExtra("img_path", uri);
		startActivityForResult(intent, Constant.ACTION_REQUEST_MASK);

	}

	public void makeMaskSticker(String uri) {
		Intent intent = new Intent();
		intent.setClass(this, MaskMakerActivity.class);
		intent.putExtra("img_path", uri);
		startActivityForResult(intent, Constant.ACTION_REQUEST_MASK_STICKER);

	}

	private Dialog finishDialog;

	public void showFinishMenu() {
		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.popup_finish, null);
		Button shareBtn = (Button) layout.findViewById(R.id.sharebtn);
		TextView shareTV = (TextView) layout.findViewById(R.id.sharetv);
		shareBtn.setOnClickListener(onClickListener);
		shareTV.setOnClickListener(onClickListener);
		Button wallBtn = (Button) layout.findViewById(R.id.wallbtn);
		TextView wallTV = (TextView) layout.findViewById(R.id.walltv);
		wallBtn.setOnClickListener(onClickListener);
		wallTV.setOnClickListener(onClickListener);
		Button viewBtn = (Button) layout.findViewById(R.id.viewbtn);
		TextView viewTV = (TextView) layout.findViewById(R.id.viewtv);
		viewBtn.setOnClickListener(onClickListener);
		viewTV.setOnClickListener(onClickListener);
		finishDialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);
		finishDialog.setContentView(layout);
		finishDialog.setCanceledOnTouchOutside(false);
		finishDialog.show();

	}

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finishDialog.dismiss();
			if (target == null || !target.exists()) {
				return;

			}
			Uri phototUri = Uri.parse("file:///" + target.getAbsolutePath());
			if (v.getId() == R.id.sharebtn || v.getId() == R.id.sharetv) {
				Intent shareIntent = new Intent(Intent.ACTION_SEND);

				shareIntent.setData(phototUri);
				shareIntent.setType("image/png");
				shareIntent.putExtra(Intent.EXTRA_STREAM, phototUri);
				startActivity(Intent.createChooser(shareIntent,
						getString(R.string.tvshare)));

			} else if (v.getId() == R.id.wallbtn || v.getId() == R.id.walltv) {

				boolean res = Utils.setWallPaper(PhotoActivity.this, Utils
						.decodeSampledBitmapFromFile(PhotoActivity.this,
								target.getAbsolutePath(),
								Constant.displayWidth, Constant.displayWidth));
				if (res)
					Toast.makeText(PhotoActivity.this,
							getString(R.string.msg_set_wall_complete),
							Toast.LENGTH_LONG).show();
				else
					Toast.makeText(PhotoActivity.this,
							getString(R.string.msg_set_wall_err),
							Toast.LENGTH_LONG).show();
			} else if (v.getId() == R.id.viewbtn || v.getId() == R.id.viewtv) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(phototUri, "image/*");
				startActivity(Intent.createChooser(intent,
						getString(R.string.tvview)));
			}

		}
	};

	@Override
	public void onCancel(AmbilWarnaDialog dialog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOk(AmbilWarnaDialog dialog, int color, int action) {
		// TODO Auto-generated method stub
		dialog.getDialog().dismiss();
		switch (action) {
		case Constant.PICK_COLOR_ACTION_BG:
			photoSorter.changeBackgroundColor(color);
			break;
		case Constant.PICK_COLOR_ACTION_BORDER:
			Constant.cropOverLayColor = color;
			findViewById(R.id.borderColorBtn).setBackgroundColor(color);
			photoSorter.reLoadBorder();
			break;
		default:
			break;
		}

	}

}