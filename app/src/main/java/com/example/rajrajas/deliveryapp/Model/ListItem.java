package com.example.rajrajas.deliveryapp.Model;


/**
 * Created by rajrajas on 5/17/2017.
 */

public class ListItem
{
    private String name,description,Address;
    private double lat,lon;
    private int consigneeId,statusId;


    public ListItem(int statusId,int consigneeId,String name,String description,double lat,double lon,String Address)
    {
        this.name=name;
        this.description=description;
        this.lat=lat;
        this.lon=lon;
        this.Address=Address;
        this.consigneeId=consigneeId;
        this.statusId=statusId;
    }

    public  String getName()
    {
        return name;
    }
    public String getDescription()
    {
        return description;
    }
    public double getLat()
    {
        return lat;
    }
    public double getLon()
    {
        return lon;
    }
    public String getAddress()
    {
        return Address;
    }
    public int getConsigneeId()
    {
        return consigneeId;
    }

    public int getStatusId()
    {
        return statusId;
    }
}
