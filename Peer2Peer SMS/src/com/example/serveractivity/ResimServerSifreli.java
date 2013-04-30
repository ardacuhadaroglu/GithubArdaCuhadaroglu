package com.example.serveractivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;




public class ResimServerSifreli extends MainActivity{

//	EditText ipAdres;
//	Button connect;
//	String serverIpAddress;
	ImageView serverimg;
	
	TextView durum;
	
	Thread sThread;
	
	private Handler handler = new Handler();
	
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
				Intent intent = new Intent(ResimServerSifreli.this,ShowImage.class);
				bundle.putString("photo", photo.getPath());
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		sThread = new Thread(new ServerThread());
		sThread.start();
	}
	
	byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00 };
	
	byte[] keyBytes;
	
	byte[] decrypted;
	byte[] encrypted;
	byte[] result;
	
	public File photo;
	
	Socket sock;
	ServerSocket serverSocket;
	public class ServerThread implements Runnable{

		public void run() {
			// TODO Auto-generated method stub
			
			try {
				
				serverSocket = new ServerSocket(8080);
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
				byte[] mybytearray = new byte[filesize + 32];
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
	
				keyBytes = subArray(mybytearray, 0, 32);
				byte[] image = subArray(mybytearray, 32, mybytearray.length - 32);
				
				decrypted = ObjectCrypter.decrypt(ivBytes, keyBytes, Base64.decode(image , Base64.DEFAULT));
				
				//-------------Hashing-------------------------//working
				String imageHashString = computeHash(decrypted);
				System.out.println(imageHashString);
				//---------------------------------------------//
				
				bos.write(decrypted, 0, decrypted.length);
				
				
				
				handler.post(new Runnable() {
                    public void run() {
                    	//Toast.makeText(getApplicationContext(), imageHashString, Toast.LENGTH_LONG).show();
                    	serverimg.setImageURI(Uri.parse(photo.getPath()));
                    }
                });
				bos.flush();
				long end = System.currentTimeMillis();
				System.out.println(end - start);
				bos.close();
				sock.close();
				is.close();
				sThread = null;
			}

			catch (Exception e) {
				e.printStackTrace();
				Log.d("try 1", "Error");
			}
			
			
			
		}
		
	}
	
	public String computeHash(byte[] input) throws NoSuchAlgorithmException, UnsupportedEncodingException{
	    MessageDigest digest = MessageDigest.getInstance("SHA-256");
	    digest.reset();

	    byte[] byteData = digest.digest(input);
	    StringBuffer sb = new StringBuffer();

	    for (int i = 0; i < byteData.length; i++){
	      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
	
	public String md5(String message) {
    	String messageHash = "";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(message.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            messageHash  = hexString.toString();
           

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return messageHash;
    }
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
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
