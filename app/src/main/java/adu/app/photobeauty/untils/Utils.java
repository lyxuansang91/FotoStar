package adu.app.photobeauty.untils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import adu.app.photobeautystar.full.R;

public class Utils {
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromAssets(Context context,
			String path, int reqWidth, int reqHeight) {
		try {

			AssetManager assetManager = context.getAssets();
			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(assetManager.open(path), null, options);
			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth,
					reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			Bitmap bm = BitmapFactory.decodeStream(assetManager.open(path),
					null, options);
			bm = Bitmap.createScaledBitmap(bm, reqWidth, reqHeight, true);
			Utils.freeMem("decodeSampledBitmapFromResource");
			return bm;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	public static Bitmap decodeSampledBitmapFromFile(Context context,
			String path, int reqWidth, int reqHeight) {
		try {

			// AssetManager assetManager = context.getAssets();
			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth,
					reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			Bitmap bm = BitmapFactory.decodeFile(path, options);
			bm = Bitmap.createScaledBitmap(bm, reqWidth, reqHeight, true);
			Utils.freeMem("decodeSampledBitmapFromResource");
			return bm;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	public static Picture resizePicture(Picture src, int height, int width) {
		Picture newPicture = new Picture();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas = newPicture.beginRecording(width, height);
		canvas.drawPicture(src, new Rect(0, 0, width, height));
		newPicture.endRecording();

		return newPicture;
	}

	public static Bitmap getPatternBitmap(Context context, String patternId) {

		String svgBorderData = getPatternDataFromAsset(context, patternId);
		if (svgBorderData.equals(""))
			return null;
		Bitmap bitmap = Bitmap.createBitmap(Constant.patternSize,
				Constant.patternSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		StringBuilder modStr = new StringBuilder();
		modStr.append("stroke-width=");
		modStr.append('"');
		modStr.append('2');
		modStr.append('"');
		String res = modStr.toString().replace("2",
				String.valueOf(Constant.patternStroke));
		svgBorderData = svgBorderData.replace(modStr.toString(), res);
		SVG svg = SVGParser.getSVGFromString(svgBorderData);

		Picture picture = Utils.resizePicture(svg.getPicture(),
				Constant.patternSize, Constant.patternSize);
		// canvas.drawColor(Constants.patternColor);
		canvas.drawPicture(picture);
		Log.v("", "bitmap w:" + bitmap.getWidth());

		//

		Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas2 = new Canvas(bitmap2);
		canvas2.drawColor(Constant.patternColor);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		paint.setXfermode(new PorterDuffXfermode(
				android.graphics.PorterDuff.Mode.DST_OUT));
		canvas2.drawBitmap(bitmap, 0, 0, paint);
		bitmap.recycle();
		canvas = null;
		paint = null;
		return bitmap2;
	}

	public static void freeMem(String des) {
		Log.v("", des + "  call gc " + Runtime.getRuntime().freeMemory());
		if (Constant.freeMem) {
			System.gc();
		}
	}

	private static ProgressDialog progress;
	private static Activity parentActivity;

	public static void popWait() {
		if (progress == null || parentActivity == null)
			return;
		parentActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					progress.dismiss();
					progress = null;
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

			}
		});

	}

	public static void onWait(Activity parent) {
		parentActivity = parent;
		if (progress == null)
			progress = new ProgressDialog(parentActivity);
		progress.setCanceledOnTouchOutside(true);
		parentActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					progress.setMessage(parentActivity
							.getString(R.string.msg_waiting));
					progress.show();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

			}
		});

	}

	private static Animation transInAni;
	private static Animation transOutAni;

	public static void doAnimation(Context context, final ViewGroup inGroup,
			final ViewGroup outGroup) {
		inGroup.setVisibility(View.VISIBLE);
		outGroup.setVisibility(View.VISIBLE);
		if (transOutAni == null) {
			transOutAni = new TranslateAnimation(
					TranslateAnimation.RELATIVE_TO_SELF,
					TranslateAnimation.RELATIVE_TO_SELF,
					(int) (80 * Constant.density),
					TranslateAnimation.RELATIVE_TO_SELF);
			transOutAni.setDuration(300);
			transOutAni.setFillAfter(true);

		}

		if (transInAni == null) {
			transInAni = new TranslateAnimation(
					TranslateAnimation.RELATIVE_TO_SELF,
					TranslateAnimation.RELATIVE_TO_SELF,
					TranslateAnimation.RELATIVE_TO_SELF,
					(int) (80 * Constant.density));
			transInAni.setDuration(200);
			transInAni.setFillAfter(false);
		}

		transInAni.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				// locking = false;
				inGroup.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				// locking = true;
			}

		});
		outGroup.startAnimation(transOutAni);
		inGroup.startAnimation(transInAni);
	}

	public static String getPatternDataFromAsset(Context context,
			String svgRawResourceId) {
		// TODO Auto-generated method stub
		AssetManager assetManager = context.getAssets();
		InputStream instream = null;
		String res = "";
		try {
			instream = assetManager.open(svgRawResourceId);
			InputStreamReader inputreader = new InputStreamReader(instream);
			BufferedReader buffreader = new BufferedReader(inputreader);
			String line;
			StringBuilder text = new StringBuilder();
			try {
				while ((line = buffreader.readLine()) != null) {
					text.append(line);
					text.append('\n');
				}
			} catch (IOException e) {
				return null;
			}
			res = text.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} finally {
			try {
				instream.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		if (!svgRawResourceId.contains(Constant.PATTERN_EXT))
			return res;
		else
			return decrypt(res);
	}

	public static String readRawTextFile(Context ctx, int resId) {
		InputStream inputStream = ctx.getResources().openRawResource(resId);
		InputStreamReader inputreader = new InputStreamReader(inputStream);
		BufferedReader buffreader = new BufferedReader(inputreader);
		String line;
		StringBuilder text = new StringBuilder();
		try {
			while ((line = buffreader.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}

	public static String[] listMaskFile(Context context, String folderName) {
		AssetManager am = context.getResources().getAssets();
		String[] list = new String[0];
		try {
			list = am.list(folderName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String s : list) {
			// Log.v("", "listMaskFile: " + s);
		}
		return list;
	}

	public static void deleteTempFiles() {
		File dir = new File(Constant.FOLDER_PATH);
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				if (children[i].endsWith(".tmp"))
					new File(dir, children[i]).delete();
			}
		}
	}

	public static File createAppFolder() {
		File baseDir;

		if (android.os.Build.VERSION.SDK_INT < 8) {
			baseDir = Environment.getExternalStorageDirectory();
		} else {
			baseDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		}

		if (baseDir == null) {
			Constant.FOLDER_PATH = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			return Environment.getExternalStorageDirectory();
		}

		File aviaryFolder = new File(baseDir, Constant.FOLDER_NAME);

		if (aviaryFolder.exists()) {
			Constant.FOLDER_PATH = aviaryFolder.getAbsolutePath();
			return aviaryFolder;
		}
		if (aviaryFolder.mkdirs())
			return aviaryFolder;
		Constant.FOLDER_PATH = aviaryFolder.getAbsolutePath();
		return Environment.getExternalStorageDirectory();
	}

	public static void shareIt(Context context, String title, String url) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		// intent.putExtra(Intent.EXTRA_SUBJECT, title);
		intent.putExtra(Intent.EXTRA_TEXT, url);
		Intent chooser = Intent.createChooser(intent, title);
		context.startActivity(chooser);

	}

	public static boolean setWallPaper(Context context, Bitmap bitmap) {
		if (bitmap == null)
			return false;
		WallpaperManager wm = WallpaperManager.getInstance(context);
		try {
			// wm.suggestDesiredDimensions(Constant.displayWidth,
			// Constant.displayWidth);
			wm.setBitmap(bitmap);
			return true;
		} catch (IOException e) {

		} finally {
			bitmap.recycle();
		}
		return false;
	}

	public static void initImageLoader(Context context) {
		try {
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
					context).threadPriority(Thread.NORM_PRIORITY - 2)
					.denyCacheImageMultipleSizesInMemory()
					.discCacheFileNameGenerator(new Md5FileNameGenerator())
					.tasksProcessingOrder(QueueProcessingType.LIFO)
					.writeDebugLogs() // Remove for release app
					.build();
			ImageLoader.getInstance().init(config);

		} catch (Exception e) {

		}
	}

	public static Bitmap reCreateBitmap(Bitmap source) {
		if (source == null)
			return null;
		Bitmap bitmap2 = Bitmap.createBitmap(source.getWidth(),
				source.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas2 = new Canvas(bitmap2);
		canvas2.drawBitmap(source, 0, 0, null);
		source.recycle();
		source = null;
		return bitmap2;
	}

	// public static void encryptAllPattern(Context context) {
	// String[] patternList = listMaskFile(context, "pattern");
	// File file;
	// FileOutputStream out = null;
	// for (int i = 0; i < patternList.length; i++) {
	// String rawData = getPatternDataFromAsset(context, "pattern/"
	// + patternList[i]);
	// file = new File(Constant.FOLDER_PATH + "/pattern/",
	// patternList[i].substring(0, patternList[i].indexOf('.'))
	// + Constant.PATTERN_EXT);
	//
	// try {
	// out = new FileOutputStream(file);
	// out.write(encrypt(rawData));
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }
	//
	// for (int j = 0; j <= 6; j++) {
	// String[] maskList = listMaskFile(context, "mask/" + j + "");
	// for (int i = 0; i < maskList.length; i++) {
	// String rawData = getPatternDataFromAsset(context, "mask/" + j
	// + "/" + maskList[i]);
	// file = new File(Constant.FOLDER_PATH + "/mask/" + j + "/",
	// maskList[i].substring(0, maskList[i].indexOf('.'))
	// + Constant.PATTERN_EXT);
	//
	// try {
	// out = new FileOutputStream(file);
	// out.write(encrypt(rawData));
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }
	// }
	//
	// try {
	// out.close();
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	private static byte[] encrypt(String str) {
		byte[] b = Base64.encode(str.getBytes(), Base64.DEFAULT);
		reverse(b);
		return b;
	}

	private static void reverse(byte[] array) {
		if (array == null) {
			return;
		}
		int i = 0;
		int j = array.length - 1;
		byte tmp;
		while (j > i) {
			tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;
			j--;
			i++;
		}
	}

	private static String decrypt(String str) {
		byte[] dec = str.getBytes();
		reverse(dec);
		return new String(Base64.decode(dec, Base64.DEFAULT));

	}

	public static void setLocale(Context context) {
		String lang = Locale.getDefault().getLanguage();
		if (getValueByKey(context, Constant.LANGUAGE_PREF_KEY).equals(
				Constant.LANGUAGE_EN_VAL))
			lang = "en";
		Log.v("", "setLocale: " + lang);
		Locale myLocale = new Locale(lang);
		Resources res = context.getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
	}

	public static boolean isSupportCurrentLang(Context context) {
		// TODO Auto-generated method stub
		String lang = Locale.getDefault().getLanguage();
		Locale locales[] = Locale.getAvailableLocales();
		for (Locale str : locales) {
			Log.v("", "str: " + str + " ,lang: " + lang);
			if (str.getCountry().equals(lang))
				return true;
		}
		return false;
	}

	private static SharedPreferences mSharedPreferences;
	private static SharedPreferences.Editor mEditor;

	public static SharedPreferences getSharedPreferences(Context context) {
		if (mSharedPreferences != null)
			return mSharedPreferences;
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static SharedPreferences.Editor getSharedPreferencesEditor(
			Context context) {
		if (mEditor != null)
			return mEditor;
		return getSharedPreferences(context).edit();
	}

	// for extends key
	public static String getValueByKey(Context context, String key) {
		SharedPreferences preferences = getSharedPreferences(context);
		return preferences.getString(key, "");
	}

	public static void setValueByKey(String value, Context context, String key) {
		SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
		editor.putString(key, value);
		editor.commit();

	}

	public static String getRealAppVer(Context context) {
		String currAppVer = "";
		try {
			currAppVer = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return currAppVer;
	}
}
