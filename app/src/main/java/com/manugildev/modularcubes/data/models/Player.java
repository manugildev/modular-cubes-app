package com.manugildev.modularcubes.data.models;

import com.manugildev.modularcubes.fragments.ThirdFragment;

import java.util.ArrayList;

public class Player {

    private ThirdFragment fragment;
    private ArrayList<Integer> times = new ArrayList<>();
    private ModularCube cube1, cube2;
    private int number;
    private String color;
    private int progress = 0;
    private int id;


    private String name;

    private ArrayList<Integer> cubeSequence = new ArrayList<>();

    public Player(ThirdFragment thirdFragment, String name, int id, int number, ModularCube cube1, ModularCube cube2, String color) {
        this.fragment = thirdFragment;
        this.id = id;
        this.name = name;
        this.number = number;
        this.cube1 = cube1;
        this.cube2 = cube2;
        this.color = color;
    }

    public ModularCube getCube1() {
        return cube1;
    }

    public void setCube1(ModularCube cube1) {
        this.cube1 = cube1;
    }

    public ModularCube getCube2() {
        return cube2;
    }

    public void setCube2(ModularCube cube2) {
        this.cube2 = cube2;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getProgress() {
        return progress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public ArrayList<Integer> getCubeSequence() {
        return cubeSequence;
    }

    public void setCubeSequence(ArrayList<Integer> cubeSequence) {
        this.cubeSequence.clear();
        this.cubeSequence.addAll(cubeSequence);
        this.number = cubeSequence.get(0);
        this.progress = 0;
        this.times.clear();
    }

    public boolean areBothOnSequence() {
        if (cubeSequence.size() > 0) {
            if (cube1.getCurrentOrientation() == cube2.getCurrentOrientation() && cube1.getCurrentOrientation() == cubeSequence.get(0)) {
                cubeSequence.remove(0);
                if (cubeSequence.size() > 0) setNumber(cubeSequence.get(0));
                return true;
            }
        } else setProgress(0);
        return false;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Integer> getTimes() {
        return times;
    }

    public void setTimes(ArrayList<Integer> times) {
        this.times = times;
    }

    public int getLastTime() {
        if (times.size() == 0) return 0;
        else return times.get(times.size() - 1);
    }

    public void addTime(int totalTime, long timeLeft) {
        getTimes().add((int) (totalTime - timeLeft));

    }
}
