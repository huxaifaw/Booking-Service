package com.justlife.bookingservice.service.impl;

import com.justlife.bookingservice.model.Booking;
import com.justlife.bookingservice.model.BookingDetail;
import com.justlife.bookingservice.model.Vehicle;
import com.justlife.bookingservice.model.Worker;
import com.justlife.bookingservice.repository.BookingDetailRepository;
import com.justlife.bookingservice.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckAvailabilityServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private BookingDetailRepository bookingDetailRepository;

    @InjectMocks
    private CheckAvailabilityService checkAvailabilityService;

    private List<Worker> testWorkers;
    private LocalDate testDate;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testWorkers = createTestWorkers();
        testDate = LocalDate.of(2024, 1, 15);
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    }

    @Test
    void testCheckAvailabilityForDate_Success() {
        // Given
        int workersRequired = 2;
        when(workerRepository.findAll()).thenReturn(testWorkers);
        when(bookingDetailRepository.findByWorkerIdAndBookingStartTimeBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<Worker> result = checkAvailabilityService.checkAvailabilityForDate(testDate, workersRequired);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(workerRepository).findAll();
        verify(bookingDetailRepository, times(testWorkers.size()))
                .findByWorkerIdAndBookingStartTimeBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testCheckAvailabilityForDate_NoWorkersAvailable() {
        // Given
        int workersRequired = 1;
        when(workerRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Worker> result = checkAvailabilityService.checkAvailabilityForDate(testDate, workersRequired);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(workerRepository).findAll();
    }

    @Test
    void testCheckAvailabilityForDate_WorkersNotWorkingOnFridays() {
        // Given
        LocalDate friday = LocalDate.of(2024, 1, 12); // Friday
        int workersRequired = 1;
        List<Worker> workersNotWorkingFridays = Arrays.asList(
                createTestWorker(1L, "Worker 1", false) // Not working on Fridays
        );

        when(workerRepository.findAll()).thenReturn(workersNotWorkingFridays);

        // When
        List<Worker> result = checkAvailabilityService.checkAvailabilityForDate(friday, workersRequired);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(workerRepository).findAll();
    }

    @Test
    void testCheckAvailabilityByDateTime_Success() {
        // Given
        int duration = 2;
        int workersRequired = 1;
        when(workerRepository.findAll()).thenReturn(testWorkers);
        when(bookingDetailRepository.findByWorkerIdAndBookingStartTimeBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<Worker> result = checkAvailabilityService.checkAvailabilityByDateTime(testDateTime, duration, workersRequired);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(workerRepository).findAll();
        // The service uses stream.limit(workersRequired), so it only checks availability for the first worker that meets criteria
        verify(bookingDetailRepository, atLeastOnce())
                .findByWorkerIdAndBookingStartTimeBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testCheckAvailabilityByDateTime_WorkerHasConflictingBooking() {
        // Given
        int duration = 2;
        int workersRequired = 1;
        
        // Create a conflicting booking
        Booking conflictingBooking = Booking.builder()
                .startTime(testDateTime.minusHours(1))
                .endTime(testDateTime.plusHours(1))
                .build();
        
        BookingDetail conflictingBookingDetail = BookingDetail.builder()
                .booking(conflictingBooking)
                .worker(testWorkers.get(0))
                .build();

        when(workerRepository.findAll()).thenReturn(testWorkers);
        when(bookingDetailRepository.findByWorkerIdAndBookingStartTimeBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(conflictingBookingDetail));
        when(bookingDetailRepository.findByWorkerIdAndBookingStartTimeBetween(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<Worker> result = checkAvailabilityService.checkAvailabilityByDateTime(testDateTime, duration, workersRequired);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId()); // Should return the second worker
        verify(workerRepository).findAll();
    }

    @Test
    void testCheckAvailabilityByDateTime_OutsideWorkingHours() {
        // Given
        LocalDateTime earlyMorning = LocalDateTime.of(2024, 1, 15, 6, 0); // Before 8 AM
        int duration = 2;
        int workersRequired = 1;

        when(workerRepository.findAll()).thenReturn(testWorkers);

        // When
        List<Worker> result = checkAvailabilityService.checkAvailabilityByDateTime(earlyMorning, duration, workersRequired);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty()); // No workers available outside working hours
        verify(workerRepository).findAll();
    }

    @Test
    void testUpdateWorkersAvailability_Success() {
        // Given
        Booking booking = Booking.builder()
                .id(1L)
                .startTime(testDateTime)
                .endTime(testDateTime.plusHours(2))
                .duration(2)
                .requiredWorkers(2)
                .build();

        List<Worker> workers = Arrays.asList(testWorkers.get(0), testWorkers.get(1));

        when(bookingDetailRepository.save(any(BookingDetail.class))).thenReturn(new BookingDetail());

        // When
        checkAvailabilityService.updateWorkersAvailability(workers, booking);

        // Then
        verify(bookingDetailRepository, times(2)).save(any(BookingDetail.class));
    }

    @Test
    void testUpdateWorkersAvailability_EmptyWorkersList() {
        // Given
        Booking booking = Booking.builder()
                .id(1L)
                .startTime(testDateTime)
                .endTime(testDateTime.plusHours(2))
                .build();

        List<Worker> emptyWorkers = Collections.emptyList();

        // When
        checkAvailabilityService.updateWorkersAvailability(emptyWorkers, booking);

        // Then
        verify(bookingDetailRepository, never()).save(any(BookingDetail.class));
    }

    // Helper methods
    private List<Worker> createTestWorkers() {
        return Arrays.asList(
                createTestWorker(1L, "Worker 1", true),
                createTestWorker(2L, "Worker 2", true)
        );
    }

    private Worker createTestWorker(Long id, String name, boolean workingOnFridays) {
        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .name("Test Vehicle")
                .build();

        return Worker.builder()
                .id(id)
                .name(name)
                .available(true)
                .workingHours("08:00-22:00")
                .workingOnFridays(workingOnFridays)
                .vehicle(vehicle)
                .build();
    }
} 