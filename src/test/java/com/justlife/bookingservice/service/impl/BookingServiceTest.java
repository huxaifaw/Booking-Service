package com.justlife.bookingservice.service.impl;

import com.justlife.bookingservice.model.Booking;
import com.justlife.bookingservice.model.BookingDetail;
import com.justlife.bookingservice.model.Vehicle;
import com.justlife.bookingservice.model.Worker;
import com.justlife.bookingservice.repository.BookingDetailRepository;
import com.justlife.bookingservice.repository.BookingRepository;
import com.justlife.bookingservice.service.ICheckAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingDetailRepository bookingDetailRepository;

    @Mock
    private ICheckAvailabilityService availabilityCheckService;

    @InjectMocks
    private BookingService bookingService;

    private Booking testBooking;
    private List<Worker> availableWorkers;

    @BeforeEach
    void setUp() {
        testBooking = createTestBooking();
        availableWorkers = createTestWorkers();
    }

    @Test
    void testCreateBooking_Success() {
        // Given
        Booking savedBooking = createTestBooking();
        savedBooking.setId(1L);

        when(availabilityCheckService.checkAvailabilityByDateTime(any(LocalDateTime.class), anyInt(), anyInt()))
                .thenReturn(availableWorkers);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // When
        Booking result = bookingService.createBooking(testBooking);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2, result.getDuration());
        assertEquals(1, result.getRequiredWorkers());

        verify(availabilityCheckService).checkAvailabilityByDateTime(any(LocalDateTime.class), eq(2), eq(1));
        verify(bookingRepository).save(any(Booking.class));
        verify(availabilityCheckService).updateWorkersAvailability(anyList(), any(Booking.class));
    }

    @Test
    void testCreateBooking_InvalidDuration_ThrowsException() {
        // Given
        testBooking.setDuration(3);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(testBooking));

        assertEquals("Invalid booking duration. Must be 2 or 4 hours.", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_InvalidWorkersRequired_ThrowsException() {
        // Given
        testBooking.setRequiredWorkers(5);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(testBooking));

        assertEquals("Invalid number of workers required", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_NotEnoughWorkersAvailable_ThrowsException() {
        // Given
        when(availabilityCheckService.checkAvailabilityByDateTime(any(LocalDateTime.class), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(testBooking));

        assertEquals("Not enough workers available for the provided time", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_NotEnoughWorkersFromSameVehicle_ThrowsException() {
        // Given
        testBooking.setRequiredWorkers(2);
        List<Worker> workersFromDifferentVehicles = Arrays.asList(
                createTestWorker(1L, "Worker 1", 1L),
                createTestWorker(2L, "Worker 2", 2L) // Different vehicle
        );

        when(availabilityCheckService.checkAvailabilityByDateTime(any(LocalDateTime.class), anyInt(), anyInt()))
                .thenReturn(workersFromDifferentVehicles);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(testBooking));

        assertEquals("Not enough workers available from the same vehicle", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testUpdateBooking_Success() {
        // Given
        Long bookingId = 1L;
        Booking existingBooking = createTestBooking();
        existingBooking.setId(bookingId);
        
        Booking updatedBooking = createTestBooking();
        updatedBooking.setDuration(4);
        updatedBooking.setRequiredWorkers(2);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(availabilityCheckService.checkAvailabilityByDateTime(any(LocalDateTime.class), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(availableWorkers.get(0), availableWorkers.get(0))); // Same vehicle workers
        when(bookingRepository.save(any(Booking.class))).thenReturn(existingBooking);

        // When
        Booking result = bookingService.updateBooking(bookingId, updatedBooking);

        // Then
        assertNotNull(result);
        assertEquals(bookingId, result.getId());
        assertEquals(4, result.getDuration());
        assertEquals(2, result.getRequiredWorkers());

        verify(bookingRepository).findById(bookingId);
        verify(bookingRepository).save(any(Booking.class));
        verify(availabilityCheckService).updateWorkersAvailability(anyList(), any(Booking.class));
    }

    @Test
    void testUpdateBooking_BookingNotFound_ThrowsException() {
        // Given
        Long bookingId = 999L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.updateBooking(bookingId, testBooking));

        assertEquals("Booking not found!", exception.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testUpdateBooking_InvalidDuration_ThrowsException() {
        // Given
        Long bookingId = 1L;
        testBooking.setDuration(1);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.updateBooking(bookingId, testBooking));

        assertEquals("Invalid booking duration. Must be 2 or 4 hours.", exception.getMessage());
        verify(bookingRepository, never()).findById(anyLong());
    }

    @Test
    void testGetAllBookingDetails_Success() {
        // Given
        List<BookingDetail> bookingDetails = Arrays.asList(
                createTestBookingDetail(1L),
                createTestBookingDetail(2L)
        );

        when(bookingDetailRepository.findAll()).thenReturn(bookingDetails);

        // When
        List<BookingDetail> result = bookingService.getAllBookingDetails();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookingDetailRepository).findAll();
    }

    @Test
    void testGetAllBookingDetails_EmptyList() {
        // Given
        when(bookingDetailRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<BookingDetail> result = bookingService.getAllBookingDetails();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookingDetailRepository).findAll();
    }

    // Helper methods
    private Booking createTestBooking() {
        return Booking.builder()
                .startTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .duration(2)
                .requiredWorkers(1)
                .build();
    }

    private List<Worker> createTestWorkers() {
        return Arrays.asList(
                createTestWorker(1L, "Worker 1", 1L),
                createTestWorker(2L, "Worker 2", 1L) // Same vehicle
        );
    }

    private Worker createTestWorker(Long id, String name, Long vehicleId) {
        Vehicle vehicle = Vehicle.builder()
                .id(vehicleId)
                .name("Test Vehicle " + vehicleId)
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

    private BookingDetail createTestBookingDetail(Long id) {
        return BookingDetail.builder()
                .id(id)
                .booking(createTestBooking())
                .worker(createTestWorker(1L, "Test Worker", 1L))
                .build();
    }
} 