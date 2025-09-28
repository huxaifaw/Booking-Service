package com.justlife.bookingservice.controller;

import com.justlife.bookingservice.model.Booking;
import com.justlife.bookingservice.model.BookingDetail;
import com.justlife.bookingservice.model.Worker;
import com.justlife.bookingservice.service.IBookingService;
import com.justlife.bookingservice.service.ICheckAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static com.justlife.bookingservice.util.Constants.DATE_PATTERN;
import static com.justlife.bookingservice.util.Constants.DATE_TIME_PATTERN;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final IBookingService bookingService;

    private final ICheckAvailabilityService availabilityCheckService;

    @Operation(summary = "Check availability of workers/cleaning professionals")
    @GetMapping("/availability")
    public ResponseEntity<List<Worker>> availabilityCheck(@RequestParam String date, @RequestParam(required = false) String startTime,
                                                          @RequestParam(required = false) Integer duration, @RequestParam(required = false, defaultValue = "1") Integer workersRequired) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            LocalDate parsedDate = LocalDate.parse(date, dateFormatter);

            if (workersRequired < 1 || workersRequired > 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number of workers/cleaning professionals required");
            }

            if (duration != null && duration != 2 && duration != 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must be 2 or 4 hours.");
            }

            if (startTime == null || duration == null) {
                return ResponseEntity.ok(availabilityCheckService.checkAvailabilityForDate(parsedDate, workersRequired));
            } else {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
                LocalDateTime parsedStartTime = LocalDateTime.parse(startTime, dateTimeFormatter);
                return ResponseEntity.ok(availabilityCheckService.checkAvailabilityByDateTime(parsedStartTime, duration, workersRequired));
            }
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format", e);
        }
    }

    @Operation(summary = "Create a new booking")
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        if (booking.getRequiredWorkers() < 1 || booking.getRequiredWorkers() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number of workers/professionals required");
        }
        if (booking.getDuration() != 2 && booking.getDuration() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking duration. Must be 2 or 4 hours.");
        }

        return ResponseEntity.ok(bookingService.createBooking(booking));
    }

    @Operation(summary = "Update an existing booking")
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking booking) {
        if (booking.getRequiredWorkers() < 1 || booking.getRequiredWorkers() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number of workers required");
        }
        if (booking.getDuration() != 2 && booking.getDuration() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking duration. Must be 2 or 4 hours.");
        }

        return ResponseEntity.ok(bookingService.updateBooking(id, booking));
    }

    @Operation(summary = "Get all bookings")
    @GetMapping
    public ResponseEntity<List<BookingDetail>> getAllBookingDetails() {
        return ResponseEntity.of(Optional.ofNullable(bookingService.getAllBookingDetails()));
    }
}
