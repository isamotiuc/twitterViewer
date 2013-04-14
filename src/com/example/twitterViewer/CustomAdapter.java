package com.example.twitterViewer;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends BaseAdapter {

	public Dialog progressDialog;
	
	private ArrayList<MessageDetails> _data;
	Context _c;

	CustomAdapter(ArrayList<MessageDetails> data, Context c) {
		_data = data;
		_c = c;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return _data.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return _data.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) _c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.list_item_message, null);
		}
		
		

		ImageView image = (ImageView) v.findViewById(R.id.icon);
		TextView fromView = (TextView) v.findViewById(R.id.From);
		TextView descView = (TextView) v.findViewById(R.id.description);
		TextView timeView = (TextView) v.findViewById(R.id.time);
//		TextView subView = (TextView) v.findViewById(R.id.subject);

		MessageDetails msg = _data.get(position);
		
		fromView.setText(msg.from);
		descView.setText(msg.desc);
		timeView.setText(msg.time);
		v.setBackgroundColor(msg.color);
		image.setImageBitmap(msg.icon);
//		subView.setText("Subject: " + msg.sub);
//		image.setImageResource(msg.icon);

		return v;
	}
	
	
	
}