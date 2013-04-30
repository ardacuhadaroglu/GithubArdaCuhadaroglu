package com.example.serveractivity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class ShowImage extends Activity{

	ImageView img;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showimage);
		
		Bundle gelenveri = getIntent().getExtras();
		final String path = gelenveri.getString("photo"); 
		
		img = (ImageView) findViewById(R.id.imageView1);
		
		Log.i("ShowImage Path", path);
		
		img.setImageURI(Uri.parse(path));
	}
	

}
