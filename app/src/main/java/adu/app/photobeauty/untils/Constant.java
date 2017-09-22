package adu.app.photobeauty.untils;

import adu.app.photobeauty.activity.SplashActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class Constant {
	public static final int ACTION_REQUEST_PICK_GALLERY_CHANGE = 100;
	public static final int ACTION_REQUEST_PICK_CAMERA_CHANGE = 101;
	public static final int ACTION_REQUEST_PICK_GALLERY_MASK = 102;
	public static final int ACTION_REQUEST_PICK_CAMERA_MASK = 103;
	public static final int ACTION_REQUEST_FEATHER = 104;
	public static final int ACTION_REQUEST_MASK = 105;
	public static final int ACTION_REQUEST_TEXT = 106;
	public static final int ACTION_REQUEST_MASK_STICKER = 107;
	public static int strokeSize = 2;// dp
	public static int strokeColor = Color.WHITE;
	public static int displayWidth, displayHeight;
	public static float density;
	public static int maskSize;
	public static int patternSize = 35;// dp
	public static int patternColor = 0xFF106a3d;
	public static int patternStroke = 3;
	// public static final int patternBorderColor = 0xff014724;
	public static final int patternBorderColor = 0xFFe3eff2;
	public static final int patternBorderColorSelected = 0xff00b1fd;
	public static final int patternBorderSize = 2;
	public static int maskMargin = 20;// dp
	public static int debugColor = Color.YELLOW;
	public final static int maxPick = 9;
	public final static int minPick = 2;
	public static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG
			| Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
			| Canvas.FULL_COLOR_LAYER_SAVE_FLAG
			| Canvas.CLIP_TO_LAYER_SAVE_FLAG;
	public static final String API_SECRET = "d5707beb9c91f40d";
	public static final boolean freeMem = true;
	public static final String FOLDER_NAME = "photostar";
	public static String FOLDER_PATH = "";
	public static final float SCREEN_MARGIN = 30;
	public static final int ACTIVITY_CODE_RESULT_MASK = 99;
	// border cropoverlay

	public static int cropOverLayStroke = 3;// dp
	public static int cropOverLayColor = Color.WHITE;
	public static int cropOverLayStrokeSelect = 4;// dp
	public final static int cropOverLayColorSelect = 0xffff4e00;
	public static final int PICK_COLOR_ACTION_BG = 1000;
	public static final int PICK_COLOR_ACTION_BORDER = 1001;
	public static final int ACTION_CHANGE = 0;
	public static final int ACTION_MASK = 1;
	public static final long deltaBackPress = 3000;
	public static final int MAX_BORDER_SIZE_FREE = 15;
	public static final int MAX_BORDER_SIZE_LAYOUT = 10;
	public static String APP_LINK = "https://play.google.com/store/apps/details?id=";
	public static String APP_LINK_MARKET = "market://details?id=";
	public static boolean mAlive;
	public final static String PATTERN_EXT = ".bin";

	public static int getResId(Context ctx, String varName, String resType) {
		return ctx.getResources().getIdentifier(varName, resType,
				ctx.getPackageName());

	}

	public static boolean checkAlive(Activity context) {
		Log.v("", "checkAlive: " + mAlive);
		if (!mAlive) {
			Intent intent = new Intent(context, SplashActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
			context.finish();
			return false;
		}
		return true;
	}

	public static void init(Context context) {
		// TODO Auto-generated method stub
		Resources resources = context.getResources();
		if (density > 0)
			return;
		DisplayMetrics metrics = resources.getDisplayMetrics();
		Constant.displayWidth = resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math
				.max(metrics.widthPixels, metrics.heightPixels) : Math.min(
				metrics.widthPixels, metrics.heightPixels);
		Constant.displayHeight = resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math
				.min(metrics.widthPixels, metrics.heightPixels) : Math.max(
				metrics.widthPixels, metrics.heightPixels);
		Constant.density = metrics.density;
		Constant.maskMargin = (int) (Constant.maskMargin * Constant.density);
		Constant.maskSize = Constant.displayWidth - 2 * Constant.maskMargin;
		Constant.patternSize = (int) (density * Constant.patternSize);
		Constant.patternStroke = (int) (density * Constant.patternStroke);
		Constant.handW = (int) (density * Constant.handW);
		Constant.cropOverLayStroke = (int) (density * Constant.cropOverLayStroke);
		Constant.cropOverLayStrokeSelect = (int) (density * Constant.cropOverLayStrokeSelect);
		APP_LINK = APP_LINK + context.getPackageName();
		APP_LINK_MARKET = APP_LINK_MARKET + context.getPackageName();
	}

	public static DisplayImageOptions options = new DisplayImageOptions.Builder()
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
			.bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true)
			.cacheOnDisc(true).considerExifParams(true)
			.displayer(new RoundedBitmapDisplayer(0)).build();
	public static String mApiKey;
	public static int handW = 60;// dp
	public final static String LANGUAGE_PREF_KEY = "LANGUAGE_PREF_KEY";
	public final static String LANGUAGE_AUTO_VAL = "LANGUAGE_AUTO_VAL";
	public final static String LANGUAGE_EN_VAL = "LANGUAGE_EN_VAL";
	public final static String RATE_PREF_KEY = "RATE_PREF_KEY";
}
