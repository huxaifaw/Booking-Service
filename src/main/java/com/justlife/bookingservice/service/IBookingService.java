package com.justlife.bookingservice.service;

import com.justlife.bookingservice.model.Booking;
import com.justlife.bookingservice.model.BookingDetail;

import java.util.List;

public interface IBookingService {
    Booking createBooking(Booking booking);

    Booking updateBooking(Long bookingId, Booking updatedBooking);

    List<BookingDetail> getAllBookingDetails();
}
