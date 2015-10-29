package com.seojunkyo.soma.controlhome.util;

import android.widget.ToggleButton;

/**
 * Created by seojunkyo on 15. 8. 17..
 */
public class DeviceList {

    public ToggleButton ToggleBTN;
    public String Device;
    public String on;
    public String off;

    public DeviceList(ToggleButton ToggleBTN, String Device, String on, String off){
        this.ToggleBTN = ToggleBTN;
        this.Device = on;
        this.on = on;
        this.off = off;
    }

    public void setDevice(String device) {
        Device = device;
    }

    public void setToggleBTN(ToggleButton toggleBTN) {
        ToggleBTN = toggleBTN;
    }

    public void setOn(String on) {
        this.on = on;
    }

    public void setOff(String off) {
        this.off = off;
    }

    public ToggleButton getToggleBTN() {
        return ToggleBTN;
    }

    public String getDevice() {
        return Device;
    }

    public String getOn() {
        return on;
    }

    public String getOff() {
        return off;
    }
}
