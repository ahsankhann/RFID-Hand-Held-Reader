package com.ahsanshamim.novatexrfid.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahsanshamim.novatexrfid.Model.VehicleDetails;
import com.ahsanshamim.novatexrfid.Model.Vehicle_Recyclerview;
import com.ahsanshamim.novatexrfid.R;

import java.util.List;

public class Scan_RecyclerView extends RecyclerView.Adapter<Scan_RecyclerView.MyVieHolder> {
    VehicleDetails[] ArrayVehicle;
    List<VehicleDetails> vehicleList;
    Context context;

    public Scan_RecyclerView(List<VehicleDetails> vehicleList, Context context) {
        this.ArrayVehicle = vehicleList.toArray(new VehicleDetails[vehicleList.size()]);
        vehicleList = vehicleList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.rc_scantag, viewGroup, false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder myVieHolder, int i) {
        final VehicleDetails vehicle = ArrayVehicle[i];
        myVieHolder.vehicleNo.setText(vehicle.Vehical_No);
        myVieHolder.vehiclePos.setText("Pos: " + vehicle.tag_position);
    }



    @Override
    public int getItemCount() {
        return ArrayVehicle.length;
    }

    public static class MyVieHolder extends RecyclerView.ViewHolder{
        TextView vehicleNo;
        TextView vehiclePos;
        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            vehicleNo = (TextView) itemView.findViewById(R.id.vehicleRC);
            vehiclePos = (TextView) itemView.findViewById(R.id.positionRC);
        }
    }
}
