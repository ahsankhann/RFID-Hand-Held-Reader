package com.ahsanshamim.novatexrfid;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.serialport.api.SerialPort;
import android.util.Log;

import com.smartdevicesdk.scanner.ScanGpio;
import com.smartdevicesdk.utils.StringUtility;


/**
 * Created by leoxu on 2017/8/28.
 */

public class UhfHelper {
    private static final String TAG = "UhfHelper";
    static int countRev = 0, countSend = 0;
    static String text;
    int totalSize=0;
    byte[] totalByte=new byte[1024];
    SerialPort mSerialport=new SerialPort();


    private boolean isconnected;

    AutoReadEPCThread autoReadEPCThread;

    ScanGpio scanGpio=new ScanGpio();

    String device;
    int buadRate;
    Handler infoHandler;
    boolean isRead=false;

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    private int sleepTime=60;

    public static int totalSend=0,totalRec=0;

    public UhfHelper(String _device, int _buadRate, Handler _infoHandler){
        device=_device;
        buadRate=_buadRate;
        infoHandler=_infoHandler;

        scanGpio.openPower();
    }

    public void disconnect(){
        isRead=false;
        if(mSerialport!=null){
            mSerialport.closePort();
        }
        mSerialport=null;
        setIsconnected(false);
        scanGpio.closePower();
    }

    public void connect(){
        mSerialport=new SerialPort();
        mSerialport.open(device,buadRate);
        mSerialport.setOnserialportDataReceived(new android.serialport.api.SerialPortDataReceived() {
            @Override
            public void onDataReceivedListener(byte[] buffer, int size) {
                if(totalSize+size>totalByte.length){
                    totalSize=0;
                }
                System.arraycopy(buffer,0,totalByte,totalSize,size);
                totalSize+=size;
                if(totalByte[0]==(byte)0xbb&&totalByte[totalSize-1]==(byte)0x7E){//数据接收完成
                    byte[] recData=new byte[totalSize];
                    System.arraycopy(totalByte,0,recData,0,recData.length);
                    Message msg=infoHandler.obtainMessage();
                    msg.what=1000;
                    msg.obj=recData;
                    infoHandler.sendMessage(msg);

                    totalSize=0;
                    totalByte=new byte[1024];
                }
                Log.e(TAG, "onDataReceivedListener: "+size );
            }
        });
        setIsconnected(true);
    }



    class AutoReadEPCThread extends Thread{
        @Override
        public void run() {
            super.run();
            while (isRead){
                totalSend++;
                mSerialport.WriteNoSleep(new byte[]{(byte) 0xBB, 0x00, 0x22, 0x00, 0x00, 0x22, 0x7E});
                Log.d("AutoReadEPCThread", "run: auto send epc command,sleepTime="+getSleepTime());
                SystemClock.sleep(getSleepTime());
            }
            ////mSerialport.WriteGetData(new byte[]{(byte) 0xBB, 0x00, 0x22, 0x00, 0x00, 0x22, 0x7E});
        }
    }

    /**
     * 自动发送单次轮巡
     * @param flg
     */
    public void setAutoWork(boolean flg){
        if(flg){
            totalSend=0;
            totalRec=0;
            isRead=true;
            autoReadEPCThread=new AutoReadEPCThread();
            autoReadEPCThread.start();
        }else {
            if(autoReadEPCThread!=null&&autoReadEPCThread.isAlive()) {
                isRead=false;
                autoReadEPCThread = null;
            }
        }
    }

    /**
     * 设置Select 参数指令
     * @param target
     * @param action
     * @param memBank
     * @param pointer
     * @param maskLen
     * @param truncated
     * @param mask
     */
    public void setSelectPara(byte target, byte action, byte memBank, int pointer, byte maskLen, boolean truncated, byte[] mask)
    {
        int cmdLen = 14 + maskLen;
        int parameterLen = 7 + maskLen;
        byte[] cmd = new byte[cmdLen];
        cmd[0] = -69;
        cmd[1] = 0;
        cmd[2] = 12;
        cmd[3] = 0;
        cmd[4] = ((byte)parameterLen);
        cmd[5] = ((byte)(target << 5 | action << 2 | memBank));
        cmd[6] = ((byte)(pointer >> 24));
        cmd[7] = ((byte)(pointer >> 16));
        cmd[8] = ((byte)(pointer >> 8));
        cmd[9] = ((byte)(pointer >> 0));
        cmd[10] = maskLen;
        cmd[11] = ((byte)(truncated ? 1 : 0));
        Log.i("select para: ", cmd[5] + " " + pointer + " " + maskLen + " " + StringUtility.ByteArrayToString(mask, maskLen));
        System.arraycopy(mask, 0, cmd, 12, maskLen);

        sendData(getCommand(cmd));
    }

