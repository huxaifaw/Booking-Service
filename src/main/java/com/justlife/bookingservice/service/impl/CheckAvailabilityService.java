package com.justlife.bookingservice.service.impl;

import com.justlife.bookingservice.model.Booking;
import com.justlife.bookingservice.model.BookingDetail;
import com.justlife.bookingservice.model.Worker;
import com.justlife.bookingservice.repository.BookingDetailRepository;
import com.justlife.bookingservice.repository.WorkerRepository;
import com.justlife.bookingservice.service.ICheckAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckAvailabilityService implements ICheckAvailabilityService {

    private final WorkerRepository workerRepository;

    private final BookingDetailRepository bookingDetailRepository;

    /**
     * Checks the availability of workers on a given date.
     *
     * @param date            the date to check availability for
     * @param workersRequired the number of workers required
     * @return a list of available workers
     */
    @Override
    public List<Worker> checkAvailabilityForDate(LocalDate date, int workersRequired) {
        LocalDateTime startOfDay = date.atTime(8, 0);
        LocalDateTime endOfDay = date.atTime(22, 0);

        List<Worker> allWorkers = workerRepository.findAll();

        return allWorkers.stream()
                .filter(wrk -> isAvailableOnDate(wrk, startOfDay, endOfDay))
                .limit(workersRequired)
                .toList();
    }

    /**
     * Checks the availability of workers for a given date and time range.
     *
     * @param startTime       the start time of the required availability
     * @param duration        the duration of the required availability
     * @param workersRequired the number of workers required
     * @return a list of available workers
     */
    @Override
    public List<Worker> checkAvailabilityByDateTime(LocalDateTime startTime, int duration, int workersRequired) {
        LocalDateTime endTime = startTime.plusHours(duration);
        List<Worker> allWorkers = workerRepository.findAll();

        return allWorkers.stream()
                .filter(wrk -> isAvailableOnDataTime(wrk, startTime, endTime))
                .limit(workersRequired)
                .toList();
    }

    /**
     * Checks if a workers is available on a given date.
     *
     * @param worker     the workers to check availability for
     * @param startOfDay the start of the day to check availability from
     * @param endOfDay   the end of the day to check availability until
     * @return true if the worker is available on the given date, false otherwise
     */
    private boolean isAvailableOnDate(Worker worker, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        if (checkIfOutsideWorkingHours(worker, startOfDay, endOfDay)) return false;

        // check for existing bookings and ensure a 30-minute break
        List<BookingDetail> bookingDetails = bookingDetailRepository.findByWorkerIdAndBookingStartTimeBetween(
                worker.getId(), startOfDay, endOfDay);

        return checkAvailability(startOfDay, endOfDay, bookingDetails);
    }

    /**
     * Checks if a worker is available for a given time range.
     *
     * @param worker    the worker to check availability for
     * @param startTime the start time of the required availability
     * @param endTime   the end time of the required availability
     * @return true if the worker is available for the given time range, false otherwise
     */
    private boolean isAvailableOnDataTime(Worker worker, LocalDateTime startTime, LocalDateTime endTime) {
        if (checkIfOutsideWorkingHours(worker, startTime, endTime)) return false;

        // check for existing bookings and ensure a 30-minute break
        List<BookingDetail> bookingDetails = bookingDetailRepository.findByWorkerIdAndBookingStartTimeBetween(
                worker.getId(), startTime.toLocalDate().atStartOfDay(), endTime.toLocalDate().atTime(23, 59));

        return checkAvailability(startTime, endTime, bookingDetails);
    }

    private boolean checkAvailability(LocalDateTime startTime, LocalDateTime endTime, List<BookingDetail> bookingDetails) {
        for (BookingDetail bookingDetail : bookingDetails) {
            LocalDateTime bookingStart = bookingDetail.getBooking().getStartTime();
            LocalDateTime bookingEnd = bookingDetail.getBooking().getEndTime();

            if (startTime.isBefore(bookingEnd.plusMinutes(30)) && endTime.isAfter(bookingStart.minusMinutes(30))) {
                return false;
            }
        }

        return true;
    }

    private boolean checkIfOutsideWorkingHours(Worker worker, LocalDateTime startTime, LocalDateTime endTime) {
        boolean isWorkingDay = worker.isWorkingOnFridays() || startTime.getDayOfWeek().getValue() != 5;
        if (!isWorkingDay) {
            return true;
        }
        String[] workingHours = worker.getWorkingHours().split("-");
        LocalTime startWorkTime = LocalTime.parse(workingHours[0]);
        LocalTime endWorkTime = LocalTime.parse(workingHours[1]);
        boolean isWithinWorkingHours = !startTime.toLocalTime().isBefore(startWorkTime) && !endTime.toLocalTime().isAfter(endWorkTime);

        return !isWithinWorkingHours;
    }

    /**
     * Updates the availability of workers after a booking is created.
     *
     * @param workers the list of workers to update
     * @param booking the booking information
     */
    @Override
    public void updateWorkersAvailability(List<Worker> workers, Booking booking) {
        for (Worker worker : workers) {
            BookingDetail bookingDetail = new BookingDetail();
            bookingDetail.setBooking(booking);
            bookingDetail.setWorker(worker);
            bookingDetailRepository.save(bookingDetail);
        }
    }
}
