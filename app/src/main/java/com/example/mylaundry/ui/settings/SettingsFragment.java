package com.example.mylaundry.ui.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mylaundry.BookActivity;
import com.example.mylaundry.BookItemAdapter;
import com.example.mylaundry.Booking;
import com.example.mylaundry.MachineListAdapter;
import com.example.mylaundry.MainActivity;
import com.example.mylaundry.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.N)
public class SettingsFragment extends Fragment{
    private RecyclerView bookingRecyclerView;
    private BookItemAdapter bookingAdapter;
    private RecyclerView.LayoutManager bookingLayoutManager;
    private TabLayout bookingsTab;
    private ArrayList<Booking> dbBookingList = new ArrayList<>();
    String TODAY = "";
    FirebaseFirestore db;
    private SettingsViewModel settingsViewModel;
    private MainActivity main;
    private Date currentTime = null;
    private DateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @SuppressLint("ResourceType")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_settings, container, false);
        getCurrentTime();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        TODAY = dateFormat.format(date);
        db = FirebaseFirestore.getInstance();
        bookingsTab = root.findViewById(R.id.tabLayout);
        getUpcomingBookings(root);

        handlePopup(root);

        bookingsTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0){
                    getUpcomingBookings(root);
                } else {
                    getPreviousBookings(root);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        handleClick(root);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((Activity) root.getContext()).getWindow().setStatusBarColor(ContextCompat.getColor(root.getContext(), R.color.colorAccent));
        }
        return root;
    }

    private void getUpcomingBookings(View root){
        dbBookingList.clear();
        db.collection("bookings").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            List<DocumentSnapshot> tempBookings = queryDocumentSnapshots.getDocuments();
                            Date bookingDate = null;
                            Date todayDate = null;
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                            try {
                                todayDate = sdf.parse(TODAY);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            for (DocumentSnapshot d: tempBookings){
                                Booking pulledBooking = d.toObject(Booking.class);
                                pulledBooking.setId(d.getId());
                                try {
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat bookFormat = new SimpleDateFormat("dd/MM/yyyy");
                                    bookingDate = bookFormat.parse(pulledBooking.getDate());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (bookingDate != null && !(bookingDate.before(todayDate))){ // TODO: user check here after implementing user authent.
                                    Date bookingTime = null;
                                    try {
                                        bookingTime = timeFormat.parse(pulledBooking.getEndHr() + ":" + pulledBooking.getMinute());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if (pulledBooking.getDate().equals(TODAY) && bookingTime.after(currentTime)){
                                        dbBookingList.add(pulledBooking);
                                    } else if (!pulledBooking.getDate().equals(TODAY)){
                                        dbBookingList.add(pulledBooking);
                                    }
                                }                            }
                            bookingAdapter.notifyDataSetChanged();
                        }
                    }
                });


        bookingRecyclerView = root.findViewById(R.id.bookings);
        bookingLayoutManager = new LinearLayoutManager(getContext());
        bookingAdapter = new BookItemAdapter(dbBookingList);

        bookingRecyclerView.setLayoutManager(bookingLayoutManager);
        bookingRecyclerView.setAdapter(bookingAdapter);
        handleClick(root);
    }

    private void getPreviousBookings(View root){
        dbBookingList.clear();
        db.collection("bookings").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            List<DocumentSnapshot> tempBookings = queryDocumentSnapshots.getDocuments();
                            Date bookingDate = null;
                            Date todayDate = null;
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                            try {
                                todayDate = sdf.parse(TODAY);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            for (DocumentSnapshot d: tempBookings){
                                Booking pulledBooking = d.toObject(Booking.class);
                                try {
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat bookFormat = new SimpleDateFormat("dd/MM/yyyy");
                                    bookingDate = bookFormat.parse(pulledBooking.getDate());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (bookingDate != null && !(bookingDate.after(todayDate))){ // TODO: user check here after implementing user authent.
                                    Date bookingTime = null;
                                    try {
                                        bookingTime = timeFormat.parse(pulledBooking.getEndHr() + ":" + pulledBooking.getMinute());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if (pulledBooking.getDate().equals(TODAY) && bookingTime.before(currentTime)){
                                        dbBookingList.add(pulledBooking);
                                    } else if (!pulledBooking.getDate().equals(TODAY)){
                                        dbBookingList.add(pulledBooking);
                                    }                                }                            }
                            bookingAdapter.notifyDataSetChanged();
                        }
                    }
                });


        bookingRecyclerView = root.findViewById(R.id.bookings);
        bookingLayoutManager = new LinearLayoutManager(getContext());
        bookingAdapter = new BookItemAdapter(dbBookingList);

        bookingRecyclerView.setLayoutManager(bookingLayoutManager);
        bookingRecyclerView.setAdapter(bookingAdapter);
    }

    private void getCurrentTime(){
        currentTime = Calendar.getInstance().getTime();
        String temp = timeFormat.format(currentTime);
        try {
            currentTime = timeFormat.parse(temp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void handlePopup(View root){
        final ImageButton imgButton = (ImageButton) root.findViewById(R.id.settingsButton);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), imgButton);
                popup.getMenuInflater().inflate(R.menu.popup_user_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()){
                            case R.id.profile:
                                //TODO: open a new activity
                                return true;
                            case R.id.logout:
                                //TODO: logout this user
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });

    }

    private void handleClick(final View root){
        bookingAdapter.setOnItemClickListener(new BookItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                final AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Alert")
                        .setMessage("Would you like to view the booking pin-code or delete this booking?")
                        .setPositiveButton("View Pin-Code", null)
                        .setNegativeButton("Delete Booking", null)
                        .setNeutralButton("Cancel", null)
                        .setCancelable(false)
                        .create();

                dialog.show();

                Button codeBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                codeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                Button delBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                delBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id = dbBookingList.get(position).getId();
                        db.collection("bookings").document(id).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), "Successfully Deleted", Toast.LENGTH_LONG).show();
                                        getUpcomingBookings(root);
                                        dialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    }
                                });
                    }
                });

            }
        });
    }

}