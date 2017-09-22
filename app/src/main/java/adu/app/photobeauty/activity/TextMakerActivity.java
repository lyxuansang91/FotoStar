/**
 * PhotoSorterActivity.java
 * 
 * (c) Luke Hutchison (luke.hutch@mit.edu)
 * 
 * Released under the Apache License v2.
 */
package adu.app.photobeauty.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import adu.app.photobeauty.adapter.FontAdapter;
import adu.app.photobeauty.untils.Constant;
import adu.app.photobeauty.untils.Utils;
import adu.app.photobeauty.view.AmbilWarnaDialog;
import adu.app.photobeauty.view.AmbilWarnaDialog.OnAmbilWarnaListener;
import adu.app.photobeauty.view.PhotoSortView;
import adu.app.photobeautystar.full.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class TextMakerActivity extends BaseActivity implements OnAmbilWarnaListener {

	PhotoSortView photoSorter;
	LinearLayout captureLayout;
	private final static String TAG = "PhotoSortrActivity";
	private TextView titleTV;
	private TextView fontTV;
	private EditText fontEditText;
	private GridView gridFont;
	private String fontListPath[];
	private TextWatcher inputTextWatcher;
	private boolean mBold;
	private boolean mItalic;
	private boolean mUnderLine;
	private ViewGroup colorBtn;
	private Button boldBtn;
	private Button italicBtn;
	private Button underlineBtn;
	private Typeface mTypeface;
	private int mColor = Color.MAGENTA;
	private int mSize = 28;
	private Spinner sizeSpinner;
	private View colorView;
	private final static int[] sizeValues = new int[] { 8, 9, 10, 11, 12, 14,
			16, 18, 20, 22, 2, 26, 28, 36, 48, 72 };
	private ArrayList<String> sizeStringValues = new ArrayList<String>();
	public static ArrayList<Typeface> fontList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (!Constant.checkAlive(this))
			return;
		setContentView(R.layout.text_maker);
		titleTV = (TextView) findViewById(R.id.tvTitleText);
		titleTV.setText(getString(R.string.tv_title_text_maker));
		fontTV = (TextView) findViewById(R.id.fontTV);
		fontEditText = (EditText) findViewById(R.id.fontEditText);
		gridFont = (GridView) findViewById(R.id.gridFont);
		colorBtn = (ViewGroup) findViewById(R.id.colorButton);
		boldBtn = (Button) findViewById(R.id.boldBtn);
		italicBtn = (Button) findViewById(R.id.italicBtn);
		underlineBtn = (Button) findViewById(R.id.underlineBtn);
		colorView = (View) findViewById(R.id.fontColorView);
		colorView.setBackgroundColor(mColor);
		// mTypeface = Typeface.createFromAsset(getAssets(),
		// "font/Ziggy Zoe.ttf");

		sizeSpinner = (Spinner) findViewById(R.id.sizeList);
		for (int i = 0; i < sizeValues.length; i++) {
			sizeStringValues.add(String.valueOf(sizeValues[i]));
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				R.layout.list_size, sizeStringValues);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sizeSpinner.setAdapter(dataAdapter);
		sizeSpinner
				.setSelection(sizeStringValues.indexOf(String.valueOf(mSize)));
		sizeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapterview, View view,
					int i, long l) {
				// TODO Auto-generated method stub
				mSize = sizeValues[i];
				Log.v("", "mSize: " + mSize);
				updateText();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterview) {
				// TODO Auto-generated method stub

			}
		});

		// fontTV.settex
		inputTextWatcher = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				updateText();
			}
		};
		fontEditText.addTextChangedListener(inputTextWatcher);
		fontListPath = Utils.listMaskFile(this, "font");
		initFontList();
		Utils.popWait();
		// Config.maybeShowClickAdWall(this);
	}

	FontAdapter adapter;

	private void initFontList() {
		if (adapter != null)
			return;
		// TODO Auto-generated method stub
		fontList = new ArrayList<Typeface>();
		ArrayList<String> data = new ArrayList<String>();
		for (String str : fontListPath) {
			String res = "font/" + str;
			data.add(res);
			fontList.add(Typeface.createFromAsset(getAssets(), res));
		}
		mTypeface = fontList.get(0);
		adapter = new FontAdapter(this);
		adapter.setData(data);
		gridFont.setAdapter(adapter);
		gridFont.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterview, View view,
					int i, long l) {
				// TODO Auto-generated method stub
				mTypeface = fontList.get(i);
				updateText();
			}
		});
	}

	public void onResume() {
		super.onResume();
		// Config.onResumeService(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// Config.onPauseService(this);

	}

	public void updateText() {
		String str = "\u00A0" + fontEditText.getText().toString() + "\u00A0";
		fontTV.setTextSize(Constant.density * mSize);
		fontTV.setTextColor(mColor);
		if (mBold)
			fontTV.setTypeface(mTypeface, Typeface.BOLD);
		if (mItalic)
			fontTV.setTypeface(mTypeface, Typeface.ITALIC);
		if (mBold && mItalic)
			fontTV.setTypeface(mTypeface, Typeface.BOLD_ITALIC);
		if (!mBold && !mItalic)
			fontTV.setTypeface(mTypeface, Typeface.NORMAL);
		if (mUnderLine) {
			SpannableString mSpannableString = new SpannableString(str);
			mSpannableString.setSpan(new UnderlineSpan(), 0, str.length(), 0);
			str = mSpannableString.toString();
			fontTV.setText(mSpannableString);
			return;
		}
		if (mItalic)
			str = "\u00A0" + fontEditText.getText().toString() + "\u00A0";
		fontTV.setText(str);
	}

	public void onBoldBtn(View v) {
		mBold = !mBold;
		if (mBold)
			boldBtn.setBackgroundResource(R.drawable.ic_b_focus);
		else
			boldBtn.setBackgroundResource(R.drawable.ic_b);
		updateText();

	}

	public void onClearBtn(View v) {
		fontEditText.setText("");

	}

	public void onColorBtn(View v) {
		new AmbilWarnaDialog(this, mColor, this, -1).show();

	}

	public void onItalicBtn(View v) {
		mItalic = !mItalic;
		if (mItalic)
			italicBtn.setBackgroundResource(R.drawable.ic_i_focus);
		else
			italicBtn.setBackgroundResource(R.drawable.ic_i);
		updateText();

	}

	public void onUnderLineBtn(View v) {
		mUnderLine = !mUnderLine;
		if (mUnderLine)
			underlineBtn.setBackgroundResource(R.drawable.ic_u_focus);
		else
			underlineBtn.setBackgroundResource(R.drawable.ic_u);
		updateText();

	}

	public String onCapture() {
		int w = fontTV.getMeasuredWidth();
		int h = fontTV.getMeasuredHeight();

		// float scaleX = (float) w / (float) fontTV.getMeasuredWidth();
		// float scaleY = (float) h / (float) fontTV.getMeasuredHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h,
				android.graphics.Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		// canvas.scale(1, 1);
		// fontTV.setScaleX(5);
		// fontTV.setScaleY(5);
		fontTV.draw(canvas);
		File target = new File(Constant.FOLDER_PATH, "text_"
				+ System.currentTimeMillis() + ".tmp");
		Log.d(TAG, "Target path: " + target.getPath());
		try {
			FileOutputStream out = new FileOutputStream(target);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			// Toast.makeText(this, "Image captured!",
			// Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
			// Toast.makeText(this, "Unable to capture screen",
			// Toast.LENGTH_LONG)
			// .show();
			Log.e(TAG, "Unable to capture image", e);
		} catch (IOException e) {
			// Toast.makeText(this, "Unable to capture screen",
			// Toast.LENGTH_LONG)
			// .show();
			Log.e(TAG, "Unable to capture image", e);
		}
		bitmap.recycle();
		bitmap = null;
		return target.getAbsolutePath();
	}

	@Override
	public void onCancel(AmbilWarnaDialog dialog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOk(AmbilWarnaDialog dialog, int color, int action) {
		// TODO Auto-generated method stub
		mColor = color;
		colorView.setBackgroundColor(mColor);
		updateText();
	}

	public void onCheckBtn(View v) {

		String res = onCapture();
		Intent intent = getIntent();
		intent.putExtra("new_path", res);
		setResult(RESULT_OK, intent);
		clearRes();

		finish();
	}

	public void onBackBtn(View v) {
		onBackPressed();
	}

	public void onBackPressed() {

		clearRes();
		finish();
	}

	private void clearRes() {
		// TODO Auto-generated method stub
		if (fontList != null)
			fontList.clear();
		fontList = null;
		if (adapter != null)
			adapter.clear();
		adapter = null;
		Utils.freeMem("textmaker finish: ");
	}
}