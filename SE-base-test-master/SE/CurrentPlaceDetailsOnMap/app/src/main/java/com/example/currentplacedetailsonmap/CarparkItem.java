package com.example.currentplacedetailsonmap;

import android.location.Location;

import com.google.android.gms.maps.model.Marker;
import java.nio.channels.CancelledKeyException;


//Carpark items objects, should implement getters and setters but meh
public class CarparkItem {

    CarparkItem cp;
    private String carparkID;
    private String carparkArea;
    private String carparkDevelopment;
    private String carparkLocationString;
    private Location carparkLocation;
    private double distanceToUser=99999999;
    private int carparkLots;
    private String carparkType;
    private int carLots=0;
    private int motorLots=0;
    private int hvLots=0;
    private String carparkAgency;
    private Marker carparkMarker=null;

    //Should do getters and setters but cant be bothered

    public void setCarparkAgency(String carparkAgency) {
        this.carparkAgency = carparkAgency;
    }

    public void setCarLots(int carLots) {
        this.carLots = carLots;
    }

    public void setCarparkArea(String carparkArea) {
        this.carparkArea = carparkArea;
    }

    public void setCarparkDevelopment(String carparkDevelopment) {
        this.carparkDevelopment = carparkDevelopment;
    }

    public void setCarparkID(String carparkID) {
        this.carparkID = carparkID;
    }

    public void setCarparkLocation(Location carparkLocation) {
        this.carparkLocation = carparkLocation;
    }

    public void setCarparkLocationString(String carparkLocationString) {
        this.carparkLocationString = carparkLocationString;
    }

    public void setCarparkMarker(Marker carparkMarker) {
        this.carparkMarker = carparkMarker;
    }

    public void setCarparkLots(int carparkLots) {
        this.carparkLots = carparkLots;
    }

    public void setCarparkType(String carparkType) {
        this.carparkType = carparkType;
    }

    public void setCp(CarparkItem cp) {
        this.cp = cp;
    }

    public void setDistanceToUser(double distanceToUser) {
        this.distanceToUser = distanceToUser;
    }

    public void setHvLots(int hvLots) {
        this.hvLots = hvLots;
    }

    public void setMotorLots(int motorLots) {
        this.motorLots = motorLots;
    }

    public String getCarparkAgency() {
        return carparkAgency;
    }

    public CarparkItem getCp() {
        return this;
    }

    public int getCarLots() {
        return carLots;
    }

    public int getCarparkLots() {
        return carparkLots;
    }

    public int getHvLots() {
        return hvLots;
    }

    public int getMotorLots() {
        return motorLots;
    }

    public Location getCarparkLocation() {
        return carparkLocation;
    }

    public Marker getCarparkMarker() {
        return carparkMarker;
    }

    public String getCarparkArea() {
        return carparkArea;
    }

    public String getCarparkDevelopment() {
        return carparkDevelopment;
    }

    public String getCarparkLocationString() {
        return carparkLocationString;
    }

    public String getCarparkType() {
        return carparkType;
    }

    public String getCarparkID() {
        return carparkID;
    }

    public double getDistanceToUser()
    {
        return this.distanceToUser;
    }
    public void setItem(CarparkItem another) {
        this.cp = another.cp; // you can access
    }
    public CarparkItem getCarparkItem()
    {

        return cp;
    }
    public void setMarkerNullMap()
    {
        if(this.carparkMarker!=null && this!=null)
        {
            this.carparkMarker.remove();
        }
        else
            return;
    }
}
