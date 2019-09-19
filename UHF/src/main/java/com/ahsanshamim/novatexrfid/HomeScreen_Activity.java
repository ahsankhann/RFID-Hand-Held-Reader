package com.ahsanshamim.novatexrfid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ahsanshamim.novatexrfid.Adapters.Database_Helper;
import com.smartdevicesdk.database.DatabaseHelper;

import java.util.Timer;
import java.util.TimerTask;

public class HomeScreen_Activity extends AppCompatActivity {

    AlertDialog alertDialog;
    String username = "";
    Database_Helper db;
    android.support.v7.widget.GridLayout gridLayout;
    TextView txtName, txtUserId, txtLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        gridLayout = (android.support.v7.widget.GridLayout)findViewById(R.id.mainGrid);
        txtName = (TextView) findViewById(R.id.txtDashboard);
        txtUserId = (TextView) findViewById(R.id.txtuserid);
        txtLogout = (TextView) findViewById(R.id.logouttxt);
        Bundle extras = getIntent().getExtras();
        if(extras.getString("Username").toString().length() > 14)
            txtName.setText(extras.getString("Username").substring(0, 14).toString());
        else
            txtName.setText(extras.getString("Username").toString());
        txtUserId.setText(extras.getString("userid"));
        setsingleClick(gridLayout);

        txtLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Database_Helper db = new Database_Helper(HomeScreen_Activity.this);
                db.deleteTitle();
                finish();
                System.exit(0);
            }
        });


    }

    private void setsingleClick(GridLayout gridLayout) {
        for (int i=0; i< gridLayout.getChildCount(); i++){
            CardView cardView = (CardView)gridLayout.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentActivity(finalI);
                }
            });
        }
    }

    private void IntentActivity(int i){
        switch (i){
            case 0:
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkProduction = AccessGet("PQR1");
                if(checkProduction) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
                else {
                    ErrorResponse("Access Denied");
                }
                break;

            case 1:
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkScan = AccessGet("PQR1");
                if(checkScan) {
                    startActivity(new Intent(getApplicationContext(), SacnRFIDActivity.class));
                }
                else {
                    ErrorResponse("Access Denied");
                }
                break;
            default:
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(getApplicationContext(), "No Action Found", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private Boolean AccessGet(String ObjectCode){
        if (username.equals("")){
            db = new Database_Helper(this);
            Cursor cursor = db.getDataUser();
            if(cursor.moveToLast()){
                username = cursor.getString(0);
            }
        }

        db = new Database_Helper(this);
        Cursor cursor = db.GetAccess(username, ObjectCode);
        if(cursor.moveToFirst()){
            return true;
        }

        return true;

    }
    private void ErrorResponse(String Error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
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

}
