package com.justlife.bookingservice.repository;

import com.justlife.bookingservice.model.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {

    List<BookingDetail> findByWorkerIdAndBookingStartTimeBetween(Long professionalId, LocalDateTime startTime, LocalDateTime endTime);
}
