package com.example.serveractivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;




public class Server extends MainActivity{

	ImageView serverimg;
	
	TextView durum;
	
	Thread sThread;
	
	private Handler handler = new Handler();
	
	boolean isConnected;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serverresim);
		serverimg = (ImageView) findViewById(R.id.ivServerImg);
		durum = (TextView) findViewById(R.id.tvDurum);
		
		durum.setText(getLocalIpAddress());
		
		serverimg.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Bundle bundle = new Bundle();
				Intent intent = new Intent(Server.this,ShowImage.class);
				bundle.putString("photo", photo.getPath());
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		isConnected = true;
		sThread = new Thread(new ServerThread());
		sThread.start();
	}
	
	public File photo;
	
	Socket sock;
	ServerSocket serverSocket;
	public class ServerThread implements Runnable{

		public void run() {
			// TODO Auto-generated method stub
			
			try {
				
				serverSocket = new ServerSocket(8080);
				while(isConnected){
	                while (true) {
	                    // listen for incoming clients
	                    sock = serverSocket.accept();
	                    if(sock.isConnected()){
	                    	break;
	                    }
	                }
					
					int filesize = 1000000; // filesize temporary hardcoded
					
					long start = System.currentTimeMillis();
					int bytesRead;
					int current = 0;
					
					handler.post(new Runnable() {
	                    public void run() {
	                    	durum.setText("Connected.");
	                    }
	                });
					System.out.println("opened socket on server side...");
		
					// receive file
					photo=new File(Environment.getExternalStorageDirectory(), "photo.jpg");
					if (photo.exists()) {
						photo.delete();
					}
					byte[] mybytearray = new byte[filesize];
					InputStream is = sock.getInputStream();
					FileOutputStream fos = new FileOutputStream(photo.getPath());
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					
					bytesRead = is.read(mybytearray, 0, mybytearray.length);
					current = bytesRead;
					
					// thanks to A. Cádiz for the bug fix
					do {
	//					Log.d("server", "girdi");
						bytesRead = is.read(mybytearray, current,
								(mybytearray.length - current));
						if (bytesRead >= 0)
							current += bytesRead;
					} while (bytesRead > -1);
									
					bos.write(mybytearray, 0, current);
					
					handler.post(new Runnable() {
	                    public void run() {
	                    	serverimg.setImageURI(Uri.parse(photo.getPath()));
	                    }
	                });
					bos.flush();
					long end = System.currentTimeMillis();
					System.out.println(end - start);
					bos.close();	
				}
			}

			catch (Exception e) {
				e.printStackTrace();
				isConnected = false;
				Log.d("try 1", "Error");
			}
			
			
			
		}
		
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		isConnected = false;
		try {
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public byte[] subArray(byte[] b, int offset, int length) {
	    byte[] sub = new byte[length];
	    for (int i = offset; i < offset + length; i++) {
	      try {
	        sub[i - offset] = b[i];
	      } catch (Exception e) {

	      }
	    }
	    return sub;
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
