package com.example.kp.finalandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView txvResult,answer;
    private TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    // responsible to create an activity
    //whenever orientation or terminating by os take place it is saved by savedInstanceState, After Orientation changed then onCreate(Bundle savedInstanceState) will call and recreate the activity and load all data from savedInstanceStat
    {
        super.onCreate(savedInstanceState);
        //However, you must include this super call in your method, because if you don't then the onCreate() code in Activity is never run, and your app will run into all sorts of problem like having no Context assigned to the Activity
        setContentView(R.layout.activity_main);
        //R.layout.* references any layout resource you have created, usually in /res/layout. So if you created an activity layout called activity_main.xml, you can then use the reference in R.layout.activity_main to access it.
        txvResult = (TextView) findViewById(R.id.txvResult);
        answer=(TextView) findViewById(R.id.answer);
        //  tts = new TextToSpeech(this, (TextToSpeech.OnInitListener) this);


        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });
    }


    public void getSpeechInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
        //locale.getDefault -- default language of mobile
//The Google Voice API is performed in the network, so if you’re using the Google speech rec, it will only function when your network is activated.Speech recognition is achieved using the android.speech.RecognizerIntent. class. When we want to invoke the speech recogniser we simply fire a RecognizerIntent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);

        } else {
            Toast.makeText(this, "your device dont support speech input", Toast.LENGTH_SHORT).show();
            //A toast provides simple feedback about an operation in a small popup. It only fills the amount of space required for the message and the current activity remains visible and interactive. Toasts automatically disappear after a timeout.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//When the user is done with the subsequent activity and returns, the system calls your activity's (onActivityResult)method. This method includes three arguments:
        //The request code you passed to requestCode
        //A result code specified by the second activity. This is either resultcode if the operation was successful or result_cancelled if the user backed out or the operation failed for some reason.
        //An Intent that carries the result data
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txvResult.setText(result.get(0));
                    new senddata().execute(String.valueOf(result.get(0)));
                }
                break;
        }

    }

    public class senddata extends AsyncTask<String, String, String> {

        BufferedReader br = null;
        URL url;
        String txt = "";
        HttpURLConnection conn;
        private int _LONG;
        private HashMap<Object, Object> parameters;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {             //url of your php file
                url = new URL("http://10.1.54.221:8080/farEdgeEngine/api/nlp/chatbot.farEdge?request="+params[0]+"&userid=123");


            } catch (MalformedURLException m1) {

                txt = "exception";
            }
            try {
                //establishing connection
                conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000);
                //connection timeout
                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                // appending the query parameteres with key
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("request", params[0]);

                String query = builder.build().getEncodedQuery();
                //starting output stream to upload data to server
                OutputStream os = conn.getOutputStream();
                BufferedWriter wr = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                //writing the data
                wr.write(query);
                wr.flush();
                wr.close();

                conn.connect();


            } catch (IOException e1) {
                txt = "input";
            }
            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection is established
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;
                    //System.out.print(input);

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    result.substring(12);
                    return (result.substring(12));
                   // System.out.print(input);
                    // Pass data to onPostExecute method
                  //  return (result.toString());



                } else {

                    return ("unsuccessful with response code "+response_code);
                }

            } catch (IOException e) {
                e.printStackTrace();
                txt = "exceptio " + e.getMessage()+e.getStackTrace();
            } finally {
                conn.disconnect();
            }
            return txt;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //  setContentView(R.layout.activity_main);
            // set.content(MainActivity.this,s,LENGTH_LONG).show();
            //Toast.makeText(MainActivity.this, s, LENGTH_LONG).show();
            answer.setVisibility(View.VISIBLE);
            answer.setText(s);
            t1.speak(s,TextToSpeech.QUEUE_FLUSH,null);

        }



    }


}