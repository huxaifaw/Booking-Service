package com.justlife.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justlife.bookingservice.model.Vehicle;
import com.justlife.bookingservice.model.Worker;
import com.justlife.bookingservice.service.IWorkerService;
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
class WorkerControllerTest {

    @Mock
    private IWorkerService workerService;

    @InjectMocks
    private WorkerController workerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(workerController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetWorkerById_Success() throws Exception {
        // Given
        Long workerId = 1L;
        Worker worker = createTestWorker(workerId, "John Doe");

        when(workerService.getWorkerById(workerId)).thenReturn(Optional.of(worker));

        // When & Then
        mockMvc.perform(get("/workers/{id}", workerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workerId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.workingHours").value("08:00-22:00"))
                .andExpect(jsonPath("$.workingOnFridays").value(false));

        verify(workerService).getWorkerById(workerId);
    }

    @Test
    void testGetWorkerById_NotFound() throws Exception {
        // Given
        Long workerId = 999L;

        when(workerService.getWorkerById(workerId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/workers/{id}", workerId))
                .andExpect(status().isNotFound());

        verify(workerService).getWorkerById(workerId);
    }

    @Test
    void testCreateWorker_Success() throws Exception {
        // Given
        Worker worker = createTestWorker(null, "Jane Smith");
        Worker savedWorker = createTestWorker(1L, "Jane Smith");

        when(workerService.createWorker(any(Worker.class))).thenReturn(savedWorker);

        // When & Then
        mockMvc.perform(post("/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(worker)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.available").value(true));

        verify(workerService).createWorker(any(Worker.class));
    }

    @Test
    void testUpdateWorker_Success() throws Exception {
        // Given
        Long workerId = 1L;
        Worker worker = createTestWorker(null, "Updated Name");
        Worker updatedWorker = createTestWorker(workerId, "Updated Name");

        when(workerService.updateWorker(eq(workerId), any(Worker.class))).thenReturn(Optional.of(updatedWorker));

        // When & Then
        mockMvc.perform(put("/workers/{id}", workerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(worker)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workerId))
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(workerService).updateWorker(eq(workerId), any(Worker.class));
    }

    @Test
    void testUpdateWorker_NotFound() throws Exception {
        // Given
        Long workerId = 999L;
        Worker worker = createTestWorker(null, "Updated Name");

        when(workerService.updateWorker(eq(workerId), any(Worker.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/workers/{id}", workerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(worker)))
                .andExpect(status().isNotFound());

        verify(workerService).updateWorker(eq(workerId), any(Worker.class));
    }

    @Test
    void testDeleteWorker_Success() throws Exception {
        // Given
        Long workerId = 1L;

        when(workerService.deleteWorker(workerId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/workers/{id}", workerId))
                .andExpect(status().isNoContent());

        verify(workerService).deleteWorker(workerId);
    }

    @Test
    void testDeleteWorker_NotFound() throws Exception {
        // Given
        Long workerId = 999L;

        when(workerService.deleteWorker(workerId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/workers/{id}", workerId))
                .andExpect(status().isNotFound());

        verify(workerService).deleteWorker(workerId);
    }

    @Test
    void testGetAllWorkers_Success() throws Exception {
        // Given
        List<Worker> workers = Arrays.asList(
                createTestWorker(1L, "John Doe"),
                createTestWorker(2L, "Jane Smith")
        );

        when(workerService.getAllWorkers()).thenReturn(workers);

        // When & Then
        mockMvc.perform(get("/workers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));

        verify(workerService).getAllWorkers();
    }

    @Test
    void testGetAllWorkers_EmptyList() throws Exception {
        // Given
        when(workerService.getAllWorkers()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/workers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(workerService).getAllWorkers();
    }

    // Helper method
    private Worker createTestWorker(Long id, String name) {
        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .name("Test Vehicle")
                .build();

        return Worker.builder()
                .id(id)
                .name(name)
                .available(true)
                .workingHours("08:00-22:00")
                .workingOnFridays(false)
                .vehicle(vehicle)
                .build();
    }
} 