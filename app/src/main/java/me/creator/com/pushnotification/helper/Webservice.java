package me.creator.com.pushnotification.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import me.creator.com.pushnotification.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by User1 on 05-08-2016.
 */
public class Webservice {
    //public static String serviceUrlFirstPart = "";
    public static String serviceUrlFirstPart = "http://52.66.88.181/";      //test server
    //public static String serviceUrlFirstPart = "http://52.66.108.224/";    //live server
    //public static String serviceUrlFirstPart = "http://35.154.191.62/";
    public static final String Soap_action_firstPart = "http://tempuri.org/";
    private static final String NAMESPACE = "http://tempuri.org/";
    Thread networkThread;
    private String checkResponse = "nothing";
    private boolean processComplete = false;
    Context context;
    ProgressDialog progDailog;
    String serviceUrlSecondPart, Method_name, Function_Name;
    ArrayList<PropertyInfo> Collection = new ArrayList<PropertyInfo>();
    static final int MAX_CONNECTIONS = 5;

    public ArrayList<PropertyInfo> callMethodName(ArrayList<Parameters> Recieved, Context contexts, String SecondPart, String Method, String Function) {
        serviceUrlSecondPart = SecondPart;
        Method_name = Method;
        Function_Name = Function;
        checkResponse = "";
        for (int i = 0; i < Recieved.size(); i++) {
            PropertyInfo pi = new PropertyInfo();
            if (!Recieved.isEmpty()) {
                Parameters p = Recieved.get(i);
                if (p.valueType.equalsIgnoreCase("int")) {
                    Integer intValue = Integer.parseInt(p.value);
                    pi.setValue(intValue);
                    pi.setName(p.name);
                    pi.setType(Integer.class);
                    Collection.add(pi);
                } else if (p.valueType.equalsIgnoreCase("String")) {
                    pi.setValue(p.value);
                    pi.setName(p.name);
                    pi.setType(String.class);
                    Collection.add(pi);
                } else if (p.valueType.equalsIgnoreCase("Float")) {
                    pi.setValue(p.value);
                    pi.setName(p.name);
                    pi.setType(Float.class);
                    Collection.add(pi);
                }
            }
        }
        return Collection;
    }


    protected Object call(final String soapAction,
                          final SoapSerializationEnvelope envelope, String URL, int failures) {
        Object result = null;
        final HttpTransportSE transportSE = new HttpTransportSE(URL);

        try {
            transportSE.call(soapAction, envelope);
            result = envelope.getResponse();
            String resultData = result.toString();

        } catch (EOFException e) {
            //Log.d("exe-Exception",""+e.toString());
        } catch (final IOException e) {
            //Log.d("exe-Exception",""+e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final XmlPullParserException e) {
            //Log.d("exe-Exception",""+e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final Exception e) {
            //Log.d("exe-Exception",""+e.toString());
            // TODO Auto-generated catch block

            e.printStackTrace();
        }
        return result;
    }

    public static boolean isNetworkStatusAvialable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null)
                if (netInfos.isConnected())
                    return true;
        }
        return false;
    }

    public String getCheckResponse() {
        return checkResponse;
    }

    public class YourTask extends AsyncTask<Void, Void, String> { //change Object to required type
        private OnTaskCompleted listener;
        String result = null;


        public YourTask(Context cxt, OnTaskCompleted listener) {
            this.listener = listener;
            //if (cxt != null) {
            progDailog = new ProgressDialog(cxt);
            // }
            context = cxt;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {

              /*  //Fetch Firebase url.................//
                // Initialize Firebase Remote Config.
                mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                // Define Firebase Remote Config Settings.
                FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                        new FirebaseRemoteConfigSettings.Builder()
                                .setDeveloperModeEnabled(true)
                                .build();
                // Define default config values. Defaults are used when fetched config values are not
                // available. Eg: if an error occurred fetching values from the server.
                Map<String, Object> defaultConfigMap = new HashMap<>();
                defaultConfigMap.put("first_part", Webservice.serviceUrlFirstPart);

                // Apply config settings and default values.
                mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
                mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
                // Fetch remote config.
                serviceUrlFirstPart = fetchConfig();*/
                //.......................................Fetch Firebase url//


                Object response = null;
                String URL = serviceUrlFirstPart + serviceUrlSecondPart;
                String SOAP_ACTION = Soap_action_firstPart
                        + Method_name;
                String METHOD_NAME = Function_Name;
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                        SoapEnvelope.VER11);
                // THIS BLOCK READS THE ARRALIST CONTENTS AND ASSIGNS TO THE
                // PROPERTY
                if (Collection != null) {
                    for (int i = 0; i < Collection.size(); i++) {
                        request.addProperty(Collection.get(i));
                        Log.d("exe-Request-", "" + URL + request);
                    }
                }

                envelope.setOutputSoapObject(request);
                envelope.dotNet = true;
                int attempts = 1;
                response = call(SOAP_ACTION, envelope, URL, 0);
                processComplete = true;

                if (response != null) {
                    checkResponse = response.toString();

                }
                // }
            } catch (Exception e) {

            }
            return checkResponse;

        }
// required methods

        protected void onPostExecute(String o) {

            // your stuff
            Log.d("exe-Response-", o);
            if (!context.getClass().getName().contains("Application")) {

            }
            final String response = o;
            if (o.equalsIgnoreCase("")) {
                String title = "Some error occurred";
                String message = "Please try again later!";
                if (!isNetworkStatusAvialable(context)) {
                    title = "No Internet Connection";
                    message = "You are offline. Please check your internet connection";
                }
                android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(context);
                alertDialogBuilder.setTitle(title);
                alertDialogBuilder.setMessage(message);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        try {

                            if (context.getClass().toString().contains("Login")) {
                                ((Activity) context).finish();
                            } else if (context.getClass().toString().contains("Splash")) {
                                Toast.makeText(context, "Some error occurred..", Toast.LENGTH_LONG).show();
                                ((Activity) context).finish();
                            } else {
                                // MainActivity.progDailog.dismiss();
                            }
                        } catch (Exception e) {

                        }

                    }
                });
                android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                listener.onTaskCompleted(response);
            }
        }
    }
}

