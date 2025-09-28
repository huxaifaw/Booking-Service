package com.justlife.bookingservice.service.impl;

import com.justlife.bookingservice.model.Vehicle;
import com.justlife.bookingservice.model.Worker;
import com.justlife.bookingservice.repository.WorkerRepository;
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
class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    @InjectMocks
    private WorkerService workerService;

    private Worker testWorker;

    @BeforeEach
    void setUp() {
        testWorker = createTestWorker(1L, "John Doe");
    }

    @Test
    void testGetWorkerById_Success() {
        // Given
        Long workerId = 1L;
        when(workerRepository.findById(workerId)).thenReturn(Optional.of(testWorker));

        // When
        Optional<Worker> result = workerService.getWorkerById(workerId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(workerId, result.get().getId());
        assertEquals("John Doe", result.get().getName());
        verify(workerRepository).findById(workerId);
    }

    @Test
    void testGetWorkerById_NotFound() {
        // Given
        Long workerId = 999L;
        when(workerRepository.findById(workerId)).thenReturn(Optional.empty());

        // When
        Optional<Worker> result = workerService.getWorkerById(workerId);

        // Then
        assertFalse(result.isPresent());
        verify(workerRepository).findById(workerId);
    }

    @Test
    void testCreateWorker_Success() {
        // Given
        Worker newWorker = createTestWorker(null, "Jane Smith");
        Worker savedWorker = createTestWorker(2L, "Jane Smith");

        when(workerRepository.save(any(Worker.class))).thenReturn(savedWorker);

        // When
        Worker result = workerService.createWorker(newWorker);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Jane Smith", result.getName());
        assertTrue(result.isAvailable());
        verify(workerRepository).save(newWorker);
    }

    @Test
    void testUpdateWorker_Success() {
        // Given
        Long workerId = 1L;
        Worker existingWorker = createTestWorker(workerId, "John Doe");
        Worker updateWorker = createTestWorker(null, "John Updated");
        Worker savedWorker = createTestWorker(workerId, "John Updated");

        when(workerRepository.findById(workerId)).thenReturn(Optional.of(existingWorker));
        when(workerRepository.save(any(Worker.class))).thenReturn(savedWorker);

        // When
        Optional<Worker> result = workerService.updateWorker(workerId, updateWorker);

        // Then
        assertTrue(result.isPresent());
        assertEquals(workerId, result.get().getId());
        assertEquals("John Updated", result.get().getName());
        verify(workerRepository).findById(workerId);
        verify(workerRepository).save(any(Worker.class));
    }

    @Test
    void testUpdateWorker_NotFound() {
        // Given
        Long workerId = 999L;
        Worker updateWorker = createTestWorker(null, "Updated Name");

        when(workerRepository.findById(workerId)).thenReturn(Optional.empty());

        // When
        Optional<Worker> result = workerService.updateWorker(workerId, updateWorker);

        // Then
        assertFalse(result.isPresent());
        verify(workerRepository).findById(workerId);
        verify(workerRepository, never()).save(any(Worker.class));
    }

    @Test
    void testDeleteWorker_Success() {
        // Given
        Long workerId = 1L;
        when(workerRepository.findById(workerId)).thenReturn(Optional.of(testWorker));

        // When
        boolean result = workerService.deleteWorker(workerId);

        // Then
        assertTrue(result);
        verify(workerRepository).findById(workerId);
        verify(workerRepository).deleteById(workerId);
    }

    @Test
    void testDeleteWorker_NotFound() {
        // Given
        Long workerId = 999L;
        when(workerRepository.findById(workerId)).thenReturn(Optional.empty());

        // When
        boolean result = workerService.deleteWorker(workerId);

        // Then
        assertFalse(result);
        verify(workerRepository).findById(workerId);
        verify(workerRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetAllWorkers_Success() {
        // Given
        List<Worker> workers = Arrays.asList(
                createTestWorker(1L, "John Doe"),
                createTestWorker(2L, "Jane Smith")
        );

        when(workerRepository.findAll()).thenReturn(workers);

        // When
        List<Worker> result = workerService.getAllWorkers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
        verify(workerRepository).findAll();
    }

    @Test
    void testGetAllWorkers_EmptyList() {
        // Given
        when(workerRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Worker> result = workerService.getAllWorkers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(workerRepository).findAll();
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