    /**
     * 设置工作地区
     * @param para
     * @return
     */
    public boolean setWorkAreas(int para){
        byte[] buffer=new byte[]{(byte) 0xBB,0x00,0x07,0x00,0x01,0x01,0x09,0x7E};
        buffer[5]=(byte)para;
        boolean flg= sendData(getCommand(buffer));
        if(!flg){
            for(int i=0;i<10;i++){
                flg= sendData(getCommand(buffer));
                if(flg){
                    return flg;
                }
            }
        }
        return flg;
    }

    /**
     * 设置工作信道
     * @param startFrequency
     * @return
     */
    public boolean setWorkChannel(int startFrequency)
    {
        byte[] buffer=new byte[]{(byte) 0xBB,0x00, (byte) 0xAB,0x00,0x01,0x01,0x09,0x7E};
        buffer[5]=(byte)startFrequency;
        boolean flg= sendData(getCommand(buffer));
        if(!flg){
            for(int i=0;i<10;i++){
                flg= sendData(getCommand(buffer));
                if(flg){
                    return flg;
                }
            }
        }
        return flg;
    }

    /**
     * 设置自动跳频
     * @param para
     * @return
     */
    public boolean setWorkAutoFh(int para){
        byte[] buffer=new byte[]{(byte) 0xBB,0x00, (byte) 0xAD,0x00,0x01,0x01,0x09,0x7E};
        buffer[5]=(byte)para;
        return sendData(getCommand(buffer));
    }

    /**
     * 设置发射功率
     * @param para
     * @return
     */
    public boolean setWorkPower(int para){
        byte[] buffer=new byte[]{(byte) 0xBB,0x00, (byte) 0xB6,0x00,0x02,0x07,0x07,0x09,0x7E};
        byte[] paraBt=intToBytes(para);
        buffer[5]=paraBt[2];
        buffer[6]=paraBt[3];
        return sendData(getCommand(buffer));
    }

    /**
     * 设置连续载波
     * @param para
     * @return
     */
    public boolean setContinnousWave(int para){
        byte[] buffer=new byte[]{(byte) 0xBB,0x00, (byte) 0xB0,0x00,0x01,0x00,0x00,0x7E};
        buffer[5]=(byte)para;
        return sendData(getCommand(buffer));
    }

    /**
     * 设置接收解调器参数
     * @param mixer_g
     * @param if_g
     * @param trd
     */
    public boolean setRecvParam(int mixer_g, int if_g, int trd) {
        byte[] cmd = {-69, 0, -16,
                0, 4, 3, 6, 1, -80,
                -82, 126};
        byte[] recv = null;
        byte[] content = null;
        cmd[5] = ((byte) mixer_g);
        cmd[6] = ((byte) if_g);
        cmd[7] = ((byte) (trd / 256));
        cmd[8] = ((byte) (trd % 256));
        return sendData(getCommand(cmd));
    }


    public boolean isConnected() {
        return isconnected;
    }

    public void setIsconnected(boolean isconnected) {
        this.isconnected = isconnected;
    }

    public boolean sendData(byte[] buffer){
        if(buffer!=null&&buffer.length>0) {
            Log.e(TAG, "sendData:,send:"+StringUtility.ByteArrayToString(buffer,buffer.length) );
           byte[]  data= mSerialport.WriteGetData(buffer);
            Log.e(TAG, "sendData,back:"+StringUtility.ByteArrayToString(data,data.length) );
            if(data!=null&&data.length>3&&data[0]==(byte)0xbb&&buffer[2]==data[2]){
                return true;
            }
        }
        return false;
    }
    private void initModelSetting() {
        //设置发射功率
        byte[] bt=getCommand(new byte[]{(byte) 0xBB,0x00, (byte) 0xB6,0x00,0x02,0x0A, (byte) 0x28, (byte) 0x00,0x7E});
        mSerialport.WriteNoSleep(bt);

        //设置接收解调器参数
        bt=getCommand(new byte[]{(byte) 0xBB,0x00, (byte) 0xF0,0x00,0x04,0x02, 0x06, 0x01, (byte) 0xB0, (byte) 0x00,0x7E});
        mSerialport.WriteNoSleep(bt);
    }

    private byte[] getCommand(byte[] buffer){
        buffer[buffer.length-2]=0;
        for(int i=1;i<buffer.length-2;i++){
            buffer[buffer.length-2]+=buffer[i];
        }
        Log.d(TAG, "getCommand: buffer="+StringUtility.ByteArrayToString(buffer,buffer.length));
        return buffer;
    }


    /* int -> byte[] */
    public byte[] intToBytes(int num) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (num >>> (24 - i * 8));
        }

        return b;
    }
}
