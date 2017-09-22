package adu.app.photobeauty.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import adu.app.photobeauty.adapter.GalleryAdapter;
import adu.app.photobeauty.adapter.GalleryRootAdapter;
import adu.app.photobeauty.entity.GalleryObject;
import adu.app.photobeauty.entity.GalleryRootObject;
import adu.app.photobeauty.untils.Constant;
import adu.app.photobeauty.untils.Utils;
import adu.app.photobeautystar.full.R;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

public class GalleryActivity extends BaseActivity {

	// private static final int MAX_PICK = 8;
	GridView gridGallery;
	Handler handler;
	GalleryAdapter adapter;
	GalleryRootAdapter rootAdapter;
	ImageView imgNoMedia;
	Button btnGalleryOk;

	String action;
	private boolean mIsOnRootScreen = true;
	private ImageLoader imageLoader;
	private ViewGroup pickGroup;
	private ArrayList<GalleryObject> pickedList = new ArrayList<GalleryObject>();
	private HorizontalScrollView pickScroll;
	private TextView titleTV;
	private TextView pickedTV;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Constant.init(getResources());
		if (!Constant.checkAlive(this))
			return;
		setContentView(R.layout.gallery);
		gridGallery = (GridView) findViewById(R.id.gridGallery);
		gridGallery.setFastScrollEnabled(false);
		// PauseOnScrollListener listener = new
		// PauseOnScrollListener(imageLoader,
		// true, true);
		// gridGallery.setOnScrollListener(listener);
		imgNoMedia = (ImageView) findViewById(R.id.imgNoMedia);
		titleTV = (TextView) findViewById(R.id.tvTitleText);
		pickedTV = (TextView) findViewById(R.id.pickedTxt);
		imgNoMedia.setVisibility(View.GONE);
		handler = new Handler();
		initImageLoader();
		initPickedList();
		updatePickedTV();
		// Config.maybeShowClickAdWall(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		loadAd();
		// Config.onPauseService(this);
		// photoSorter.unloadImages();
	}

	public void onResume() {
		super.onResume();
		// Config.onPauseService(this);
		if (mIsOnRootScreen)
			initRootAdapter();
		// Config.showAdBanner(this, (ViewGroup) findViewById(R.id.adGroup));
		// Config.showAdBanner(this, true);
	}

	public void updatePickedTV() {
		int picked = pickedList.size();
		String msg = "Picked: " + picked + " (Max: " + Constant.maxPick
				+ ", Min: " + Constant.minPick + ")";
		try {
			msg = String.format(getString(R.string.tv_picked), picked,
					Constant.maxPick, Constant.minPick);
		} catch (Exception e) {
			// TODO: handle exception
		}
		pickedTV.setText(msg);
		if (picked == 0)
			pickGroup.setVisibility(View.GONE);
		else
			pickGroup.setVisibility(View.VISIBLE);
	}

	public void initPickedList() {
		inflater = ((LayoutInflater) this.getSystemService("layout_inflater"));
		H = (int) (Constant.density * 80);
		W = H + (int) (Constant.density * 3);
		pickScroll = (HorizontalScrollView) findViewById(R.id.pickScroll);
		if (pickGroup == null)
			pickGroup = (ViewGroup) findViewById(R.id.pickGroup);
		pickGroup.removeAllViews();
	}

	private LayoutInflater inflater;
	private View localView;
	int W, H;

	public void addPickView(final GalleryObject item) {
		localView = inflater.inflate(R.layout.picked_item, null);
		ImageView itemImage = (ImageView) localView
				.findViewById(R.id.item_image);
		imageLoader.displayImage("file://" + item.sdcardPath, itemImage);
		itemImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.v("", "click me: " + item.sdcardPath);
				unPickPhoto(item);
				adapter.changeSelection(item);
			}
		});

		pickGroup.addView(localView, new LayoutParams(W, H));
		// pickScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
		handler.postDelayed(new Runnable() {
			public void run() {
				pickScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		}, 100L);
	}

	public boolean onPickPhoto(GalleryObject data) {
		if (pickedList.size() >= Constant.maxPick) {
			String msg = "The maximum is " + Constant.maxPick + " photos";

			try {
				msg = String.format(getString(R.string.msg_max_pick),
						Constant.maxPick);
			} catch (Exception e) {
				// TODO: handle exception
			}
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

			return false;

		}
		pickedList.add(data);
		addPickView(data);
		updatePickedTV();
		return true;
	}

	public void unPickPhoto(GalleryObject data) {
		int index = findPickPhotoIndex(data);
		if (index > -1) {
			pickedList.remove(index);
			pickGroup.removeViewAt(index);
			pickGroup.invalidate();
		}
		updatePickedTV();
	}

	public int findPickPhotoIndex(GalleryObject data) {
		for (int i = 0; i < pickedList.size(); i++) {
			if (data.sdcardPath.equals(pickedList.get(i).sdcardPath))
				return i;
		}
		return -1;
	}

	private void initImageLoader() {
		imageLoader = ImageLoader.getInstance();
	}

	private void initRootAdapter() {

		mIsOnRootScreen = true;
		titleTV.setText(getString(R.string.tv_title_gallery));
		// if (rootAdapter == null) {
		rootAdapter = new GalleryRootAdapter(getApplicationContext(),
				imageLoader);
		new Thread() {

			@Override
			public void run() {
				Looper.prepare();
				handler.post(new Runnable() {

					@Override
					public void run() {
						rootAdapter.addData(listPhotoFolders());

					}
				});
				Looper.loop();
			};

		}.start();
		// }

		gridGallery.setAdapter(rootAdapter);
		gridGallery.setOnItemClickListener(mItemRootClickListener);
		// rootAdapter.notifyDataSetChanged();
		// gridGallery.invalidate();

	}

	public void onSelectRootFolder(GalleryRootObject data) {
		mIsOnRootScreen = false;
		adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
		rootAdapter = new GalleryRootAdapter(getApplicationContext(),
				imageLoader);
		gridGallery.setAdapter(adapter);
		gridGallery.setOnItemClickListener(mItemMulClickListener);
		final String folderName = data.getSdCardPath();
		titleTV.setText(folderName);
		new Thread() {

			@Override
			public void run() {
				Looper.prepare();
				handler.post(new Runnable() {

					@Override
					public void run() {
						adapter.addAll(getPhotosFromFolder(folderName));
						checkImageStatus();
					}
				});
				Looper.loop();
			};

		}.start();

	}

	private void checkImageStatus() {
		// if (adapter.isEmpty()) {
		// imgNoMedia.setVisibility(View.VISIBLE);
		// } else {
		// imgNoMedia.setVisibility(View.GONE);
		// }
	}

	View.OnClickListener mOkClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			ArrayList<GalleryObject> selected = adapter.getSelected();

			String[] allPath = new String[selected.size()];
			for (int i = 0; i < allPath.length; i++) {
				allPath[i] = selected.get(i).sdcardPath;
			}

			Intent data = new Intent().putExtra("all_path", allPath);
			setResult(RESULT_OK, data);
			finish();

		}
	};
	AdapterView.OnItemClickListener mItemMulClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			boolean res = adapter.changeSelection(position);
			GalleryObject data = adapter.getItem(position);
			// data.position = position;
			if (res) {
				if (!onPickPhoto(data))
					data.isSeleted = false;
			} else {
				unPickPhoto(data);
			}
			adapter.notifyDataSetChanged();
		}
	};

	AdapterView.OnItemClickListener mItemRootClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			GalleryRootObject obj = rootAdapter.getItemAt(position);
			onSelectRootFolder(obj);
		}
	};

	public ArrayList<GalleryRootObject> listPhotoFolders() {
		ArrayList<GalleryRootObject> res = new ArrayList<GalleryRootObject>();
		HashMap<String, GalleryRootObject> hash = new HashMap<String, GalleryRootObject>();
		// which image properties are we querying
		String[] projection = new String[] { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
				MediaStore.Images.Media.DATA };

		// Get the base URI for the People table in the Contacts content
		// provider.
		Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		// Make the query.
		Cursor cur = managedQuery(images, projection, // Which columns to return
				"", // Which rows to return (all rows)
				null, // Selection arguments (none)
				MediaStore.Images.Media._ID + " desc" // Ordering
		);

		Log.v("ListingImages", " query count=" + cur.getCount());

		if (cur.moveToFirst()) {
			String bucket;
			String data;
			int bucketColumn = cur
					.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

			int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);
			int i = 0;
			do {
				// Get the field values
				bucket = cur.getString(bucketColumn);
				data = cur.getString(dataColumn);
				GalleryRootObject object = hash.get(bucket);
				// Log.v("", " query count=" +bucket);
				if (object == null) {
					object = new GalleryRootObject();
					object.setSdCardPath(bucket);
					object.setThumbPath(data);
					object.setNumChild(1);
					object.setOrder(i);
					hash.put(bucket, object);
					i++;
				} else {
					object.setNumChild(object.getNumChild() + 1);
				}

			} while (cur.moveToNext());
		}
		res.addAll(hash.values());

		Collections.sort(res, comparator);
		return res;

	}

	Comparator<GalleryRootObject> comparator = new Comparator<GalleryRootObject>() {
		public int compare(GalleryRootObject o1, GalleryRootObject o2) {
			if (o1.getOrder() > o2.getOrder())
				return 1;
			else if (o1.getOrder() < o2.getOrder())
				return -1;
			else
				return 0;
		}
	};

	private ArrayList<GalleryObject> getPhotosFromFolder(String folderName) {
		ArrayList<GalleryObject> galleryList = new ArrayList<GalleryObject>();

		try {
			String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
					+ " = ?";
			String[] selectionArgs = new String[] { folderName };
			final String[] columns = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media._ID };
			final String orderBy = MediaStore.Images.Media._ID + " desc";
			Cursor imagecursor = getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
					selection, selectionArgs, orderBy);

			if (imagecursor != null && imagecursor.getCount() > 0) {

				while (imagecursor.moveToNext()) {
					GalleryObject item = new GalleryObject();

					int dataColumnIndex = imagecursor
							.getColumnIndex(MediaStore.Images.Media.DATA);

					item.sdcardPath = imagecursor.getString(dataColumnIndex);
					if (findPickPhotoIndex(item) > -1)
						item.isSeleted = true;
					galleryList.add(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// show newest photo at beginning of the list
		// Collections.reverse(galleryList);
		return galleryList;
	}

	public void onBackPressed() {
		if (!mIsOnRootScreen)
			initRootAdapter();
		else {
			imageLoader.clearDiscCache();
			if (pickGroup != null)
				pickGroup.removeAllViews();
			if (adapter != null)
				adapter.clearData();
			if (rootAdapter != null)
				rootAdapter.clearData();
			pickedList.clear();
			new Thread() {
				public void run() {
					imageLoader.clearMemoryCache();
					imageLoader.clearDiscCache();
				}
			}.start();
			// Config.maybeShowAdWall(this);
			super.onBackPressed();
		}
	}

	public void onCheckBtn(View v) {
		if (pickedList.size() < Constant.minPick) {
			String msg = "You must choose at least " + Constant.minPick
					+ " photos";

			try {
				msg = String.format(getString(R.string.msg_min_pick),
						Constant.minPick);
			} catch (Exception e) {
				// TODO: handle exception
			}
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			return;
		}
		String[] allPath = new String[pickedList.size()];
		for (int i = 0; i < allPath.length; i++) {
			allPath[i] = pickedList.get(i).sdcardPath;
		}
		imageLoader.clearMemoryCache();
		Utils.onWait(this);
		Intent intent = new Intent(this, PhotoCollageActivity.class);
		intent.putExtra("all_path", allPath);
		startActivity(intent);
	}

	public void onBackBtn(View v) {
		onBackPressed();
	}
}
