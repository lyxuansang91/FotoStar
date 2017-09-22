package adu.app.photobeauty.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class AnimateFirstDisplayListener extends

SimpleImageLoadingListener {

	static final List<String> displayedImages = Collections
			.synchronizedList(new LinkedList<String>());

	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		Log.v("", "onLoadingComplete: " + (loadedImage == null));
		if (loadedImage != null) {
			ImageView imageView = (ImageView) view;
			boolean firstDisplay = !displayedImages.contains(imageUri);
			if (firstDisplay) {
				FadeInBitmapDisplayer.animate(imageView, 500);
				displayedImages.add(imageUri);
			} else
				imageView.setImageBitmap(loadedImage);
			// imageView
			// .setBackgroundDrawable(new BitmapDrawable(loadedImage));
		}
	}

}
