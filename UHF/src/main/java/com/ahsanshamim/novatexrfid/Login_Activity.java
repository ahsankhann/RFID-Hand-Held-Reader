package com.ahsanshamim.novatexrfid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ahsanshamim.novatexrfid.Adapters.Database_Helper;
import com.ahsanshamim.novatexrfid.Model.login_users;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.smartdevicesdk.database.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class Login_Activity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;
    private Timer mTimer;
    AlertDialog alertDialog;
    Button btnLogin;
    EditText edtUsername, edtPassword;
    String Url = "http://192.168.96.148:8080/api/LoginQRCode";
    Database_Helper adptr;
    int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        edtUsername = (EditText) findViewById(R.id.usernameedt);
        edtPassword = (EditText) findViewById(R.id.passwordedt);

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!edtUsername.getText().toString().equals("") && !edtPassword.getText().toString().equals("")){
                    showProgressDialog(true);
                    edtUsername.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    edtPassword.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    LoginData();
                }
            }
        });

        edtPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    if(!edtUsername.getText().toString().equals("") && !edtPassword.getText().toString().equals("")){
                        showProgressDialog(true);
                        LoginData();
                        return  true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoginAuto();
    }

    private void LoginData(){
        final JSONObject json = new JSONObject();
        try {
            json.put("username",  edtUsername.getText().toString());
            json.put("password",  edtPassword.getText().toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Bundle bundle = new Bundle();
        bundle.putString("json", json.toString());
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Url,
                json,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        login_users JsonUser = new Gson().fromJson(String.valueOf(response), login_users.class);
                        if(JsonUser.Status_API.equals( "Not Found")){
                            ErrorResponse("Username & Password Incorrect");
                        }
                        else if(JsonUser.Status_API.equals("Found")) {

                            adptr = new Database_Helper(Login_Activity.this);
                            Boolean result = adptr.InsertUsers(JsonUser.username, JsonUser.password, JsonUser.fullname);
                            if(result){

                                Intent IT = new Intent(Login_Activity.this, HomeScreen_Activity.class);
                                IT.putExtra("Username", JsonUser.fullname);
                                IT.putExtra("userid", JsonUser.username);
                                startActivity(IT);
                                finish();
                                edtUsername.setText("");
                                edtPassword.setText("");
                                Toast.makeText(getApplicationContext(), "WELCOME Mr. " + JsonUser.fullname, Toast.LENGTH_LONG).show();
                            }
                            else {
                                ErrorResponse("Mobile Application Error.");
                            }
                        }
                        showProgressDialog(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showProgressDialog(false);
                        ErrorResponse("Username & Password Incorrect");
                    }
                }
        );
        requestQueue.add(request);
    }

    private boolean mIsOpened = false;
    private void showProgressDialog(boolean isShow)
    {
        if(mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("Login");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setCancelable(false);
        }

        stopProgressTimer();
        if(isShow)
        {
            mProgressDialog.show();
            startProgressTimer();
        }
        else mProgressDialog.dismiss();
    }

    private void startProgressTimer()
    {
        mTimer = new Timer();
        mTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if(mProgressDialog!=null && mProgressDialog.isShowing())
                        {
                            mProgressDialog.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ErrorResponse("Failed result : Try open timeout");
                                }
                            });
                        }

                    }
                }, 5000);
    }

    private void stopProgressTimer()
    {
        if(mTimer != null)
        {
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = null;
    }

    private void ErrorResponse(String Error){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eroor");
        builder.setMessage(Error + " Please Contact to IS Department");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void LoginAuto(){
        showProgressDialog(true);
        Database_Helper db = new Database_Helper(this);
        Cursor cursor =db.getDataUser();
        if(cursor.moveToFirst()){
            Intent IT = new Intent(Login_Activity.this, HomeScreen_Activity.class);
            IT.putExtra("Username", cursor.getString(2));
            IT.putExtra("userid", cursor.getString(0));
            startActivity(IT);
            finish();

            Toast.makeText(getApplicationContext(), "WELCOME Mr. " +cursor.getString(2) + cursor.getString(0), Toast.LENGTH_LONG).show();

        }
        showProgressDialog(false);
    }



}
