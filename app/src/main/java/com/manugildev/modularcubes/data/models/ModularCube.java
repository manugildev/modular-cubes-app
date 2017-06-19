package com.manugildev.modularcubes.data.models;

import android.view.View;

import com.manugildev.modularcubes.fragments.MainActivityFragment;

public class ModularCube {

    private String ip;
    private long deviceId;
    private int currentOrientation;
    private Boolean activated;
    private View view;
    private MainActivityFragment activity;
    private int viewId;
    private int depth;
    private long now = 0;

    public ModularCube(MainActivityFragment activity) {
        this.activity = activity;
        //this.cubeAudio = new CubeAudio(this, activity.getActivity().getApplicationContext(), soundId);
    }


    public boolean updateCube(ModularCube cube) {
        //System.out.println("UpdateCube - " + cube.getDeviceId() + " " +cube.getCurrentOrientation());
        setIp(cube.getIp());
        setDeviceId(cube.getDeviceId());
        setCurrentOrientation(cube.getCurrentOrientation());
        setActivated(cube.isActivated());
        if (!this.ip.equals(cube.getIp()) || this.deviceId != cube.getDeviceId() ||
                this.currentOrientation != cube.getCurrentOrientation() ||
                this.activated != cube.isActivated() || this.depth != cube.getDepth())
            return true;
        return false;
    }

    public void setActivity(MainActivityFragment activity) {
        this.activity = activity;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public void setCurrentOrientation(int currentOrientation) {
        if (this.currentOrientation != currentOrientation && activity != null && viewId != 0) {
            this.currentOrientation = currentOrientation;
            activity.changeTextInButton(this);
            //cubeAudio.setPitch(currentOrientation);
        }
        this.currentOrientation = currentOrientation;
    }

    public void setActivated(Boolean activated) {
        if (activity != null && viewId != 0 && this.activated != activated) {
            activity.changeActivatedLight(getViewId(), activated);
            //if(activated && !cubeAudio.isPlaying()) cubeAudio.start();
            //else cubeAudio.pause();
        }
        this.activated = activated;

    }

    public String getIp() {
        return ip;
    }

    public long getDeviceId() {
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
                ", viewId=" + viewId +
                ", depth=" + depth +
                '}';
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    public int getViewId() {
        return this.viewId;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        if (activity != null && viewId != 0 && this.depth != depth) {
            activity.changeCubeDepth(getViewId(), depth);
        }
        this.depth = depth;
    }
}
