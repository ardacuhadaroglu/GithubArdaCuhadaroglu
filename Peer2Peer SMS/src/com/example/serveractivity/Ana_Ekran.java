package com.example.serveractivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Ana_Ekran extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.anaekran);
		
		Button btnresim = (Button) findViewById(R.id.buttonResim);
		Button btnmesaj = (Button) findViewById(R.id.buttonMesaj);
		Button exitbtn = (Button) findViewById(R.id.button1);
		
		btnresim.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(Ana_Ekran.this, MainActivityResim.class));
			}
		});
		btnmesaj.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(Ana_Ekran.this, MainActivity.class));
				
			}
		});
		
		exitbtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
	}

	
}
