package com.example.bhatvig.andriodlogin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;


public class SignUp extends Activity {

    EditText emailAddress, passWord ,userName;
    Button submitButton,loginButton;
    TextView status;
    String TextEmail;
    boolean loginStatus;
    String TextPassword;
    String TextUserName;
    String response = null;
    static boolean errored = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        userName=(EditText)findViewById(R.id.name);
        emailAddress = (EditText) findViewById(R.id.email);
        passWord = (EditText) findViewById(R.id.passWord);
        userName.setTextColor(Color.BLACK);
        emailAddress.setTextColor(Color.BLACK);
        passWord.setTextColor(Color.BLACK);
        submitButton = (Button) findViewById(R.id.submit);
        loginButton=(Button)findViewById(R.id.login);
        status = (TextView) findViewById(R.id.tv_result);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userName.getText().length() != 0) {
                    if (emailAddress.getText().length() != 0 && emailAddress.getText().toString() != "" && android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress.getText().toString()).matches()) {
                        if (passWord.getText().length() != 0 && passWord.getText().toString() != "") {
                            TextUserName=userName.getText().toString();
                            TextEmail = emailAddress.getText().toString();
                            TextPassword = passWord.getText().toString();
                            status.setText("");
                            status.setTextColor(Color.BLACK);

                       /* StrictMode.ThreadPolicy mypolicy =
                                new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(mypolicy);*/
                            CookieSyncManager.createInstance(SignUp.this);
                            CookieManager.getInstance().setAcceptCookie(true);
                            validateUserTask task = new validateUserTask();
                            task.execute(new String[]{TextEmail, TextPassword,TextUserName});
                        } else {
                            status.setText("Please enter Password");
                        }
                    } else {
                        status.setText("Please enter EmailAddress");
                    }
                } else {
                    status.setText("Please enter the UserName");
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignUp.this, Login.class);
                startActivity(i);
            }
        });
    }
         class validateUserTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                // TODO Auto-generated method stub
                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("name", params[0] ));
                postParameters.add(new BasicNameValuePair("email", params[1] ));
                postParameters.add(new BasicNameValuePair("password", params[1] ));
                String res = null;
                try {
                    response = CustomHttpClient.executeHttpPost("http://9mints.com:8080/api/signup", postParameters);
                    //res=response.toString();
                    Log.w("SENCIDE", response);
                    //res= res.replaceAll("\\s+","");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return response;
            }//close doInBackground

            @Override
            protected void onPostExecute(String result) {
                if(response.equals("200")){
                    //navigate to Main Menu
                    Intent i = new Intent(SignUp.this, AfterLogin.class);
                    startActivity(i);
                }
                else{
                    status.setText("Sorry!! Incorrect Username or Password");
                }
            }//close onPostExecute
        }

    }


