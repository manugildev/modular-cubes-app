package com.manugildev.modularcubes.data.models;

import java.util.ArrayList;

public class Player {

    private ModularCube cube1, cube2;
    private String name;
    private String color;
    private int progress = 0;
    private int id;

    private ArrayList<Integer> cubeSequence = new ArrayList<>();

    public Player(int id, String name, ModularCube cube1, ModularCube cube2, String color) {
        this.id = id;
        this.name = name;
        this.cube1 = cube1;
        this.cube2 = cube2;
        this.color = color;
    }

    public boolean bothHaveTheNumber(int number) {
        if (cube1.getCurrentOrientation() == cube2.getCurrentOrientation() && cube1.getCurrentOrientation() == number)
            return true;
        else return false;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public ArrayList<Integer> getCubeSequence() {
        return cubeSequence;
    }

    public void setCubeSequence(ArrayList<Integer> cubeSequence) {
        this.cubeSequence = cubeSequence;
    }
}
