package com.ahsanshamim.novatexrfid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by leoxu on 2017/8/29.
 */

public class DiySpinnerAdapter extends BaseAdapter {
    private List<SpinnerItem> mList;
    private Context mContext;

    public DiySpinnerAdapter(Context pContext, List<SpinnerItem> pList) {
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

    public  List<SpinnerItem> getList(){
        return mList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater _LayoutInflater = LayoutInflater.from(mContext);
        convertView = _LayoutInflater.inflate(android.R.layout.simple_spinner_item, null);
        if (convertView != null) {
            TextView text1 = (TextView) convertView
                    .findViewById(android.R.id.text1);

            text1.setText(mList.get(position).getValue());
        }
        return convertView;
    }
}
