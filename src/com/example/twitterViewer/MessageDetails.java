package com.example.twitterViewer;

import android.graphics.Bitmap;

public class MessageDetails {
	int color;
	Bitmap icon;
	String from;
	String sub;
	String desc;
	String time;
	

	public String getName() {
		return from;
	}

	public void setName(String from) {
		this.from = from;
	}


	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
	
	public int getColor(){
		return color;
	}
	
	public void setColor(int color){
		this.color = color;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}