package com.hackmit.sentry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	String TAG = "MainActivity";

	TextView myLabel;
	EditText myTextbox;
	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	OutputStream mmOutputStream;
	InputStream mmInputStream;
	Thread workerThread;
	byte[] readBuffer;
	int readBufferPosition;
	int counter;
	volatile boolean stopWorker;

	LinearLayout root;

	//SmsSender b;

	private static final String SMTP_HOST_NAME = "smtp.sendgrid.net";
	private static final String SMTP_AUTH_USER = "afridi2@illinois.edu";
	private static final String SMTP_AUTH_PWD  = "alikhanafridi1995";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		myLabel = (TextView) findViewById(R.id.label);

		// Try to start the bluetooth connection
		try {
			findBT();
			openBT();
		} 
		catch (IOException ex) {
		}
		root=(LinearLayout)findViewById(R.id.root);

		//
		/*b = new SmsSender();

		try {
			Log.e(TAG, "Trying SmsSender");
			b.main();
		} catch (Exception e) {
			Log.e(TAG, "SMS no work");
			e.printStackTrace();
		}*/

		MailbackTask task = new MailbackTask();
		task.execute();
		
		//new SMSbackTask().execute();

	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			closeBT();
		} catch (IOException ex) {
			Log.e(TAG, "Problem closing Bluetooth");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			findBT();
			openBT();
		} 
		catch (IOException ex) {
		}
	}

	void findBT() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			myLabel.setText("No bluetooth adapter available");
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBluetooth = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, 0);
		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getName().equals("HC-06")) // this name have to be
					// replaced with your
					// bluetooth device name
				{
					mmDevice = device;
					Log.e("ArduinoBT",
							"findBT found device named " + mmDevice.getName());
					Log.e("ArduinoBT",
							"device address is " + mmDevice.getAddress());
					break;
				}
			}
		}
		myLabel.setText("Bluetooth Device Found");
	}

	void openBT() throws IOException {

		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard SerialPortService ID
		mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
		mmSocket.connect();
		mmOutputStream = mmSocket.getOutputStream();
		mmInputStream = mmSocket.getInputStream();
		myLabel.setText("Bluetooth Opened");
		beginListenForData();

	}

	void beginListenForData() {
		final Handler handler = new Handler();
		final byte delimiter = 10; // This is the ASCII code for a newline
		// character

		stopWorker = false;
		readBufferPosition = 0;
		readBuffer = new byte[1024];
		workerThread = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted() && !stopWorker) {
					try {
						int bytesAvailable = mmInputStream.available();
						if (bytesAvailable > 0) {
							byte[] packetBytes = new byte[bytesAvailable];
							mmInputStream.read(packetBytes);
							for (int i = 0; i < bytesAvailable; i++) {
								byte b = packetBytes[i];
								if (b == delimiter) {
									byte[] encodedBytes = new byte[readBufferPosition];
									System.arraycopy(readBuffer, 0,
											encodedBytes, 0,
											encodedBytes.length);
									final String data = new String(
											encodedBytes, "US-ASCII");
									readBufferPosition = 0;

									handler.post(new Runnable() {
										public void run() {
											myLabel.setText(data);
											updateUI(data);
										}
									});
								} else {
									readBuffer[readBufferPosition++] = b;
								}
							}
						}
					} catch (IOException ex) {
						stopWorker = true;
					}
				}
			}
		});

		workerThread.start();
	}

	void sendData() throws IOException {
		String msg = myTextbox.getText().toString();
		msg += "";
		myLabel.setText("Data Sent " + msg);
	}

	void onButton() throws IOException {
		mmOutputStream.write("1".getBytes());
	}

	void offButton() throws IOException {
		mmOutputStream.write("2".getBytes());
	}

	void closeBT() throws IOException {
		stopWorker = true;
		mmOutputStream.close();
		mmInputStream.close();
		mmSocket.close();
		myLabel.setText("Bluetooth Closed");
	}

	public void updateUI(String activityData) {
		activityData = activityData.trim();
		if (activityData.equals("Nothing")) {
			root.setBackgroundColor(Color.parseColor("#6ED3F7")); // Blue
		}
		else if (activityData.equals("Motion")) {
			root.setBackgroundColor(Color.parseColor("#FF9999")); // Red
		}
		else {
			root.setBackgroundColor(Color.parseColor("FFFC99")); // Yellow FFCC33
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class MailbackTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				//new MailService().test();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}


	}
	/*
	private class SMSbackTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Log.e("TAG", "New SMS");
				new SmsSender().test();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}


	}
*/
}
