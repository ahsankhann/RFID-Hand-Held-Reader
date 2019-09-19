package com.ahsanshamim.novatexrfid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ahsanshamim.novatexrfid.Adapters.Database_Helper;
import com.ahsanshamim.novatexrfid.Adapters.Scan_RecyclerView;
import com.ahsanshamim.novatexrfid.Model.VehicleDetails;
import com.ahsanshamim.novatexrfid.Model.VehicleNo;
import com.ahsanshamim.novatexrfid.Model.Vehicle_Recyclerview;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartdevicesdk.utils.StringUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SacnRFIDActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {

    private final String TAG="SacnRFIDActivity";
    String Url = "http://192.168.96.148:8055/api/transports/";
    AlertDialog alertDialog;
    EditText EdtPosition, EdtJVNo;
    TextView textView_epc, txtAPIStatus;
    Button button_startepc;
    private String username = "";
    private ProgressDialog mProgressDialog;
    private Timer mTimer;
    String serialPort_path="/dev/ttyMT0";
    int serialPort_buad=115200;
    RecyclerView recyclerView;

    UhfHelper UHF;
    ThreadFindChannel threadFindChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sacn_rfid);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        UHF=new UhfHelper(serialPort_path, serialPort_buad,infoHandler);
        initView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        SoundPoolUtil.initSoundPool(this);
        UHF.connect();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                //Setting data
                Object obj= SharedPreferencesUtils.getParam(SacnRFIDActivity.this,"i",0);
                infoHandler.obtainMessage(1021,obj).sendToTarget();

                SystemClock.sleep(500);
                Object selectIndex=SharedPreferencesUtils.getParam( SacnRFIDActivity.this,"j", 0);
                infoHandler.obtainMessage(1022,selectIndex).sendToTarget();
            }
        }).start();




    }

    List<VehicleDetails> VehicleLists = new ArrayList<>();
    List<String> TagList = new ArrayList<>();
    private void Recyclershow(String JsonData){
        if(JsonData != null && JsonData != "") {
            VehicleDetails JsonVeh = new Gson().fromJson(String.valueOf(JsonData), VehicleDetails.class);
            boolean p = false;
            p = TagList.contains(JsonVeh.rfid_tag);
            if(p == false) {
                VehicleLists.add(JsonVeh);
                TagList.add(JsonVeh.rfid_tag);
            }
            recyclerView.setAdapter(new Scan_RecyclerView( VehicleLists, SacnRFIDActivity.this));



        }

    }

    class ThreadFindChannel extends Thread{
        @Override
        public void run() {
            super.run();
            boolean flg=false;
            int i=0,j=0;

            int errNum=10;
            int bestIndexI=0;
            int bestIndexJ=0;


            infoHandler.obtainMessage(1021,bestIndexI).sendToTarget();
            SystemClock.sleep(1000);
            infoHandler.obtainMessage(1022,bestIndexJ).sendToTarget();

            SystemClock.sleep(1000);
            infoHandler.obtainMessage(1023,"").sendToTarget();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UHF.disconnect();
    }

    public Handler infoHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1000://UHF news
                    byte[] totalByte = (byte[]) msg.obj;
                    switch (totalByte[1]) {
                        case (byte) 0x02://通知帧
                            if (totalByte[2] == (byte) 0x22) {
                                UHF.totalRec++;
                                StopScanning();
                                Log.d(TAG, "onDataReceivedListener: Read to EPC");
                                //Read the label EPC
                                byte[] epcArray = new byte[16];
                                System.arraycopy(totalByte, 8, epcArray, 0, epcArray.length);
                                String epcStr = StringUtility.ByteArrayToString(epcArray, epcArray.length);
                                int cpc= totalByte[totalByte.length-3] & 0xff |(totalByte[totalByte.length-4] & 0xff) << 8;

                                IDModels idModels=new IDModels();

                                idModels.setRSSI(totalByte[5]);
                                idModels.setPC(totalByte[6]);
                                idModels.setEPC(epcStr);
                                idModels.setCRC(cpc);

                                String infoStr = epcStr;



                                SoundPoolUtil.play(0);

//                                updateList(idModels);
                                //StopScanning();
                                VehicleDetails veh = new VehicleDetails();
                                ProgressDialogVehicle(true);
                                String[] SplitRFID = infoStr.split(" ");
                                String RFIDCheck = SplitRFID[0].toString() + SplitRFID[1].toString() + SplitRFID[14].toString() + SplitRFID[15].toString();
                                if(RFIDCheck.equals("ABBAABBA")) {
                                    veh.rfid_tag = SplitRFID[10] + SplitRFID[11] + SplitRFID[12] + SplitRFID[13];
                                    veh.rfid_tag_ref = infoStr;
                                    textView_epc.setText(veh.rfid_tag);
                                    ResponseData(veh);
                                }
                                else {
                                    ShowDialogue("Invalid TAG", "Invalid TAG ID. Please Contact to IS Department");
                                    //ShowDialogue("Connecting Error", "Connecting Server Error. Requst TimeOut");
                                    txtAPIStatus.setText("Invalid TAG ID");
                                    txtAPIStatus.setTextColor(Color.RED);
                                    ProgressDialogVehicle(false);
                                }
                            }
                            break;
                        case (byte) 0x01://Response frame
                            String epcStr = StringUtility.ByteArrayToString(totalByte, totalByte.length);
                            switch (totalByte[2]) {
                                case (byte) 0xff:
                                    textView_epc.setText(epcStr);
                                    break;
                                default:
                                    textView_epc.setText(textView_epc.getText() + "\r\n" + epcStr);
                                    break;
                            }
                            break;
                    }
                    break;
                case 1011://Automatic scanning
                    button_startepc.setEnabled(false);

                    threadFindChannel = new SacnRFIDActivity.ThreadFindChannel();
                    threadFindChannel.start();

                    break;
                case 1021://Setting area
                    Log.d(TAG, "handleMessage: 1021 setting area");
                    int i = (int) msg.obj;
                    Log.d(TAG, "handleMessage: 1021,i=" + i);
//                    spinner_workareas.setSelection(i);
                    break;
                case 1022://Setting frequency
                    Log.d(TAG, "handleMessage: 1022 setting frequency");
                    int j = (int) msg.obj;
                    Log.d(TAG, "handleMessage: 1022,j=" + j);
//                    spinner_workchannel.setSelection(j);
                    break;
                case 1023://Complete scan
                    Log.d(TAG, "handleMessage: 1023 complete scan");
                    //spinner_workareas.setEnabled(true);
                    //spinner_workchannel.setEnabled(true);
//                    button_search.setEnabled(true);
//                    button_search.setText("SEARCH");
                    button_startepc.setEnabled(true);
                    button_startepc.performClick();
                    break;
            }
            return false;
        }
    });



    private void initView(){
        textView_epc=(TextView)findViewById(R.id.textView_epc);
        txtAPIStatus = (TextView) findViewById(R.id.apistatustxt);
        EdtPosition = (EditText) findViewById(R.id.Positionedt);
        EdtJVNo = (EditText) findViewById(R.id.JVEdt);
        button_startepc=(Button)findViewById(R.id.button_startepc);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerviewData);
        button_startepc.setOnClickListener(this);
        EdtPosition.setEnabled(false);
        EdtJVNo.setEnabled(false);
        EdtPosition.setText("00");

    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_startepc:
                if (button_startepc.getText().equals("Start")) {
                    button_startepc.setText("Stop");
                    UHF.setAutoWork(true);
                    //lst_idModels=new ArrayList<IDModels>();
                } else {
                    button_startepc.setText("Start");
                    String str = "send：" + UHF.totalSend + "，receive：" + UHF.totalRec;
                    Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
//                    button_search.setEnabled(true);
                    UHF.setAutoWork(false);
                }
                break;


        }
    }

    private void StopScanning(){
        button_startepc.setText("Start");
        String str = "send：" + UHF.totalSend + "，receive：" + UHF.totalRec;
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        UHF.setAutoWork(false);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
        textView_epc.setText("");
        boolean flg=false;
        switch (adapterView.getId()) {
            case R.id.spinner_workareas:
//                spinner_workchannel.setAdapter(myaAdapter);
                break;
            case R.id.spinner_workchannel:
                flg=UHF.setWorkChannel(index);
                break;

        }
        String str=flg?"Successful setup":"Setup failed";
        textView_epc.setText(str);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void ResponseData(VehicleDetails veh){
        if(isNetworkConnected() == false){
            Toast.makeText(getApplicationContext(),"Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
            return;
        }

        if(username.equals("") || username.equals(null)){
            Database_Helper db = new Database_Helper(this);
            Cursor cursor = db.getDataUser();
            if(cursor.moveToLast()){
                username = cursor.getString(0);
            }

        }

        final JSONObject json = new JSONObject();
        try {
            json.put("rfid_tag",  veh.rfid_tag);
            json.put("rfid_tag_ref", veh.rfid_tag_ref);
            json.put("user_cd", username);



        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String str1 = veh.rfid_tag_ref.toString();
        Bundle bundle = new Bundle();
        bundle.putString("json", json.toString());
        final String PutUrl = Url + veh.rfid_tag;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                PutUrl,
                json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        VehicleDetails Veh_rfid = new Gson().fromJson(String.valueOf(response.toString()), VehicleDetails.class);
                        if(Veh_rfid.API_Respone.equals("Data Fetch Sucessfully")){
                            Toast.makeText(getApplicationContext(), "Successfully Scanned TAG", Toast.LENGTH_LONG).show();
                            txtAPIStatus.setText("Successfully Scaned TAG");
                            txtAPIStatus.setTextColor(Color.parseColor("#15b21d"));
                            EdtPosition.setText(Veh_rfid.tag_position);
                            EdtJVNo.setText(Veh_rfid.Vehical_No);
                            Recyclershow(response.toString());
                        }
                        else if (Veh_rfid.API_Respone.equals("Data Not Found")){
                            Toast.makeText(getApplicationContext(), "Scanned TAG not Register", Toast.LENGTH_LONG).show();
                            txtAPIStatus.setText("Scanned TAG not Register");
                            txtAPIStatus.setTextColor(Color.RED);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Failed Scaned TAG", Toast.LENGTH_LONG).show();
                            ShowDialogue("Server Error", "RFID TAG Server Not Response");
                            Toast.makeText(getApplicationContext(), str1.toString() , Toast.LENGTH_LONG).show();
                            txtAPIStatus.setText("Failed Scaned TAG");
                            txtAPIStatus.setTextColor(Color.RED);
                        }
                        ProgressDialogVehicle(false);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Failed to response", Toast.LENGTH_LONG).show();
                        ShowDialogue("Connecting Error", "Connecting Server Error. Please Contact to IS Department");
                        //ShowDialogue("Connecting Error", "Connecting Server Error. Requst TimeOut");
                        txtAPIStatus.setText("Connecting Server Error");
                        txtAPIStatus.setTextColor(Color.RED);
                        ProgressDialogVehicle(false);
                    }
                }

        );
        requestQueue.add(request);
    }

    private void ShowDialogue(String Title, String Error){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Title);
        builder.setMessage(Error);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }





    private void ProgressDialogVehicle(boolean isShow)
    {
        if(mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("Information");
            mProgressDialog.setMessage("Searching RFID TAG Details...");
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

    private void stopProgressTimer()
    {
        if(mTimer != null)
        {
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = null;
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
                                    ErrorResponse("Failed result : "+"Try open timeout");
                                }
                            });
                        }

                    }
                }, 15000);
    }

    private void ErrorResponse(String Error){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Error");
        builder.setMessage(Error+" Please Contact to IS Department");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                alertDialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


}
