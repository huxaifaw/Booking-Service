package com.justlife.bookingservice.service.impl;

import com.justlife.bookingservice.model.Vehicle;
import com.justlife.bookingservice.repository.VehicleRepository;
import com.justlife.bookingservice.service.IVehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService implements IVehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Optional<Vehicle> updateVehicle(Long id, Vehicle vehicle) {
        return vehicleRepository.findById(id).map(existingVehicle -> {
            vehicle.setId(id);
            return vehicleRepository.save(vehicle);
        });
    }

    @Override
    public boolean deleteVehicle(Long id) {
        return vehicleRepository.findById(id).map(vehicle -> {
            vehicleRepository.deleteById(id);
            return true;
        }).orElse(false);
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
}
