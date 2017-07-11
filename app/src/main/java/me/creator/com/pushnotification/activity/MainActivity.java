package me.creator.com.pushnotification.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.creator.com.pushnotification.R;
import me.creator.com.pushnotification.app.Config;
import me.creator.com.pushnotification.helper.OnTaskCompleted;
import me.creator.com.pushnotification.helper.Parameters;
import me.creator.com.pushnotification.helper.Webservice;
import me.creator.com.pushnotification.util.NotificationUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private TextView txtRegId;
    public static TextView txtMessage;
    private Button bt1;
    private ArrayList<Parameters> toSend = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtRegId = (TextView) findViewById(R.id.txt_reg_id);
        txtMessage = (TextView) findViewById(R.id.txt_push_message);
        bt1 = (Button) findViewById(R.id.bt1);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DbActivity.class);
                startActivity(i);
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    if (message != null && message.equalsIgnoreCase("1")) {
                        Toast.makeText(getApplicationContext(), "Message from server = " + message, Toast.LENGTH_LONG).show();
                        loadDropDownQuestion();
                    } else if (message != null && message.equalsIgnoreCase("2")) {
                        Toast.makeText(getApplicationContext(), "Message from server = " + message, Toast.LENGTH_LONG).show();
                        load2ndFunction();
                    } else {
                        Toast.makeText(getApplicationContext(), "Nothing from server", Toast.LENGTH_LONG).show();
                    }

                    // Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    //  txtMessage.setText(message);
                }
            }
        };
        displayFirebaseRegId();
    }

    // Fetches reg id from shared preferences
    // and displays on the screen
    private void displayFirebaseRegId() {
        //  android.os.Debug.waitForDebugger();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);

        if (!TextUtils.isEmpty(regId))
            txtRegId.setText("Firebase Reg Id: " + regId);
        else
            txtRegId.setText("Firebase Reg Id is not received yet!");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        txtMessage.setText(NotificationUtils.msg);

        if (NotificationUtils.msg != null && NotificationUtils.msg.equalsIgnoreCase("1")) {
            Toast.makeText(getApplicationContext(), "Message from server = " + NotificationUtils.msg, Toast.LENGTH_LONG).show();
            loadDropDownQuestion();
        } else if (NotificationUtils.msg != null && NotificationUtils.msg.equalsIgnoreCase("2")) {
            Toast.makeText(getApplicationContext(), "Message from server = " + NotificationUtils.msg, Toast.LENGTH_LONG).show();
            load2ndFunction();
        } else {
            Toast.makeText(getApplicationContext(), "Nothing from server", Toast.LENGTH_LONG).show();
        }

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void loadDropDownQuestion() {
        final StringBuffer sb = new StringBuffer();
        String defaultText = "Select a Question ";
        String serviceUrlSecondPart = "MyJobsApp/User/UserService.svc?wsdl";
        String Method_name_GetQuestionDetails = "IUserService/GetPRQuestion";
        String Function_Name_GetQuestionDetails = "GetPRQuestion";
        Webservice webAppUpdates = new Webservice();
        webAppUpdates.callMethodName(toSend, this, serviceUrlSecondPart, Method_name_GetQuestionDetails, Function_Name_GetQuestionDetails);
        Webservice.YourTask taskLoadDropDownQuestion = webAppUpdates.new YourTask(this, new OnTaskCompleted() {
            @Override

            public void onTaskCompleted(String webResponse) {
                String resultConfirmAppUpdates;
                resultConfirmAppUpdates = webResponse;
                try {
                    JSONArray jsonArr = new JSONArray(resultConfirmAppUpdates);
                    for (int i = 0; i < jsonArr.length(); i++) {
                        JSONObject jsonObj = jsonArr.getJSONObject(i);
                        String passwordQuestionKey = optString(jsonObj, ("PasswordQuestionKey"));
                        String passwordQuestion = optString(jsonObj, ("PasswordQuestion"));
                        String isActive = optString(jsonObj, ("isActive"));
                        if (isActive != null && isActive.equalsIgnoreCase("true")) {
                            sb.append(passwordQuestion);
                        }
                    }
                    txtMessage.setText(sb.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        taskLoadDropDownQuestion.execute();
    }

    private void load2ndFunction() {
        String serviceUrlSecondPart = "myjobsApp/User/UserService.svc";
        String Method_name_login = "IUserService/Login";
        String Function_Name_login = "Login";
        Parameters pmmob = new Parameters("9900350924", "String", "MobileNo");
        Parameters pmpaswd = new Parameters("123456", "String", "encPassword");
        Parameters isenc = new Parameters("false", "String", "isEnc");
        toSend.clear();
        toSend.add(pmmob);
        toSend.add(pmpaswd);
        toSend.add(isenc);
        Webservice webLogin = new Webservice();
        webLogin.callMethodName(toSend, getApplicationContext(), serviceUrlSecondPart, Method_name_login, Function_Name_login);
        Webservice.YourTask taskAppLogin = webLogin.new YourTask(MainActivity.this, new OnTaskCompleted() {
            @Override

            public void onTaskCompleted(String webResponse) {
                String resultlogin;
                resultlogin = webResponse;
                txtMessage.setText(resultlogin);
            }
        });
        taskAppLogin.execute();
    }

    private String optString(JSONObject json, String key) {
        String toReturn = "hello";
        if (json.isNull(key))
            return null;
        else {
            try {
                toReturn = json.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return toReturn;
    }
}

