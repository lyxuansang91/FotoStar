/**
 * PhotoSorterActivity.java
 * 
 * (c) Luke Hutchison (luke.hutch@mit.edu)
 * 
 * Released under the Apache License v2.
 */
package adu.app.photobeauty.activity;

//import thomc.app.config.Config;
import adu.app.photobeauty.untils.Constant;
import adu.app.photobeauty.untils.Utils;
import adu.app.photobeauty.view.PhotoSortView;
import adu.app.photobeautystar.full.R;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PhotoEditActivity extends PhotoActivity {

	String dataPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!Constant.checkAlive(this))
			return;
		Intent data = getIntent();
		if (data == null || data.getStringExtra("path") == null) {
			finish();
			return;
		}
		dataPath = data.getStringExtra("path");
		Log.v("", "dataPath: " + dataPath);
		setContentView(R.layout.edit_photo);
		photoSorter = (PhotoSortView) findViewById(R.id.photoSorter);
		photoSorter.initSingle(this, dataPath);
		titleTV = (TextView) findViewById(R.id.tvTitleText);
		titleTV.setText(getString(R.string.tv_title_single_edit));
		toolGroup = (ViewGroup) findViewById(R.id.toolGroup);
		extendGroup = (ViewGroup) findViewById(R.id.extendGroup);
		Utils.freeMem("PhotoEditActivity on create: ");
		Utils.popWait();

	}

	public void onEditClick(View v) {
		photoSorter.updateNothingSelect();
//		startFeather(photoSorter.getSingleImage());

	}

	public void onEffectClick(View v) {
		photoSorter.updateNothingSelect();
		titleTV.setText(getString(R.string.tveffect));
	}

	public void updateTitle() {
		titleTV.setText(getString(R.string.tv_title_single_edit));
	}

}