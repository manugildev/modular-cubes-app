package com.manugildev.modularcubes.fragments;

import com.manugildev.modularcubes.data.models.ModularCube;

public interface FragmentInterface {
    void communicateToFragment2();

    void sendMessage(String message);

    void updatedCube(ModularCube cube);

    void sendActivate(long currentCube, boolean b, boolean r);

    void removeItem(ModularCube modularCube);

    void addItem(ModularCube modularCube);

    void addCube3D(long id);

    void deleteCube3D(Long aLong);
}
