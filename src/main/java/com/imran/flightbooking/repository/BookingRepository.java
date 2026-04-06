package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.Booking;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    /**
     * Count bookings by user ID
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    /**
     * Get all bookings for a specific user
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId ORDER BY b.bookingDate DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);
    
    /**
     * Count upcoming bookings (where booking_date >= today) for a user
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId AND b.bookingDate >= CAST(CURRENT_DATE AS string)")
    Long countUpcomingBookings(@Param("userId") Long userId);
    
    /**
     * Get the latest booking for a user (most recent)
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId ORDER BY b.bookingDate DESC LIMIT 1")
    Booking findLatestByUserId(@Param("userId") Long userId);
    
    /**
     * Count active bookings (CONFIRMED or COMPLETED)
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status IN ('CONFIRMED', 'COMPLETED')")
    Long countActiveBookings();
    
    /**
     * Count confirmed bookings
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Long countConfirmedBookings();
    
    /**
     * Count pending bookings
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'PENDING'")
    Long countPendingBookings();
    
    /**
     * Count cancelled bookings
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CANCELLED'")
    Long countCancelledBookings();
    
    /**
     * Calculate total revenue from all bookings
     */
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b")
    Double getTotalRevenue();
    
    /**
     * Calculate monthly revenue for current month
     */
    @Query(value = "SELECT COALESCE(SUM(total_price), 0) FROM booking WHERE MONTH(booking_date) = MONTH(CURDATE()) AND YEAR(booking_date) = YEAR(CURDATE())", nativeQuery = true)
    Double getMonthlyRevenue();
    
    /**
     * Find booking by booking reference
     */
    @Query("SELECT b FROM Booking b WHERE b.bookingReference = :bookingReference")
    Booking findByBookingReference(@Param("bookingReference") String bookingReference);
    
    /**
     * Find bookings with filtering and pagination
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "(:bookingReference IS NULL OR LOWER(b.bookingReference) LIKE LOWER(CONCAT('%', :bookingReference, '%'))) AND " +
           "(:passengerName IS NULL OR LOWER(b.passengerName) LIKE LOWER(CONCAT('%', :passengerName, '%'))) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:dateFrom IS NULL OR b.bookingDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR b.bookingDate <= :dateTo) " +
           "ORDER BY b.bookingDate DESC")
    List<Booking> findBookingsWithFilters(@Param("bookingReference") String bookingReference,
                                         @Param("passengerName") String passengerName,
                                         @Param("status") String status,
                                         @Param("dateFrom") String dateFrom,
                                         @Param("dateTo") String dateTo);
}