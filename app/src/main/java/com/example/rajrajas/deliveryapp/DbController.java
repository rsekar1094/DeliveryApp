package com.example.rajrajas.deliveryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.rajrajas.deliveryapp.Model.ListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajrajas on 5/18/2017.
 */

public class DbController extends SQLiteOpenHelper {
    public DbController(Context applicationcontext) {
        super(applicationcontext, "dbapp_new.db", null, 1);
    }

    //Creates Tables
    @Override
    public void onCreate(SQLiteDatabase database) {

        String query;

        //Table Id: 0
        query = "CREATE TABLE ConsigneeDetails (ConsigneeId INTEGER,ConsigneeName TEXT,ConsigneeDescription TEXT,ConsigneeLatitude TEXT,ConsigneeLongitude TEXT,ConsigneeAddress TEXT,ConsigneeStatusId INTEGER)";
        database.execSQL(query);
        insert_Default_ConsigneeDetails(database);

        //Table Id: 1
        query = "CREATE TABLE ConsigneeStatus (ConsigneeStatusId INTEGER,ConsigneeStatusName TEXT)";
        database.execSQL(query);
        insert_Default_ConsigneeStatus(database);

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        String query;
        query = "DROP TABLE IF EXISTS ConsigneeDetails";
        database.execSQL(query);

        query = "DROP TABLE IF EXISTS ConsigneeStatus";
        database.execSQL(query);
    }

    //TODO inserting the Consignee Status. 1- Success 2- Pending
    private void insert_Default_ConsigneeStatus(SQLiteDatabase database) {
        String name[] = {"Success", "Pending"};

        ContentValues values = new ContentValues();

        for (int i = 0; i < name.length; i++) {
            values.put("ConsigneeStatusId", i + 1);
            values.put("ConsigneeStatusName", name[i]);
            database.insert("ConsigneeStatus", null, values);
        }
    }

    //TODO Inserting the first set of records in ConsigneeDetails table
    private void insert_Default_ConsigneeDetails(SQLiteDatabase database) {
        String name[] = {"Raja", "Gowtham", "Amal", "Manju"};
        String Description[] = {"Deliver before 5 pm and please call to 9384838383 number", "Deliver to Room No 302,If the customer is not present", "", "Deliver the product before 6 pm of friday or Reschedule the delivery to next monday i.e 29-may-2017"};
        String Lat[] = {"12.97194", "13.08784", "11.00555", "17.38405"};
        String Lon[] = {"77.59369", "80.27847", "76.96612", "78.45636"};
        String Address[] = {"Bangalore,Karnataka", "Chennai,TamilNadu", "Coimbator,TamilNadu", "Hydrerabad,Telengana"};

        ContentValues values = new ContentValues();

        for (int i = 0; i < name.length; i++) {
            values.put("ConsigneeId", i + 1);
            values.put("ConsigneeName", name[i]);
            values.put("ConsigneeDescription", Description[i]);
            values.put("ConsigneeLatitude", Lat[i]);
            values.put("ConsigneeLongitude", Lon[i]);
            values.put("ConsigneeAddress", Address[i]);
            values.put("ConsigneeStatusId", 2);
            database.insert("ConsigneeDetails", null, values);
        }
    }

    //TODO insert the record in to ConsigneeDetails table from Add activity
    public void insert_ConsigneeDetails(String[] res) {
        int max_id = Integer.parseInt(get_query_string("Select ConsigneeId from ConsigneeDetails order by ConsigneeId desc Limit 1")) + 1;

        ContentValues values = new ContentValues();
        SQLiteDatabase database = this.getWritableDatabase();
        values.put("ConsigneeId", max_id + "");
        values.put("ConsigneeName", res[0]);
        values.put("ConsigneeDescription", res[1]);
        values.put("ConsigneeLatitude", res[2]);
        values.put("ConsigneeLongitude", res[3]);
        values.put("ConsigneeAddress", res[4]);
        values.put("ConsigneeStatusId", 2);
        database.insert("ConsigneeDetails", null, values);
    }

    //TODO get the consignee information based on the Status.(Complete or Pending)
    public List<ListItem> get_ConsigneeDetails(int Id) {
        List<ListItem> data = new ArrayList<>();
        String query = "Select ConsigneeStatusId,ConsigneeId,ConsigneeName,ConsigneeDescription,ConsigneeLatitude,ConsigneeLongitude,ConsigneeAddress from ConsigneeDetails where ConsigneeStatusId=" + Id + " order by ConsigneeStatusId Desc";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ConsigneeLatitude")));
            double lon = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ConsigneeLongitude")));
            int ConsigneeId = cursor.getInt(cursor.getColumnIndex("ConsigneeId"));
            int statusId = cursor.getInt(cursor.getColumnIndex("ConsigneeStatusId"));

            ListItem l = new ListItem(statusId, ConsigneeId, cursor.getString(cursor.getColumnIndex("ConsigneeName")), cursor.getString(cursor.getColumnIndex("ConsigneeDescription")), lat, lon, cursor.getString(cursor.getColumnIndex("ConsigneeAddress")));
            data.add(l);
            cursor.moveToNext();
        }
        return data;

    }

    //TODO get the Consignee information based on the Consignee Id
    public ListItem get_ConsigneeDetails(String Id) {
        ListItem l = null;
        String query = "Select ConsigneeStatusId,ConsigneeId,ConsigneeName,ConsigneeDescription,ConsigneeLatitude,ConsigneeLongitude,ConsigneeAddress from ConsigneeDetails where ConsigneeId=" + Id;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ConsigneeLatitude")));
            double lon = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ConsigneeLongitude")));
            int ConsigneeId = cursor.getInt(cursor.getColumnIndex("ConsigneeId"));
            int statusId = cursor.getInt(cursor.getColumnIndex("ConsigneeStatusId"));

            l = new ListItem(statusId, ConsigneeId, cursor.getString(cursor.getColumnIndex("ConsigneeName")), cursor.getString(cursor.getColumnIndex("ConsigneeDescription")), lat, lon, cursor.getString(cursor.getColumnIndex("ConsigneeAddress")));
            cursor.moveToNext();
        }
        return l;

    }

    //TODO delete the consignee records based on the consigneeid
    public void delete_consigneedetails(int consigneeid) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("ConsigneeDetails", "ConsigneeId" + "=" + consigneeid, null);

    }

    //TODO updating the delivery status.
    public void update_Consignee_status(int consigneeid) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ConsigneeStatusId", 1);
        database.update("ConsigneeDetails", values, "ConsigneeId" + "=" + consigneeid, null);
    }


    private String get_query_string(String selectQuery) {
        String res;
        SQLiteDatabase database1 = this.getWritableDatabase();
        Cursor cursor = database1.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        res = "";
        while (!cursor.isAfterLast()) {
            res = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();

        return res;
    }


}