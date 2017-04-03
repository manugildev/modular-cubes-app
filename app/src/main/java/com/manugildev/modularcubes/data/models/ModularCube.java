package com.manugildev.modularcubes.data.models;

import android.view.View;

import com.manugildev.modularcubes.MainActivityFragment;

public class ModularCube {

    private String ip;
    private int deviceId;
    private int currentOrientation;
    private Boolean activated;
    private View view;
    private MainActivityFragment activity;

    public ModularCube() { }


    public boolean updateCube(ModularCube cube) {
        setActivity(cube.activity);
        setIp(cube.getIp());
        setDeviceId(cube.getDeviceId());
        setCurrentOrientation(cube.getCurrentOrientation());
        setActivated(cube.isActivated());
        if (!this.ip.equals(cube.getIp()) || this.deviceId != cube.getDeviceId() ||
                this.currentOrientation != cube.getCurrentOrientation() ||
                this.activated != cube.isActivated())
            return true;
        return false;
    }

    public void setActivity(MainActivityFragment activity) {
        this.activity = activity;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public void setCurrentOrientation(int currentOrientation) {
        if (this.currentOrientation != currentOrientation && activity != null)
            activity.changeTextInButton(getDeviceId(), String.valueOf(currentOrientation));
        this.currentOrientation = currentOrientation;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public String getIp() {
        return ip;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getCurrentOrientation() {
        return currentOrientation;
    }

    public Boolean isActivated() {
        return activated;
    }

    public void setView(View view) {
        this.view = view;
    }

    public View getView() {
        return view;
    }

    @Override
    public String toString() {
        return "ModularCube{" +
                "ip='" + ip + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", currentOrientation=" + currentOrientation +
                ", activated=" + activated +
                '}';
    }


}
