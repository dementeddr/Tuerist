package com.tuer.tuerist;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class TuerLocationListener implements LocationListener {
	
	private Location lastLocation = null;
	
	public Location getLastLocation() {
		return this.lastLocation;
	}
	
	public void setLastLocation(Location l) {
		this.lastLocation = l;
	}
	
    @Override
    public void onLocationChanged(Location loc) {
        lastLocation = loc;
    }

    @Override
    public void onProviderDisabled(String provider) {
//    	Log.v("Tuerist", "GPS disabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
//    	Log.v("Tuerist", "GPS enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    	Log.v("Tuerist", "Status changed");
    }
}
