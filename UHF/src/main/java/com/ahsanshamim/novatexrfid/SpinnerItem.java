package com.ahsanshamim.novatexrfid;

/**
 * Created by leoxu on 2017/8/28.
 */

public class SpinnerItem {
    private int ID;
    private double Para;
    private String Value = "";

    public SpinnerItem() {
        ID = 0;
        Value = "";
    }
    public SpinnerItem(int _ID,String _Value) {
        ID = _ID;
        Value = _Value;
    }

    public SpinnerItem(int _ID,double _para, String _Value) {
        ID = _ID;
        Para=_para;
        Value = _Value;
    }

    @Override
    public String toString() {
        //
        //Why rewrite toString()? Because the adapter is displaying data, if the object passed to the adapter is not a string, use the object directly..toString()
        // TODO Auto-generated method stub
        return Value;
    }

    public int getID() {
        return ID;
    }
    public double getPara() {
        return Para;
    }

    public String getValue() {
        return Value;
    }
}
