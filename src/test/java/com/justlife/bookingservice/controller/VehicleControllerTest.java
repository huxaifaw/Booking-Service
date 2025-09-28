package com.justlife.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justlife.bookingservice.model.Vehicle;
import com.justlife.bookingservice.service.IVehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private IVehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetVehicleById_Success() throws Exception {
        // Given
        Long vehicleId = 1L;
        Vehicle vehicle = createTestVehicle(vehicleId, "Test Vehicle");

        when(vehicleService.getVehicleById(vehicleId)).thenReturn(Optional.of(vehicle));

        // When & Then
        mockMvc.perform(get("/vehicles/{id}", vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.name").value("Test Vehicle"));

        verify(vehicleService).getVehicleById(vehicleId);
    }

    @Test
    void testGetVehicleById_NotFound() throws Exception {
        // Given
        Long vehicleId = 999L;

        when(vehicleService.getVehicleById(vehicleId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/vehicles/{id}", vehicleId))
                .andExpect(status().isNotFound());

        verify(vehicleService).getVehicleById(vehicleId);
    }

    @Test
    void testCreateVehicle_Success() throws Exception {
        // Given
        Vehicle vehicle = createTestVehicle(null, "New Vehicle");
        Vehicle savedVehicle = createTestVehicle(1L, "New Vehicle");

        when(vehicleService.createVehicle(any(Vehicle.class))).thenReturn(savedVehicle);

        // When & Then
        mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New Vehicle"));

        verify(vehicleService).createVehicle(any(Vehicle.class));
    }

    @Test
    void testUpdateVehicle_Success() throws Exception {
        // Given
        Long vehicleId = 1L;
        Vehicle vehicle = createTestVehicle(null, "Updated Vehicle");
        Vehicle updatedVehicle = createTestVehicle(vehicleId, "Updated Vehicle");

        when(vehicleService.updateVehicle(eq(vehicleId), any(Vehicle.class))).thenReturn(Optional.of(updatedVehicle));

        // When & Then
        mockMvc.perform(put("/vehicles/{id}", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.name").value("Updated Vehicle"));

        verify(vehicleService).updateVehicle(eq(vehicleId), any(Vehicle.class));
    }

    @Test
    void testUpdateVehicle_NotFound() throws Exception {
        // Given
        Long vehicleId = 999L;
        Vehicle vehicle = createTestVehicle(null, "Updated Vehicle");

        when(vehicleService.updateVehicle(eq(vehicleId), any(Vehicle.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/vehicles/{id}", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicle)))
                .andExpect(status().isNotFound());

        verify(vehicleService).updateVehicle(eq(vehicleId), any(Vehicle.class));
    }

    @Test
    void testDeleteVehicle_Success() throws Exception {
        // Given
        Long vehicleId = 1L;

        when(vehicleService.deleteVehicle(vehicleId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/vehicles/{id}", vehicleId))
                .andExpect(status().isNoContent());

        verify(vehicleService).deleteVehicle(vehicleId);
    }

    @Test
    void testDeleteVehicle_NotFound() throws Exception {
        // Given
        Long vehicleId = 999L;

        when(vehicleService.deleteVehicle(vehicleId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/vehicles/{id}", vehicleId))
                .andExpect(status().isNotFound());

        verify(vehicleService).deleteVehicle(vehicleId);
    }

    @Test
    void testGetAllVehicles_Success() throws Exception {
        // Given
        List<Vehicle> vehicles = Arrays.asList(
                createTestVehicle(1L, "Vehicle 1"),
                createTestVehicle(2L, "Vehicle 2")
        );

        when(vehicleService.getAllVehicles()).thenReturn(vehicles);

        // When & Then
        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Vehicle 1"))
                .andExpect(jsonPath("$[1].name").value("Vehicle 2"));

        verify(vehicleService).getAllVehicles();
    }

    @Test
    void testGetAllVehicles_EmptyList() throws Exception {
        // Given
        when(vehicleService.getAllVehicles()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(vehicleService).getAllVehicles();
    }

    // Helper method
    private Vehicle createTestVehicle(Long id, String name) {
        return Vehicle.builder()
                .id(id)
                .name(name)
                .build();
    }
} 