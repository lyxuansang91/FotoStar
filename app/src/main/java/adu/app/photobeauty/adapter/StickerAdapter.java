package adu.app.photobeauty.adapter;

import java.util.ArrayList;

import adu.app.photobeautystar.full.R;

import adu.app.photobeauty.entity.GalleryRootObject;
import adu.app.photobeauty.untils.Constant;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class StickerAdapter extends GalleryRootAdapter {
	private ArrayList<GalleryRootObject> data = new ArrayList<GalleryRootObject>();

	public StickerAdapter(Context c, ImageLoader imageLoader) {
		super(c, imageLoader);

		ICON_W = (int) (Constant.displayWidth / 4);
		ICON_W = ICON_W / 2 + (int) (8 * Constant.density);
		ICON_H = ICON_W;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	public GalleryRootObject getItemAt(int position) {
		return data.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;
		if (convertView == null) {

			convertView = infalter.inflate(R.layout.sticker_item, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.thumbImg.setImageBitmap(null);
		}
		holder.thumbImg.setLayoutParams(new LinearLayout.LayoutParams(ICON_W,
				ICON_H));
		try {

			imageLoader.displayImage(data.get(position).getSdCardPath(),
					holder.thumbImg, Constant.options, animateFirstListener);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	public class ViewHolder {
		ImageView thumbImg;
		TextView folderName;
		TextView numChild;

		public ViewHolder(View v) {
			thumbImg = (ImageView) v.findViewById(R.id.thumbIcon);
		}
	}

	public void clear() {
		data.clear();
		notifyDataSetChanged();
	}

	public void addData(ArrayList<GalleryRootObject> listPhotoFolders) {
		// TODO Auto-generated method stub
		data.addAll(listPhotoFolders);
		notifyDataSetChanged();
	}
}
