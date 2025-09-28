package com.justlife.bookingservice.service.impl;

import com.justlife.bookingservice.model.Vehicle;
import com.justlife.bookingservice.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = createTestVehicle(1L, "Test Vehicle");
    }

    @Test
    void testGetVehicleById_Success() {
        // Given
        Long vehicleId = 1L;
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(testVehicle));

        // When
        Optional<Vehicle> result = vehicleService.getVehicleById(vehicleId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(vehicleId, result.get().getId());
        assertEquals("Test Vehicle", result.get().getName());
        verify(vehicleRepository).findById(vehicleId);
    }

    @Test
    void testGetVehicleById_NotFound() {
        // Given
        Long vehicleId = 999L;
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // When
        Optional<Vehicle> result = vehicleService.getVehicleById(vehicleId);

        // Then
        assertFalse(result.isPresent());
        verify(vehicleRepository).findById(vehicleId);
    }

    @Test
    void testCreateVehicle_Success() {
        // Given
        Vehicle newVehicle = createTestVehicle(null, "New Vehicle");
        Vehicle savedVehicle = createTestVehicle(2L, "New Vehicle");

        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        // When
        Vehicle result = vehicleService.createVehicle(newVehicle);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("New Vehicle", result.getName());
        verify(vehicleRepository).save(newVehicle);
    }

    @Test
    void testUpdateVehicle_Success() {
        // Given
        Long vehicleId = 1L;
        Vehicle existingVehicle = createTestVehicle(vehicleId, "Old Name");
        Vehicle updateVehicle = createTestVehicle(null, "Updated Name");
        Vehicle savedVehicle = createTestVehicle(vehicleId, "Updated Name");

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        // When
        Optional<Vehicle> result = vehicleService.updateVehicle(vehicleId, updateVehicle);

        // Then
        assertTrue(result.isPresent());
        assertEquals(vehicleId, result.get().getId());
        assertEquals("Updated Name", result.get().getName());
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void testUpdateVehicle_NotFound() {
        // Given
        Long vehicleId = 999L;
        Vehicle updateVehicle = createTestVehicle(null, "Updated Name");

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // When
        Optional<Vehicle> result = vehicleService.updateVehicle(vehicleId, updateVehicle);

        // Then
        assertFalse(result.isPresent());
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void testDeleteVehicle_Success() {
        // Given
        Long vehicleId = 1L;
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(testVehicle));

        // When
        boolean result = vehicleService.deleteVehicle(vehicleId);

        // Then
        assertTrue(result);
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleRepository).deleteById(vehicleId);
    }

    @Test
    void testDeleteVehicle_NotFound() {
        // Given
        Long vehicleId = 999L;
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // When
        boolean result = vehicleService.deleteVehicle(vehicleId);

        // Then
        assertFalse(result);
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetAllVehicles_Success() {
        // Given
        List<Vehicle> vehicles = Arrays.asList(
                createTestVehicle(1L, "Vehicle 1"),
                createTestVehicle(2L, "Vehicle 2")
        );

        when(vehicleRepository.findAll()).thenReturn(vehicles);

        // When
        List<Vehicle> result = vehicleService.getAllVehicles();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Vehicle 1", result.get(0).getName());
        assertEquals("Vehicle 2", result.get(1).getName());
        verify(vehicleRepository).findAll();
    }

    @Test
    void testGetAllVehicles_EmptyList() {
        // Given
        when(vehicleRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Vehicle> result = vehicleService.getAllVehicles();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(vehicleRepository).findAll();
    }

    // Helper method
    private Vehicle createTestVehicle(Long id, String name) {
        return Vehicle.builder()
                .id(id)
                .name(name)
                .build();
    }
} 