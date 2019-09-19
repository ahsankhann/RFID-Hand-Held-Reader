package com.ahsanshamim.novatexrfid.Model;

import com.google.gson.annotations.SerializedName;

public class VehicleDetails {
    @SerializedName("API_Respone")
    public String API_Respone;
    @SerializedName("rfid_tag")
    public String rfid_tag;
    @SerializedName("scan_dt")
    public String scan_dt;
    @SerializedName("veh_no")
    public String Vehical_No;
    @SerializedName("tag_position")
    public String tag_position;
    @SerializedName("rfid_tag_ref")
    public String rfid_tag_ref;
    @SerializedName("user_cd")
    public String user_cd;

}
