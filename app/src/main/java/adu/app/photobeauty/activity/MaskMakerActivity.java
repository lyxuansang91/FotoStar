/**
 * PhotoSorterActivity.java
 * 
 * (c) Luke Hutchison (luke.hutch@mit.edu)
 * 
 * Released under the Apache License v2.
 */
package adu.app.photobeauty.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import adu.app.photobeauty.untils.Constant;
import adu.app.photobeauty.untils.Utils;
import adu.app.photobeauty.view.PhotoSortView;
import adu.app.photobeautystar.full.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class MaskMakerActivity extends PhotoActivity {

	private static final String TAG = null;
	private String dataPath;
	private ViewGroup toolGroupInside;

	private LayoutInflater infalter;
	private View convertView;
	private int saveBgColor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (!Constant.checkAlive(this))
			return;
		Intent data = getIntent();
		if (data == null || data.getStringExtra("img_path") == null) {
			finish();
			return;
		}
		saveBgColor = Constant.strokeColor;
		Constant.strokeColor = Color.WHITE;
		dataPath = data.getStringExtra("img_path");
		setContentView(R.layout.mask_maker);
		photoSorter = (PhotoSortView) findViewById(R.id.photoSorter);
		String listPath[] = Utils.listMaskFile(this, "mask/0");
		photoSorter.initMaskMaker(this, dataPath, "mask/0/"
				+ listPath[new Random().nextInt(listPath.length)]);
		titleTV = (TextView) findViewById(R.id.tvTitleText);
		titleTV.setText(getString(R.string.tv_title_mask_maker));
		toolGroup = (ViewGroup) findViewById(R.id.toolGroup);
		extendGroup = (ViewGroup) findViewById(R.id.extendGroup);
		initToolGroup();
	}

	private void initToolGroup() {
		if (toolGroupInside == null)
			toolGroupInside = (ViewGroup) findViewById(R.id.toolGroupInside);
		else
			return;
		infalter = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		toolGroupInside.removeAllViews();
		int i = 0;
		String maskName = "mask_lbl_" + i;

		while (Constant.getResId(this, maskName, "string") > 0) {
			final int index = i;
			String firstPatternPath = "mask/" + String.valueOf(i) + "/"
					+ Utils.listMaskFile(this, "mask/" + String.valueOf(i))[0];
			final String maskToolName = getString(Constant.getResId(this,
					maskName, "string"));
			convertView = infalter.inflate(R.layout.mask_item, null);

			ImageView img = (ImageView) convertView.findViewById(R.id.maskImg);
			img.setBackgroundColor(Constant.patternBorderColor);
			img.setImageBitmap((Utils.getPatternBitmap(this, firstPatternPath)));
			img.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {

					updateMaskTool(index);
					onMaskToolClick(index, maskToolName);
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
			toolGroupInside.addView(convertView, paramsGroup);
			i++;
			maskName = "mask_lbl_" + i;
		}
	}

	private void initPatternGroup(int numFolder) {
		if (patternGroup == null)
			patternGroup = (ViewGroup) findViewById(R.id.patternGroup);

		patternGroup.removeAllViews();

		String pathList[] = Utils.listMaskFile(this, "mask/" + numFolder);
		for (int i = 0; i < pathList.length; i++) {

			final int index = i;
			final String patternName = "mask/" + numFolder + "/" + pathList[i];
			ImageView itemImage = new ImageView(this);
			itemImage.setPadding(Constant.patternBorderSize,
					Constant.patternBorderSize, Constant.patternBorderSize,
					Constant.patternBorderSize);
			itemImage.setBackgroundColor(Constant.patternBorderColor);
			itemImage.setImageBitmap(Utils.getPatternBitmap(this, patternName));
			// patternGroup.setBackgroundResource(R.drawable.aviary_adjust_knob);
			itemImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {

					// Utils.onWait(PhotoSortrActivity.this);
					// TODO Auto-generated method stub
					// photoSorter.changePattern(MaskMakerActivity.this, index);
					// photoSorter.invalidate();
					updateSelectedPattern(index);
					photoSorter.initMaskMaker(MaskMakerActivity.this, dataPath,
							patternName);
					photoSorter.invalidate();
					Utils.freeMem("on change pattern: ");
				}
			});
			LinearLayout.LayoutParams paramsGroup = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsGroup
					.setMargins((int) (Constant.density * 12),
							(int) (Constant.density * 2),
							(int) (Constant.density * 12),
							(int) (Constant.density * 2));
			patternGroup.addView(itemImage, paramsGroup);

		}

	}

	private void updateMaskTool(int index) {
		for (int i = 0; i < toolGroupInside.getChildCount(); i++) {
			ViewGroup v = (ViewGroup) ((ViewGroup) (toolGroupInside
					.getChildAt(i))).getChildAt(0);
			if (i != index)
				v.getChildAt(0).setBackgroundColor(Constant.patternBorderColor);
			else
				v.getChildAt(0).setBackgroundColor(
						Constant.patternBorderColorSelected);
		}
		toolGroupInside.invalidate();

	}

	private void updateSelectedPattern(int index) {
		for (int i = 0; i < patternGroup.getChildCount(); i++) {
			View v = patternGroup.getChildAt(i);
			if (i != index)
				v.setBackgroundColor(Constant.patternBorderColor);
			else
				v.setBackgroundColor(Constant.patternBorderColorSelected);
		}
		patternGroup.invalidate();

	}

	private String makeBitmap() {
		Bitmap bitmap = photoSorter.onCaptureMask(Constant.maskSize,
				Constant.maskSize);
		File target = new File(Constant.FOLDER_PATH + "/", "mask_"
				+ System.currentTimeMillis() + ".tmp");
		Log.d(TAG, "Target path: " + Constant.FOLDER_PATH + target.getPath());
		try {
			FileOutputStream out = new FileOutputStream(target);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			Log.e(TAG, "Unable to capture image", e);
		}
		bitmap.recycle();
		bitmap = null;
		return target.getAbsolutePath();
	}

	private void onMaskToolClick(int index, String title) {
		mShowingExtendTool = true;
		initPatternGroup(index);
		Utils.doAnimation(this, toolGroup, extendGroup);
		titleTV.setText(title);
	}

	public void onCheckBtn(View v) {
		if (mShowingExtendTool) {
			onBackPressed();
			return;
		}
		String res = makeBitmap();
		Intent intent = getIntent();
		intent.putExtra("new_path", res);
		setResult(RESULT_OK, intent);
		clearRes();
		finish();
	}

	public void onBackBtn(View v) {
		onBackPressed();
	}

	public void clearRes() {
		Constant.strokeColor = saveBgColor;
		if (patternGroup != null) {
			patternGroup.removeAllViews();
			patternGroup = null;
		}
		if (toolGroupInside != null) {
			toolGroupInside.removeAllViews();
			toolGroupInside = null;
		}

		photoSorter.unloadImages(true);
		ImageLoader.getInstance().clearMemoryCache();
		Utils.freeMem(TAG + " onbackpressed: ");
	}

	public void onBackPressed() {
		if (mShowingExtendTool) {
			mShowingExtendTool = false;
			Utils.doAnimation(this, extendGroup, toolGroup);
			patternGroup.removeAllViews();
			titleTV.setText(getString(R.string.tv_title_mask_maker));
			return;
		}
		clearRes();
		finish();
	}

}