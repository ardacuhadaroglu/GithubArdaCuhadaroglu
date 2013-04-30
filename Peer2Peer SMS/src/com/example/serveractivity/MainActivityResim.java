package com.example.serveractivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivityResim extends Activity {

	Button serverBtn;
	Button clientBtn;
	Button guvenliServerBtn;
	Button guvenliClientBtn;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_resim);
        
        serverBtn = (Button) findViewById(R.id.btnServer);
        clientBtn = (Button) findViewById(R.id.btnClient);
        guvenliServerBtn = (Button) findViewById(R.id.Button02);
        guvenliClientBtn = (Button) findViewById(R.id.Button01);
        
        serverBtn.setOnClickListener(new View.OnClickListener()
        {
			
			public void onClick(View v) 
			{
				startActivity(new Intent(MainActivityResim.this, Server.class));
			}
		});
        
        clientBtn.setOnClickListener(new View.OnClickListener()
        {
			
			public void onClick(View v)
			{
				startActivity(new Intent(MainActivityResim.this, Client.class));	
			}
		});
        
        guvenliClientBtn.setOnClickListener(new View.OnClickListener()
        {
			
			public void onClick(View v)
			{
				startActivity(new Intent(MainActivityResim.this, ResimClientSifreli.class));	
			}
		});
        
        guvenliServerBtn.setOnClickListener(new View.OnClickListener()
        {
			
			public void onClick(View v)
			{
				startActivity(new Intent(MainActivityResim.this, ResimServerSifreli.class));	
			}
		});
        
    }

}
