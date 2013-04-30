package com.example.serveractivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class Client extends Activity{

	public static String SERVERIP = "";
	String filePath;
	TextView durum;
	ImageView imageView;
	Button imageCall;
	Button imageSend;
	
	EditText ipAdres;
	
	String serverIpAddress;
	
	boolean isConnected = false,willSend = false;
	
	private Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clientresim);
		
		durum = (TextView) findViewById(R.id.textView1);
		imageView = (ImageView) findViewById(R.id.ivClientImg);
		imageCall = (Button) findViewById(R.id.button2);
		imageSend = (Button) findViewById(R.id.button1);
		ipAdres = (EditText) findViewById(R.id.editIp);
		
		SERVERIP = getLocalIpAddress();
		durum.setText(SERVERIP);
		
		imageCall.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v) 
			{
				Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), 1);				
			}
		});
		
		imageSend.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v) {

				willSend = true;
				
				if(isConnected == false){
					Thread cthread = new Thread(new ClientThread());
					cthread.start();
					isConnected = true;
				}
				
			}
		});
		
		imageView.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Bundle bundle = new Bundle();
				Intent intent = new Intent(Client.this,ShowImage.class);
				bundle.putString("photo", filePath);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	ServerSocket servsock;
	Socket sock;
	
	public class ClientThread implements Runnable{

		public void run() {
			// TODO Auto-generated method stub
			
			
			try {
				
				while(isConnected){
					
					if(willSend){
						handler.post(new Runnable() {
		                    public void run() {
		                    	Log.d("ClientActivity","Waiting...");
		                    }
		                });
						
						serverIpAddress = ipAdres.getText().toString();
						
						InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
						Log.d("ClientActivity", "C: Connecting...");
						Socket sock = new Socket(serverAddr, 8080);
						
						handler.post(new Runnable() {
		                    public void run() {
		                    	durum.setText("Connected.");
		                    }
		                });
						Log.d("ClientActivity","Accepted connection : " + sock);
		
						
						// sendfile
						File myFile = new File(filePath);
						byte[] mybytearray = new byte[(int) myFile.length()];
						FileInputStream fis = new FileInputStream(myFile);
						BufferedInputStream bis = new BufferedInputStream(fis);
						
						bis.read(mybytearray, 0, mybytearray.length);
						OutputStream os = sock.getOutputStream();
						System.out.println("Sending...");
						
						os.write(mybytearray, 0, mybytearray.length);
						os.flush();
						os.close();
						willSend = false;
					}
				}
				sock.close();
				
			}

			catch (Exception e) {
				e.printStackTrace();
				isConnected = false;
			}
			
			
			
		}
		
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		isConnected = false;
	}
	
	//get the path of image falan
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath = getPath(selectedImageUri);
                filePath = selectedImagePath;
                imageView.setImageURI(Uri.parse(filePath));
            }
        }
    }
    //get image uri return path
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("Client", ex.toString());
        }
        return null;
    }

}
