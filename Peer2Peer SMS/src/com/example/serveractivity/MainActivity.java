package com.example.serveractivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Button serverbtn = (Button) findViewById(R.id.button1);
		Button clientbtn = (Button) findViewById(R.id.button2);
		Button serverSifrelibtn = (Button) findViewById(R.id.button3);
		Button clienSifrelitbtn = (Button) findViewById(R.id.Button01);
		
		serverbtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(MainActivity.this,ServerActivity.class));
			}
		});
		
		clientbtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(MainActivity.this,ClientActivity.class));
			}
		});
		
		serverSifrelibtn.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						// TODO Auto-generated method stub
						startActivity(new Intent(MainActivity.this,ServerActivitySifreli.class));
					}
				});
		
		clienSifrelitbtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(MainActivity.this,ClientActivitySifreli.class));
			}
		});
		
	}

	
}