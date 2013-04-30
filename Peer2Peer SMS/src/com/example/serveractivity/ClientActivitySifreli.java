package com.example.serveractivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.Dialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ClientActivitySifreli extends Activity {

	public String MYSERVERIP;
	public static final int SERVERPORT2 = 8090;
	
	private TextView durum;
	private TextView serverStatus;
	
    private EditText serverIp;
    private EditText outmessage;
    private EditText inmessage;

    private Button connectPhones;
    private Button sendPicture;

    private String serverIpAddress = "";

    private boolean connected = false;
    private boolean send = false;

    private Handler handler = new Handler();
    
    private ServerSocket serverSocket;
//    ServerActivity sa;

    public String getIP(){
    	
			return MYSERVERIP;
		
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client2);

//        sa = new ServerActivity();
        
        
        durum = (TextView) findViewById(R.id.textView4);
        serverStatus = (TextView) findViewById(R.id.textView5);
        serverIp = (EditText) findViewById(R.id.editIp);
        inmessage = (EditText) findViewById(R.id.editText2);
        inmessage.setFocusable(false);
        outmessage = (EditText) findViewById(R.id.editText3);
        connectPhones = (Button) findViewById(R.id.button1);
        sendPicture = (Button) findViewById(R.id.button2);
        connectPhones.setOnClickListener(connectListener);
        
        MYSERVERIP = getLocalIpAddress();
    }

    private OnClickListener connectListener = new OnClickListener() {

        public void onClick(View v) {
        	if(v == connectPhones){
	            if (!connected) {
	                serverIpAddress = serverIp.getText().toString();
	                if (!serverIpAddress.equals("")) {
	                    Thread cThread = new Thread(new ClientThread());
	                    cThread.start();
	                    
	                    Thread fst = new Thread(new ServerThread());
	                    fst.start();
	                }
	            }
	            send = true;
	        }
        	else if(v == sendPicture){
        		
        	}
        }
    };

    String key256 = "";
    byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00 };
    byte[] keyBytes;
    byte[] encrypted;
	byte[] decrypted;
	byte[] result;
	
    public class ClientThread implements Runnable {

    	public boolean flag = true;
        public void run() {
            try {
            	
            	RandomString rs = new RandomString(32);
            	key256 = rs.nextString();
            	keyBytes = key256.getBytes("UTF-8");
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                Log.d("ClientClientActivity", "C: Connecting...");
                Socket socket = new Socket(serverAddr, ServerActivity.SERVERPORT);
                connected = true;
                while (connected) {
                    try {
                    	if(send == true){
	                        Log.d("ClientClientActivity", "C: Sending command.");
	                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
	                                    .getOutputStream())), true);
	                        
	                        byte[] plainText = outmessage.getText().toString().getBytes(); 
	                        
	                        String md5Hashedtext = md5(outmessage.getText().toString());
	                        
	                        encrypted = ObjectCrypter.encrypt(ivBytes, keyBytes, plainText);
	                        String enc = Base64.encodeToString(encrypted, Base64.DEFAULT);
                            // where you issue the commands
                            out.println(key256 + md5Hashedtext + enc);//hash eklendi
                            
                            Log.d("ClientClientActivity", "C: Sent.");
                            Log.d("ClientClientActivity", "Size = " + enc.length());
                            send = false;
                            handler.post(new Runnable() {
                                public void run() {
                                	outmessage.setText("");
                                	durum.setText("Mesaj Gönderildi..");
//                                	durum.setText(""+enc);
                                }
                            });
                    	}      	
//                    	Log.d("ClientActivity", "C: Dönüyor");
                            
                    } catch (Exception e) {
                        Log.e("ClientClientActivity", "S: Error", e);
                    }
                }
                socket.close();
                Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
                Log.e("ClientActivity", "C: Error", e);
                connected = false;
            }
        }
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
    
    String fullline;
    boolean girdi;
    Socket client;
    
    String dec;
    
    public class ServerThread implements Runnable {

        public void run() {
            try {
                if (MYSERVERIP != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            serverStatus.setText("Listening on IP: " + MYSERVERIP);
                        }
                    });
                    serverSocket = new ServerSocket(SERVERPORT2);
                    while (true) {
//                    	Log.d("ClientServerActivity", line);
                        // listen for incoming clients
                        client = serverSocket.accept();
                        handler.post(new Runnable() {
                            public void run() {
//                                serverStatus.setText("Connected.");
                                serverStatus.append("\nSending on IP: " + client.getInetAddress().getHostAddress());
                            }
                        });
                        
                        
                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            fullline = null;
                            while ((fullline = in.readLine()) != null) {
                            	girdi = false;
                                Log.d("ClientServerActivity", fullline);
                                
                               final String hashStringGelen = fullline.substring(0, 32);//hash eklendi
                               String line = fullline.substring(32);
                                
                                try {
                                	encrypted = Base64.decode(line, Base64.DEFAULT);
                                	if(encrypted.length == 0)
                                		continue;
                                    Log.d("ClientServerActivity", "size = " + encrypted.length);
									decrypted = ObjectCrypter.decrypt(ivBytes, keyBytes, encrypted);
									dec = new String(decrypted);
									
									handler.post(new Runnable() {
                                        public void run() {
                                        	
                                        	if(hashCompare(hashStringGelen, md5(dec)))//hash karþýlaþtýrmasý
                                				Toast.makeText(getApplicationContext(), "Mesaj Bozulmamýþ", Toast.LENGTH_SHORT).show();
                                    			
                                    		else
                                    			Toast.makeText(getApplicationContext(), "Mesaj Bozulmuþ!", Toast.LENGTH_SHORT).show();
                                        
                                        }
                                    });
                            		
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                                
                                handler.post(new Runnable() {
                                    public void run() {
                                        // do whatever you want to the front end
                                        // this is where you can be creative
                                    	if(girdi == false){
                                    		inmessage.append(dec + "\n\n");
                                    		girdi = true;
                                    	}
                                    }
                                });
                            }
                            break;
                        } catch (Exception e) {
                            handler.post(new Runnable() {
                                public void run() {
                                    serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            serverStatus.setText("Couldn't detect internet connection.");
                        }
                    });
                }
            } catch (Exception e) {
                handler.post(new Runnable() {
                    public void run() {
                        serverStatus.setText("Error");
                    }
                });
                e.printStackTrace();
            }
        }
    }
    
    
    public boolean hashCompare(String hashedM1, String hashedM2)
    {
    	if(hashedM1.equals(hashedM2))
    		return true;
    	
    	return false;
    }
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("ClientActivity", ex.toString());
        }
        return null;
    }
    
    
    
}

