package com.ahsanshamim.novatexrfid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ahsanshamim.novatexrfid.R;

import java.util.List;

/**
 * Created by leoxu on 2017/8/29.
 */

public class ListViewEPCAdapter extends BaseAdapter {
    private List<IDModels> mList;
    private Context mContext;

    public ListViewEPCAdapter(Context pContext, List<IDModels> pList) {
        this.mContext = pContext;
        this.mList = pList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public  List<IDModels> getList(){
        return mList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater _LayoutInflater = LayoutInflater.from(mContext);
        convertView = _LayoutInflater.inflate(R.layout.item_list_epc, null);
        if (convertView != null) {
            TextView text_rssi = (TextView) convertView
                    .findViewById(R.id.text_rssi);
            TextView text_epc = (TextView) convertView
                    .findViewById(R.id.text_epc);
            TextView text_number = (TextView) convertView
                    .findViewById(R.id.text_number);

            text_rssi.setText(mList.get(position).getRSSI()+"");
            text_epc.setText(mList.get(position).getEPC().toString().replace(" ","")+"");
            text_number.setText(mList.get(position).getNumber()+"");

        }
        return convertView;
    }
}
