package adu.app.photobeauty.adapter;

import java.util.ArrayList;
import java.util.Random;

import adu.app.photobeauty.activity.TextMakerActivity;
import adu.app.photobeauty.untils.Constant;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FontAdapter extends BaseAdapter {
	private ArrayList<String> data = new ArrayList<String>();
	private Context context;
	private int height;
	private int width;
	final static int[] color = new int[] { Color.RED, Color.GREEN, Color.BLUE,
			Color.YELLOW, Color.MAGENTA, Color.BLACK, 0xff49691c, Color.CYAN,
			0xffff9102, 0xff74ff03, 0xff036eff, 0xffa903ff };

	public FontAdapter(Context c) {
		super();
		width = Constant.displayWidth / 4;
		height = width / 2;
		this.context = c;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	public String getItem(int position) {
		return data.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		TextView view = new TextView(context);
		view.setLayoutParams(new AbsListView.LayoutParams(width, height));
		view.setGravity(Gravity.CENTER);
		view.setTypeface(TextMakerActivity.fontList.get(position),
				Typeface.BOLD);
		view.setText("A B C");
		view.setTextSize(20);
		int index = position;
		if (index >= color.length)
			index = index % color.length;
		view.setTextColor(color[index]);
		return view;
	}

	public void clear() {
		data.clear();
		notifyDataSetChanged();
	}

	public void setData(ArrayList<String> list) {
		// TODO Auto-generated method stub
		data.clear();
		data.addAll(list);
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int i) {
		// TODO Auto-generated method stub
		return 0;
	}
}
