package com.example.serveractivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
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

public class ResimClientSifreli extends Activity{

	public static String SERVERIP = "";
	String filePath;
	TextView durum;
	ImageView imageView;
	Button imageCall;
	Button imageSend;
	
	EditText ipAdres;
	
	String serverIpAddress;
	
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
				
				Thread cthread = new Thread(new ClientThread());
				cthread.start();
				
			}
		});
		
		imageView.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Bundle bundle = new Bundle();
				Intent intent = new Intent(ResimClientSifreli.this,ShowImage.class);
				bundle.putString("photo", filePath);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	String key256;
	byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00 };
	
	byte[] keyBytes;
	
	byte[] decrypted;
	byte[] encrypted;
	byte[] result;
	
	public class ClientThread implements Runnable{

		public void run() {
			// TODO Auto-generated method stub
			
			
			try {
				RandomString rs = new RandomString(32);
				key256 = rs.nextString();
				keyBytes = key256.getBytes("UTF-8");
//				handler.post(new Runnable() {
//                    public void run() {
//                    	durum.setText(key256 +"\n" + keyBytes.length);
//                    }
//                });
				
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
				
				//------------------Hash Function----------////working
				String imageHashString = computeHash(mybytearray);
				System.out.println(imageHashString);
				//-----------------------------------------//
				
				encrypted = ObjectCrypter.encrypt(ivBytes, keyBytes, mybytearray);
				byte[] enc = Base64.encode(encrypted, Base64.DEFAULT);
				
				byte[] result = new byte[enc.length + keyBytes.length];
				
				
				
				System.arraycopy(keyBytes, 0, result, 0, keyBytes.length);
				System.arraycopy(enc, 0, result, keyBytes.length, enc.length);
				
				os.write(result, 0, result.length);
				os.flush();
				
				sock.close();
				os.close();
			}

			catch (Exception e) {
				e.printStackTrace();
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
