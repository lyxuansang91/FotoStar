/**
 * PhotoSorterActivity.java
 * 
 * (c) Luke Hutchison (luke.hutch@mit.edu)
 * 
 * Released under the Apache License v2.
 */
package adu.app.photobeauty.activity;

import java.nio.channels.DatagramChannel;
import java.util.Random;

import adu.app.photobeauty.untils.Constant;
import adu.app.photobeauty.untils.Utils;
import adu.app.photobeauty.view.PhotoSortView;
import adu.app.photobeautystar.full.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PhotoCollageActivity extends PhotoActivity {

	String[] dataPath;

	// private int cheat = 99;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!Constant.checkAlive(this))
			return;
		Intent data = getIntent();
		if (data == null || data.getStringArrayExtra("all_path") == null) {
			finish();
			return;
		}
		dataPath = data.getStringArrayExtra("all_path");

		// // start cheat
		// // Constant.init(this);
		// // Utils.initImageLoader(this);
		// switch (cheat) {
		// case 2:
		// // cheat 2
		// dataPath = new String[] {
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05764_HD.jpg" };
		// break;
		// case 3:
		// // cheat 3
		// dataPath = new String[] {
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05764_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg" };
		// break;
		// case 4:
		// // cheat 4
		// dataPath = new String[] {
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05764_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05766_HD.jpg" };
		// break;
		// case 5:
		// // cheat 5
		// dataPath = new String[] {
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05764_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05766_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05750_HD.jpg" };
		// break;
		// case 6:
		// // cheat 6
		// dataPath = new String[] {
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05764_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05766_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05750_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg" };
		// break;
		// case 7:
		// // cheat 7
		// dataPath = new String[] {
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05764_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05766_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05750_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg" };
		// break;
		// case 8:
		// // cheat 8
		// dataPath = new String[] {
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05764_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05766_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05750_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg" };
		// break;
		// case 9:
		// // cheat 9
		// dataPath = new String[] {
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05764_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05766_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05750_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05771_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg",
		// "/storage/sdcard0/OGQ/BackgroundsHD/Images/05752_HD.jpg" };
		// break;
		// default:
		// break;
		// }

		setContentView(R.layout.collage_photo);
		photoSorter = (PhotoSortView) findViewById(R.id.photoSorter);
		photoSorter.init(this, dataPath, genRandomPattern());
		titleTV = (TextView) findViewById(R.id.tvTitleText);
		titleTV.setText(getString(R.string.tv_title_collage));

		toolGroup = (ViewGroup) findViewById(R.id.toolGroup);
		extendGroup = (ViewGroup) findViewById(R.id.extendGroup);
		Utils.freeMem("PhotoCollageActivity on create: ");
		Utils.popWait();

	}

	public int genRandomPattern() {

		int i = 0;
		int numPhotos = dataPath.length;
		Log.v("", "genRandomPattern: " + numPhotos);
		String patternName = "pattern/pattern" + numPhotos + "_" + i
				+ "_stroke" + Constant.PATTERN_EXT;
		while (!Utils.getPatternDataFromAsset(this, patternName).equals("")) {
			i++;
			patternName = "pattern/pattern" + numPhotos + "_" + i + "_stroke"
					+ Constant.PATTERN_EXT;
		}
		Log.v("", "nextInt: " + i);
		return new Random().nextInt(i);
	}

	private void initPatternGroup(int numPhotos) {
		if (patternGroup == null)
			patternGroup = (ViewGroup) findViewById(R.id.patternGroup);

		patternGroup.removeAllViews();
		int i = 0;
		String patternName = "pattern/pattern" + numPhotos + "_" + i
				+ "_stroke" + Constant.PATTERN_EXT;
		while (!Utils.getPatternDataFromAsset(this, patternName).equals("")) {
			// String patternId = Constant.getResId(this, patternName, "raw");
			final int index = i;

			ImageView itemImage = new ImageView(this);
			int padding = 2;
			itemImage.setPadding(padding, padding, padding, padding);
			// itemImage.setLayoutParams(params);
			itemImage.setBackgroundColor(Constant.patternBorderColor);
			itemImage.setImageBitmap(Utils.getPatternBitmap(this, patternName));
			// patternGroup.setBackgroundResource(R.drawable.aviary_adjust_knob);
			itemImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {

					// photoSorter.updateNothingSelect();

					// TODO Auto-generated method stub
					photoSorter.changePattern(PhotoCollageActivity.this, index);

					updateSelectedPattern(index, Constant.patternBorderColor);

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

			i++;
			patternName = "pattern/pattern" + numPhotos + "_" + i + "_stroke"
					+ Constant.PATTERN_EXT;

		}

	}

	public void onLayoutClick(View v) {
		photoSorter.updateNothingSelect();
		mShowingExtendTool = true;
		initPatternGroup(dataPath.length);
		Utils.doAnimation(this, toolGroup, extendGroup);
		titleTV.setText(getString(R.string.tvlayout));

	}

	public void onFreeClick(View v) {

		if (photoSorter.isFreeLayout()) {
			photoSorter.updateNothingSelect();
			return;
		}
		photoSorter.initFreeLayout(this);
		titleTV.setText(getString(R.string.tvfree));
	}

}