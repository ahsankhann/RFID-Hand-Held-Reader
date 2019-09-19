package com.ahsanshamim.novatexrfid;

import com.google.gson.annotations.SerializedName;

public class Vehicl_RFID {

    @SerializedName("API_Respone")
    public String Api_Status;
    @SerializedName("id")
    public int id;
    @SerializedName("scan_dt")
    public String scan_dt;
    @SerializedName("veh_no")
    public  String scan_veh;
}
