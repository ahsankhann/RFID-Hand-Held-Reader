package com.ahsanshamim.novatexrfid;

/**
 * Created by leoxu on 2017/11/15.
 */

public class IDModels {
    public Object getRSSI() {
        return RSSI;
    }

    public void setRSSI(Object RSSI) {
        this.RSSI = RSSI;
    }

    public Object getPC() {
        return PC;
    }

    public void setPC(Object PC) {
        this.PC = PC;
    }

    public Object getEPC() {
        return EPC;
    }

    public void setEPC(Object EPC) {
        this.EPC = EPC;
    }

    public Object getCRC() {
        return CRC;
    }

    public void setCRC(Object CRC) {
        this.CRC = CRC;
    }

    private Object RSSI;
    private Object PC;
    private Object EPC;
    private Object CRC;

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
    }

    private int Number;
}
