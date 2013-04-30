package com.example.serveractivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.example.serveractivity.ServerActivity.ServerThread;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ClientActivity extends Activity {

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
	
    public class ClientThread implements Runnable {

    	public boolean flag = true;
        public void run() {
            try {
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
                            // where you issue the commands
	                        byte[] bytemsg = Base64.encode(outmessage.getText().toString().getBytes(), Base64.DEFAULT);
	                        String enc = new String(bytemsg);
                            out.println(enc);
                            Log.d("ClientClientActivity", "C: Sent.");
                            send = false;
                            handler.post(new Runnable() {
                                public void run() {
                                	outmessage.setText("");
                                	durum.setText("Mesaj Gönderildi..");
                                }
                            });
                    	}      	
//                    	Log.d("ClientActivity", "C: Dönüyor");
                            
                    } catch (Exception e) {
                        Log.e("ClientClientActivity", "S: Error", e);
                    }
                }
                socket.close();
                Log.d("ClientClientActivity", "C: Closed.");
            } catch (Exception e) {
                Log.e("ClientClientActivity", "C: Error", e);
                connected = false;
            }
        }
    }
    
    
    
    
    String line;
    boolean girdi;
    Socket client;
    
    String decline;
    
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
                            line = null;
                            while ((line = in.readLine()) != null) {
                            	girdi = false;
                            	if(line.length() == 0)
                                	continue;
                                Log.d("ClientServerActivity", line);
                                
                                byte[] byteline = Base64.decode(line, Base64.DEFAULT);
                                decline = new String(byteline);
                                
                                handler.post(new Runnable() {
                                    public void run() {
                                        // do whatever you want to the front end
                                        // this is where you can be creative
                                    	if(girdi == false){
                                    		inmessage.append(decline + "\n\n");
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
            Log.e("ClientServerActivity", ex.toString());
        }
        return null;
    }
    
    
    
}

