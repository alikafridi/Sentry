package com.hackmit.sentry;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Sms;

public class SmsSender {
	// Find your Account Sid and Token at twilio.com/user/account
	public static final String ACCOUNT_SID = "";
	public static final String AUTH_TOKEN = "";

	public static void main() throws TwilioRestException  {
		new SmsSender().test();
	}

	public void test() throws TwilioRestException{
		Log.e("TAG", "Starting Client");
		  TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
		    Log.e("TAG", "Starting SMS");
		    // Build a filter for the SmsList
		    Map<String, String> params = new HashMap<String, String>();
		    params.put("Body", "Testing. Please Work!");
		    params.put("To", "+17816407891");
		    params.put("From", "+17864225484");

		    SmsFactory messageFactory = client.getAccount().getSmsFactory();
		    Sms message = messageFactory.create(params);
		    System.out.println(message.getSid());

			Log.e("TAG", "SmsSent");
		
	}
}
