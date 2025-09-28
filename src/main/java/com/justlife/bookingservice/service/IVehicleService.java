package com.justlife.bookingservice.service;

import com.justlife.bookingservice.model.Vehicle;

import java.util.List;
import java.util.Optional;

public interface IVehicleService {
    Optional<Vehicle> getVehicleById(Long id);

    Vehicle createVehicle(Vehicle vehicle);

    Optional<Vehicle> updateVehicle(Long id, Vehicle vehicle);

    boolean deleteVehicle(Long id);

    List<Vehicle> getAllVehicles();
}
