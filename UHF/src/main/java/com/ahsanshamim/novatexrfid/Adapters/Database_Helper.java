package com.ahsanshamim.novatexrfid.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;
import java.util.Date;

public class Database_Helper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Ahsan.db";
    private static final String TABLE_NAME = "Settings";
    private static final String TABLE_NAME_Login = "Users";
    private static final String TABLE_NAME_Access = "AccessList";
    private String KEY_USERNAME = "Username";
    public Database_Helper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create Table " + TABLE_NAME + " (ID TEXT, Mobile_Name TEXT, Device_Setting TEXT)");
        db.execSQL("Create Table " + TABLE_NAME_Login + "(Username TEXT, Password TEXT, Fullname TEXT, LoginTime TEXT)");
        db.execSQL("Create Table " + TABLE_NAME_Access + "(Username TEXT, ObjectCode TEXT, ObjectName TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop Table If Exists " + TABLE_NAME);
        db.execSQL("Drop Table If Exists " + TABLE_NAME_Login);
        db.execSQL("Drop Table If Exists " + TABLE_NAME_Access);
        onCreate(db);
    }

    public Boolean InsertData(String id, String Mobile_Name, String Device_Name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues CV = new ContentValues();
        CV.put("ID", id);
        CV.put("Mobile_Name", Mobile_Name);
        CV.put("Device_Setting", Device_Name);

        long result = db.insert(TABLE_NAME, null, CV);
        db.close();
        if(result == -1)
            return false;
        else
            return true;

    }

    public Cursor getData(String Mobile_Info){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * from " + TABLE_NAME + " where ID = '" + Mobile_Info + "' ";
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

    public Boolean InsertUsers(String Username, String Password, String Fullname){
        Date currentTime = Calendar.getInstance().getTime();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues CV = new ContentValues();
        CV.put("Username", Username);
        CV.put("Password", Password);
        CV.put("Fullname", Fullname);
        CV.put("LoginTime", String.valueOf(currentTime));

        long result = db.insert(TABLE_NAME_Login, null, CV);
        db.close();
        if(result == -1)
            return false;
        else
            return true;

    }

    public Cursor getDataUser(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * from " + TABLE_NAME_Login;
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

    public boolean CountLogin(){
        /*SQLiteDatabase db = this.getWritableDatabase();
        String count = "SELECT count(*) FROM " + TABLE_NAME_Login;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        if(icount>0){
            db.close();
            return true;
        }
        db.close();
        return false;*/

        SQLiteDatabase db = this.getWritableDatabase();
        boolean empty = true;
        Cursor cur = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME_Login, null);
        if (cur != null && cur.moveToFirst()) {
            empty = (cur.getInt (0) == 0);
            return true;
        }
        cur.close();

        return false;
    }

    public void deleteTitle()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME_Login);
    }


    public Boolean InsertAccess(String Username, String ObjectCode, String ObjectName){
        Date currentTime = Calendar.getInstance().getTime();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues CV = new ContentValues();
        CV.put("Username", Username);
        CV.put("ObjectCode", ObjectCode);
        CV.put("ObjectName", ObjectName);


        long result = db.insert(TABLE_NAME_Access, null, CV);
        db.close();
        if(result == -1)
            return false;
        else
            return true;

    }

    public void deleteAccess()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME_Access);
    }

    public Cursor GetAccess(String Username, String ObjectCode){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * from " + TABLE_NAME_Access + " where Username = '" + Username + "' AND ObjectCode = '" + ObjectCode + "' ";
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

}
