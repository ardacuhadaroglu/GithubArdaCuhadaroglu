package com.example.serveractivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import com.example.serveractivity.ClientActivity.ClientThread;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ServerActivity extends Activity {

	ClientActivity ca;
	
	private String serverIpAddress = "";
	
    private TextView serverStatus;
    private TextView durum;
    
    private EditText inmessage;
    private EditText outmessage;
    private EditText ipAdres;
    
    private Button connectPhones;

    // default ip
    public static String SERVERIP = "10.0.2.15";
    

    // designate a port
    public static final int SERVERPORT = 8080;
    public static final int SERVERPORT2 = 8090;

    Handler handler;

    private ServerSocket serverSocket;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);
        
        ca = new ClientActivity();
        
        durum = (TextView) findViewById(R.id.textView1);
        serverStatus = (TextView) findViewById(R.id.server_status);
        inmessage = (EditText) findViewById(R.id.editIp);
        outmessage = (EditText) findViewById(R.id.editText2);
        inmessage.setFocusable(false);
        connectPhones = (Button) findViewById(R.id.button1);
        connectPhones.setOnClickListener(connectListener);
        
        SERVERIP = getLocalIpAddress();

        handler = new Handler();
        
        Thread fst = new Thread(new ServerThread());
        fst.start();
    }
    
    private OnClickListener connectListener = new OnClickListener() {

        public void onClick(View v) {
            if (!connected) {
                serverIpAddress = "10";
                if (!serverIpAddress.equals("")) {
                    Thread cThread = new Thread(new ClientThread());
                    cThread.start();
                }
            }
            send = true;
        }
    };
    
    String line;
    boolean girdi;
    Socket client;
	
    String decline;
    
    public class ServerThread implements Runnable {

        public void run() {
            try {
                if (SERVERIP != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            serverStatus.setText("Listening on IP: " + SERVERIP);
                        }
                    });
                    serverSocket = new ServerSocket(SERVERPORT);
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
                                Log.d("ServerServerActivity", line);
                                
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
                                System.out.println("salkl");
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
    
    
    
    
    private boolean connected = false;
    private boolean send = false;
    InetAddress serverAddr;
    
    public class ClientThread implements Runnable {

    	public boolean flag = true;
        public void run() {
            try {
                serverAddr = client.getInetAddress();
//                handler.post(new Runnable() {
//                    public void run() {
//                        serverStatus.setText("Sending on IP: " + serverAddr.getHostAddress());
//                    }
//                });
                Log.d("ServerClientActivity", "C: Connecting...");
                Socket socket = new Socket(serverAddr, SERVERPORT2);
                connected = true;
                while (connected) {
                    try {
                    	if(send == true){
	                        Log.d("ServerClientActivity", "C: Sending command.");
	                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
	                                    .getOutputStream())), true);
	                            // where you issue the commands
//	                        	if(flag == true){
//	                        		out.println(MYSERVERIP);
//	                        		flag=false;
//	                        	}
	                        byte[] bytemsg = Base64.encode(outmessage.getText().toString().getBytes(), Base64.DEFAULT);
	                        String enc = new String(bytemsg);
                            out.println(enc);
                            Log.d("ServerClientActivity", "C: Sent.");
                            send = false;
                            handler.post(new Runnable() {
                                public void run() {
                                	outmessage.setText("");
                                	durum.setText("Mesaj Gönderildi..");
                                }
                            });
                    	}      	
//                    	Log.d("ClientActivity", "C: DönüyorServerClient");
                            
                    } catch (Exception e) {
                        Log.e("ServerClientActivity", "S: Error", e);
                    }
                }
                socket.close();
                Log.d("ServerClientActivity", "C: Closed.");
            } catch (Exception e) {
                Log.e("ServerClientActivity", "C: Error", e);
                connected = false;
            }
        }
    }
    
    
    
    
    
    
    

    // gets the ip address of your phone's network
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
             // make sure you close the socket upon exiting
             serverSocket.close();
             connected = false;
         } catch (IOException e) {
             e.printStackTrace();
         }
    }

}


