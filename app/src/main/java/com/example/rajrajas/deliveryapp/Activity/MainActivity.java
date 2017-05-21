package com.example.rajrajas.deliveryapp.Activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rajrajas.deliveryapp.DbController;
import com.example.rajrajas.deliveryapp.LocationDetector;
import com.example.rajrajas.deliveryapp.Model.ListItem;
import com.example.rajrajas.deliveryapp.Model.ListItemAdapter;
import com.example.rajrajas.deliveryapp.R;
import com.example.rajrajas.deliveryapp.databinding.ActivityMainBinding;
import com.example.rajrajas.deliveryapp.dummy;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private RecyclerView item_recycler_view, completed_list_view;
    List<ListItem> data, completed_date;

    private LocationDetector locationDetector;
    private Timer timer;
    private DbController dbController;
    private Boolean order_low_to_high = false;
    private TreeMap<Double, String> sortinglist;
    private ListItemAdapter completedListAdapter;
    private Paint p = new Paint();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        item_recycler_view = (RecyclerView) findViewById(R.id.item_list);
        completed_list_view = (RecyclerView) findViewById(R.id.completed_item_list);

        dummy.db_context = getApplicationContext();
        dbController = new DbController(dummy.db_context);

        data = dbController.get_ConsigneeDetails(2);
        initRecycler(item_recycler_view, "Not Delivered List", data);

        completed_date = dbController.get_ConsigneeDetails(1);
        initRecycler(completed_list_view, "Completed List", completed_date);
    }

    //TODO Intialize the two recycerview.one for pending list and another one for Completed list.
    //TODO pending list's item onclick will open the view activity
    private void initRecycler(RecyclerView recyclerView, final String status, final List<ListItem> tdata) {
        recyclerView.setHasFixedSize(true);
        if (status.equals("Not Delivered List"))
            recyclerView.setAdapter(new ListItemAdapter(MainActivity.this, tdata, status));
        else {
            completedListAdapter = new ListItemAdapter(MainActivity.this, tdata, status);
            recyclerView.setAdapter(completedListAdapter);
            initSwipe();
        }

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(MainActivity.this, recyclerView, new ClickListener() {
            @Override
            public void onClick(final View view, final int position) {
                if (status.equals("Not Delivered List"))
                {
                    ListItem l = tdata.get(position);
                    timer.cancel();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    Intent intent = new Intent(MainActivity.this, ViewActivity.class);
                    intent.putExtra("name", l.getName());
                    intent.putExtra("Description", l.getDescription());
                    intent.putExtra("Status", status);
                    intent.putExtra("lat", l.getLat() + "");
                    intent.putExtra("lon", l.getLon() + "");
                    intent.putExtra("Address", l.getAddress());
                    intent.putExtra("Id", l.getConsigneeId() + "");
                    MainActivity.this.startActivity(intent);
                }

            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        if (status.equals("Not Delivered List")) {
            timer = new Timer();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            timer.schedule(new location_detector_timer(), 0, 10000);
        }

    }

    //TODO floating action bar button onclick listener. calls the add activity
    public void fab_click(View view) {
        timer.cancel();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        MainActivity.this.startActivity(intent);
    }

    //TODO TimerTask class used to find the Km between the user location and Consignee Location for every 10 seconds
    private class location_detector_timer extends TimerTask {
        public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    locationDetector = new LocationDetector(getApplicationContext());
                    ArrayList<String> locationDetectorResult = null;
                    try {
                        locationDetectorResult = locationDetector.getting_current_location();
                        calculate_distance(locationDetectorResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });

        }
    }

    //TODO Respective actions will take based on the result returns from locationDetector
    void calculate_distance(ArrayList<String> result) {
        switch (result.get(0)) {
            case "On your GPS":
                timer.cancel();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                buildAlertMessageNoGps();
                break;
            case "Request Permission":
                timer.cancel();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                request_permission();
                break;
            case "Location Found": {
                sortinglist = new TreeMap<Double, String>();
                for (int i = 0; i < item_recycler_view.getAdapter().getItemCount(); i++) {
                    RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) item_recycler_view.findViewHolderForAdapterPosition(i);
                    Double dist = locationDetector.distance(data.get(i).getLat(), data.get(i).getLon(), Double.parseDouble(result.get(1)), Double.parseDouble(result.get(2)));
                    sortinglist.put(dist, data.get(i).getConsigneeId() + "");
                    try {
                        DecimalFormat df = new DecimalFormat("#.##");
                        df.setRoundingMode(RoundingMode.FLOOR);
                        ((TextView) holder.itemView.findViewById(R.id.distance_text)).setText(df.format(dist) + " kms");
                    } catch (Exception ignored) {

                    }

                }

            }
            break;
            case "Location Not Found":
                Snackbar.make(findViewById(R.id.main_container), "Unable to find the Current Location", Snackbar.LENGTH_SHORT).show();
                break;

        }

    }

    //todo from Marshmallow version permissions are maded dynimacally.Request permission at run time only.
    @TargetApi(Build.VERSION_CODES.M)
    public void request_permission() {
        requestPermissions(dummy.PermissionsLocation, dummy.RequestLocationId);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        timer = new Timer();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timer.schedule(new location_detector_timer(), 0, 10000);
    }

    //TODO stopping the timer which used to measure the distance for every 10 seconds
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        timer.cancel();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //TODO Re intializing the recycler views
    @Override
    public void onResume() {
        super.onResume();
        timer.cancel();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        data = dbController.get_ConsigneeDetails(2);
        initRecycler(item_recycler_view, "Not Delivered List", data);

        synchronized(item_recycler_view.getAdapter()) {
            item_recycler_view.getAdapter().notifyDataSetChanged();
        }

        completed_date = dbController.get_ConsigneeDetails(1);
        initRecycler(completed_list_view, "Completed List", completed_date);

        synchronized(completed_list_view.getAdapter()) {
            completed_list_view.getAdapter().notifyDataSetChanged();
        }

    }

    //TODO stopping the timer which used to measure the distance for every 10 seconds
    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //TODO stopping the timer which used to measure the distance for every 10 seconds
    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    //TODO Recyclerview Item view touch listener class
    private class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }


    }

    //TODO Triggers when GPS of the user phone is off
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        MainActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        timer = new Timer();
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        timer.schedule(new location_detector_timer(), 0, 10000);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    static interface ClickListener {
        public void onClick(View view, int position);

        public void onLongClick(View view, int position);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //TODO Order by the distance(Both Ascending and Descending)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        timer.cancel();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        switch (id) {
            case R.id.order:
                if (!order_low_to_high) {
                    item.setIcon(getResources().getDrawable(R.mipmap.order_high_low));
                    order_low_to_high = true;

                    data = new ArrayList<>();

                    for (Map.Entry<Double, String> doubleStringEntry : sortinglist.entrySet()) {
                        data.add(dbController.get_ConsigneeDetails(doubleStringEntry.getValue()));
                    }

                    timer.cancel();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    initRecycler(item_recycler_view, "Not Delivered List", data);

                } else {
                    item.setIcon(getResources().getDrawable(R.mipmap.order_low_high));
                    order_low_to_high = false;

                    TreeMap<Double, String> descendingList = sortinglist;
                    data = new ArrayList<>();


                    while (!descendingList.isEmpty()) {
                        Map.Entry<Double, String> e = descendingList.pollLastEntry();
                        data.add(dbController.get_ConsigneeDetails(e.getValue()));
                    }

                    timer.cancel();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    initRecycler(item_recycler_view, "Not Delivered List", data);
                }


        }

        return super.onOptionsItemSelected(item);
    }

    //TODO Swipe listener for the completed list for deleting the item
    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();


                if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT)
                {
                    dbController.delete_consigneedetails(completed_date.get(position).getConsigneeId());
                    completedListAdapter.delete(position,"Completed List");
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.delete);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.delete);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(completed_list_view);
    }

}
