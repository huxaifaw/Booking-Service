package com.justlife.bookingservice.service;

import com.justlife.bookingservice.model.Booking;
import com.justlife.bookingservice.model.Worker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ICheckAvailabilityService {
    List<Worker> checkAvailabilityForDate(LocalDate date, int workersRequired);

    List<Worker> checkAvailabilityByDateTime(LocalDateTime startTime, int duration, int workersRequired);

    void updateWorkersAvailability(List<Worker> workers, Booking booking);
}
