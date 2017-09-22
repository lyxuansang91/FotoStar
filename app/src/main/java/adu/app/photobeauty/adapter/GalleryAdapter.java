package adu.app.photobeauty.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import adu.app.photobeautystar.full.R;

import adu.app.photobeauty.entity.GalleryObject;
import adu.app.photobeauty.untils.Constant;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class GalleryAdapter extends BaseAdapter {

	private Context mContext;
	public LayoutInflater infalter;
	private ArrayList<GalleryObject> data = new ArrayList<GalleryObject>();
	ImageLoader imageLoader;

	protected ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	protected int ICON_W;
	protected int ICON_H;

	public GalleryAdapter(Context c, ImageLoader loader) {
		infalter = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = c;

		ICON_W = (int) (Constant.displayWidth / 3 - 2 * 2 * Constant.density);
		ICON_H = ICON_W;
		imageLoader = loader;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public GalleryObject getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void selectAll(boolean selection) {
		for (int i = 0; i < data.size(); i++) {
			data.get(i).isSeleted = selection;

		}
		notifyDataSetChanged();
	}

	public boolean isAllSelected() {
		boolean isAllSelected = true;

		for (int i = 0; i < data.size(); i++) {
			if (!data.get(i).isSeleted) {
				isAllSelected = false;
				break;
			}
		}

		return isAllSelected;
	}

	public boolean isAnySelected() {
		boolean isAnySelected = false;

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).isSeleted) {
				isAnySelected = true;
				break;
			}
		}

		return isAnySelected;
	}

	public ArrayList<GalleryObject> getSelected() {
		ArrayList<GalleryObject> dataT = new ArrayList<GalleryObject>();

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).isSeleted) {
				dataT.add(data.get(i));
			}
		}

		return dataT;
	}

	public void addAll(ArrayList<GalleryObject> files) {

		try {
			this.data.clear();
			this.data.addAll(files);

		} catch (Exception e) {
			e.printStackTrace();
		}

		notifyDataSetChanged();
	}

	public void clearData() {
		data.clear();
		notifyDataSetChanged();
	}

	public void changeSelection(GalleryObject item) {
		int index = -1;
		for (int i = 0; i < this.data.size(); i++) {
			if (item.sdcardPath.equals(this.data.get(i).sdcardPath)) {
				index = i;
				break;
			}
		}
		if (index > -1) {
			if (data.get(index).isSeleted) {
				data.get(index).isSeleted = false;
			} else {
				data.get(index).isSeleted = true;
			}
			notifyDataSetChanged();
		}
	}

	public boolean changeSelection(int position) {
		boolean res;
		if (data.get(position).isSeleted) {
			data.get(position).isSeleted = false;
			res = false;
		} else {
			data.get(position).isSeleted = true;
			res = true;
		}

		return res;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;
		if (convertView == null) {

			convertView = infalter.inflate(R.layout.gallery_item, null);
			holder = new ViewHolder();
			holder.imgQueue = (ImageView) convertView
					.findViewById(R.id.imgQueue);

			holder.imgQueueMultiSelected = (ImageView) convertView
					.findViewById(R.id.imgQueueMultiSelected);

			holder.imgQueueMultiSelected.setVisibility(View.VISIBLE);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.imgQueue.setImageBitmap(null);
			holder.imgQueueMultiSelected.setSelected(false);
		}
		holder.imgQueue.setTag(position);
		holder.imgQueue.setLayoutParams(new FrameLayout.LayoutParams(ICON_W,
				ICON_H));
		try {

			imageLoader.displayImage("file://" + data.get(position).sdcardPath,
					holder.imgQueue, Constant.options, animateFirstListener);

			holder.imgQueueMultiSelected
					.setSelected(data.get(position).isSeleted);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	public class ViewHolder {
		ImageView imgQueue;
		ImageView imgQueueMultiSelected;
	}

	public void clearCache() {
		imageLoader.clearDiscCache();
		imageLoader.clearMemoryCache();
	}

	public void clear() {
		data.clear();
		notifyDataSetChanged();
	}

}
