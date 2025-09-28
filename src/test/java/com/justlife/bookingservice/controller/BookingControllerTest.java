package com.justlife.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.justlife.bookingservice.model.Booking;
import com.justlife.bookingservice.model.BookingDetail;
import com.justlife.bookingservice.model.Vehicle;
import com.justlife.bookingservice.model.Worker;
import com.justlife.bookingservice.service.IBookingService;
import com.justlife.bookingservice.service.ICheckAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private IBookingService bookingService;

    @Mock
    private ICheckAvailabilityService availabilityCheckService;

    @InjectMocks
    private BookingController bookingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testAvailabilityCheck_WithDateOnly_Success() throws Exception {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<Worker> availableWorkers = Arrays.asList(
                createTestWorker(1L, "John Doe"),
                createTestWorker(2L, "Jane Smith")
        );

        when(availabilityCheckService.checkAvailabilityForDate(testDate, 1))
                .thenReturn(availableWorkers);

        // When & Then
        mockMvc.perform(get("/bookings/availability")
                        .param("date", "2024-01-15")
                        .param("workersRequired", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));

        verify(availabilityCheckService).checkAvailabilityForDate(testDate, 1);
    }

    @Test
    void testAvailabilityCheck_WithDateTimeAndDuration_Success() throws Exception {
        // Given
        LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        List<Worker> availableWorkers = Arrays.asList(createTestWorker(1L, "John Doe"));

        when(availabilityCheckService.checkAvailabilityByDateTime(testDateTime, 2, 1))
                .thenReturn(availableWorkers);

        // When & Then
        mockMvc.perform(get("/bookings/availability")
                        .param("date", "2024-01-15")
                        .param("startTime", "2024-01-15T10:00:00")
                        .param("duration", "2")
                        .param("workersRequired", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(availabilityCheckService).checkAvailabilityByDateTime(testDateTime, 2, 1);
    }

    @Test
    void testAvailabilityCheck_InvalidWorkersRequired_BadRequest() throws Exception {
        mockMvc.perform(get("/bookings/availability")
                        .param("date", "2024-01-15")
                        .param("workersRequired", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAvailabilityCheck_InvalidDuration_BadRequest() throws Exception {
        mockMvc.perform(get("/bookings/availability")
                        .param("date", "2024-01-15")
                        .param("startTime", "2024-01-15T10:00:00")
                        .param("duration", "3")
                        .param("workersRequired", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBooking_Success() throws Exception {
        // Given
        Booking booking = createTestBooking();
        Booking savedBooking = createTestBooking();
        savedBooking.setId(1L);

        when(bookingService.createBooking(any(Booking.class))).thenReturn(savedBooking);

        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.duration").value(2))
                .andExpect(jsonPath("$.requiredWorkers").value(1));

        verify(bookingService).createBooking(any(Booking.class));
    }

    @Test
    void testCreateBooking_InvalidWorkersRequired_BadRequest() throws Exception {
        // Given
        Booking booking = createTestBooking();
        booking.setRequiredWorkers(5);

        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any(Booking.class));
    }

    @Test
    void testCreateBooking_InvalidDuration_BadRequest() throws Exception {
        // Given
        Booking booking = createTestBooking();
        booking.setDuration(3);

        // When & Then
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any(Booking.class));
    }

    @Test
    void testUpdateBooking_Success() throws Exception {
        // Given
        Long bookingId = 1L;
        Booking booking = createTestBooking();
        Booking updatedBooking = createTestBooking();
        updatedBooking.setId(bookingId);

        when(bookingService.updateBooking(eq(bookingId), any(Booking.class))).thenReturn(updatedBooking);

        // When & Then
        mockMvc.perform(put("/bookings/{id}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.duration").value(2))
                .andExpect(jsonPath("$.requiredWorkers").value(1));

        verify(bookingService).updateBooking(eq(bookingId), any(Booking.class));
    }

    @Test
    void testUpdateBooking_InvalidWorkersRequired_BadRequest() throws Exception {
        // Given
        Long bookingId = 1L;
        Booking booking = createTestBooking();
        booking.setRequiredWorkers(0);

        // When & Then
        mockMvc.perform(put("/bookings/{id}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).updateBooking(anyLong(), any(Booking.class));
    }

    @Test
    void testUpdateBooking_InvalidDuration_BadRequest() throws Exception {
        // Given
        Long bookingId = 1L;
        Booking booking = createTestBooking();
        booking.setDuration(1);

        // When & Then
        mockMvc.perform(put("/bookings/{id}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).updateBooking(anyLong(), any(Booking.class));
    }

    @Test
    void testGetAllBookingDetails_Success() throws Exception {
        // Given
        List<BookingDetail> bookingDetails = Arrays.asList(
                createTestBookingDetail(1L),
                createTestBookingDetail(2L)
        );

        when(bookingService.getAllBookingDetails()).thenReturn(bookingDetails);

        // When & Then
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingService).getAllBookingDetails();
    }

    @Test
    void testGetAllBookingDetails_EmptyList() throws Exception {
        // Given
        when(bookingService.getAllBookingDetails()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookingService).getAllBookingDetails();
    }

    @Test
    void testGetAllBookingDetails_NullResult() throws Exception {
        // Given
        when(bookingService.getAllBookingDetails()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isNotFound());

        verify(bookingService).getAllBookingDetails();
    }

    // Helper methods
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

    private Booking createTestBooking() {
        return Booking.builder()
                .startTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .endTime(LocalDateTime.of(2024, 1, 15, 12, 0))
                .duration(2)
                .requiredWorkers(1)
                .build();
    }

    private BookingDetail createTestBookingDetail(Long id) {
        return BookingDetail.builder()
                .id(id)
                .booking(createTestBooking())
                .worker(createTestWorker(1L, "Test Worker"))
                .build();
    }
} 