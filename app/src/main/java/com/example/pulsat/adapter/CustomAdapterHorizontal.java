package com.example.pulsat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.pulsat.R;
import com.example.pulsat.activity.MainActivity;
import com.example.pulsat.event.RdvActionEvent;
import com.example.pulsat.model.DayRdv;
import com.example.pulsat.model.Rdv;
import com.example.pulsat.viewmodel.RdvViewModel;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CustomAdapterHorizontal extends RecyclerView.Adapter<ViewHolderHorizontal> {

    final static String TAG = "CUSTOMADAPTERHORIZONTAL";
    private List<DayRdv> dayRdvList;
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    @Override
    public ViewHolderHorizontal onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_h, viewGroup, false);
        return new ViewHolderHorizontal(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolderHorizontal holder, int position) {
        DayRdv dayRdv = dayRdvList.get(position);
        holder.getDate().setText(dayRdv.getDay());
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(
                        holder
                        .getChildRecyclerview()
                        .getContext(),
                LinearLayoutManager.VERTICAL,
                false);
        layoutManager
                .setInitialPrefetchItemCount(
                        dayRdv
                        .getRdvList()
                        .size());
        CustomAdapter customAdapter = new CustomAdapter();

        holder.getChildRecyclerview().setLayoutManager(layoutManager);
        holder.getChildRecyclerview().setHasFixedSize(true);
        holder.getChildRecyclerview().setAdapter(customAdapter);
        holder.getChildRecyclerview().setRecycledViewPool(viewPool);
        customAdapter.updateRdvsList(dayRdv.getRdvList());
        customAdapter.notifyDataSetChanged();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // this method is called
                // when the item is moved.
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int positionRdv = viewHolder.getAdapterPosition();
                int positionList = holder.getAdapterPosition();

                DayRdv dayRdv1 = dayRdvList.get(positionList);
                Rdv deletedRdv = dayRdv1.getRdvAtPos(positionRdv);
                EventBus.getDefault().post(new RdvActionEvent(deletedRdv,"delete"));

                // below line is to display our snackbar with action.
                Snackbar.make(holder.getChildRecyclerview(), deletedRdv.getAddress(), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new RdvActionEvent(deletedRdv,"insert"));
                    }
                }).show();

            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(holder.getChildRecyclerview());

    }

    @Override
    public int getItemCount() {
        if (dayRdvList != null) {
            return dayRdvList.size();
        }else{ return 0;}
    }

    public void updateDatesList(List<Rdv> allRdvsList) {
        List<DayRdv> newDayRdvList = new ArrayList<>();
        List<Rdv> rdvByDate = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE d MMM");
        String date = formatter.format(new Date(System.currentTimeMillis()));
        String newDate ;

        for (Rdv rdv : allRdvsList ){
            newDate = rdv.getDate();
            if (newDate.equals(date)){
                rdvByDate.add(rdv);
            }else{
                newDayRdvList.add(new DayRdv(date,rdvByDate));
                rdvByDate = new ArrayList<>();
                date = newDate;
                rdvByDate.add(rdv);
            }
        }
        newDayRdvList.add(new DayRdv(date,rdvByDate));

        this.dayRdvList = newDayRdvList;
        this.dayRdvList.removeAll(Collections.singleton(null));

    }

}
