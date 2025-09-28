package com.justlife.bookingservice.service.impl;

import com.justlife.bookingservice.model.Booking;
import com.justlife.bookingservice.model.BookingDetail;
import com.justlife.bookingservice.model.Vehicle;
import com.justlife.bookingservice.model.Worker;
import com.justlife.bookingservice.repository.BookingDetailRepository;
import com.justlife.bookingservice.repository.BookingRepository;
import com.justlife.bookingservice.service.IBookingService;
import com.justlife.bookingservice.service.ICheckAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService {

    private final BookingRepository bookingRepository;

    private final BookingDetailRepository bookingDetailRepository;

    private final ICheckAvailabilityService availabilityCheckService;

    @Override
    @Transactional
    public Booking createBooking(Booking booking) {
        validateBooking(booking);

        List<Worker> assignedWorkers = getAssignedWorkers(booking);

        Booking savedBooking = bookingRepository.save(booking);

        availabilityCheckService.updateWorkersAvailability(assignedWorkers, savedBooking);

        return savedBooking;
    }

    @Override
    @Transactional
    public Booking updateBooking(Long bookingId, Booking updatedBooking) {
        validateBooking(updatedBooking);

        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found!"));

        List<Worker> assignedWorkers = getAssignedWorkers(updatedBooking);

        existingBooking.setStartTime(updatedBooking.getStartTime());
        existingBooking.setEndTime(updatedBooking.getEndTime());
        existingBooking.setDuration(updatedBooking.getDuration());
        existingBooking.setRequiredWorkers(updatedBooking.getRequiredWorkers());

        Booking savedBooking = bookingRepository.save(existingBooking);

        availabilityCheckService.updateWorkersAvailability(assignedWorkers, savedBooking);

        return savedBooking;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetail> getAllBookingDetails() {
        return bookingDetailRepository.findAll();
    }

    private List<Worker> getAssignedWorkers(Booking booking) {
        LocalDateTime startTime = booking.getStartTime();
        LocalDateTime endTime = startTime.plusHours(booking.getDuration());
        booking.setEndTime(endTime);

        List<Worker> availableWorkers = availabilityCheckService.checkAvailabilityByDateTime(startTime, booking.getDuration(), booking.getRequiredWorkers());

        if (availableWorkers.size() < booking.getRequiredWorkers()) {
            throw new IllegalStateException("Not enough workers available for the provided time");
        }

        Vehicle vehicle = availableWorkers.get(0).getVehicle();
        return filterWorkersByVehicle(vehicle.getId(), availableWorkers, booking.getRequiredWorkers());
    }

    private void validateBooking(Booking booking) {
        if (booking.getDuration() != 2 && booking.getDuration() != 4) {
            throw new IllegalArgumentException("Invalid booking duration. Must be 2 or 4 hours.");
        }
        if (booking.getRequiredWorkers() < 1 || booking.getRequiredWorkers() > 3) {
            throw new IllegalArgumentException("Invalid number of workers required");
        }
    }

    private List<Worker> filterWorkersByVehicle(Long vehicleId, List<Worker> availableWorkers, int workersRequired) {
        List<Worker> assignedWorkers = availableWorkers.stream()
                .filter(wrk -> wrk.getVehicle().getId().equals(vehicleId))
                .limit(workersRequired)
                .toList();

        if (assignedWorkers.size() < workersRequired) {
            throw new IllegalStateException("Not enough workers available from the same vehicle");
        }

        return assignedWorkers;
    }
}
