package ru.project.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.project.booking.dto.BookingShortDto;
import ru.project.booking.model.Booking;
import ru.project.booking.model.BookingStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.bookerId = :bookerId
    ORDER BY b.start DESC
    """)
    List<Booking> findByBookerIdOrderByStartDesc(@Param("bookerId") long bookerId);

    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.bookerId = :bookerId
    AND b.end < :dateTime
    ORDER BY b.start DESC
    """)
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(@Param("bookerId") long bookerId,
                                                             @Param("dateTime") OffsetDateTime dateTime);

    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.bookerId = :bookerId
    AND b.start <= :dateTimeStart
    AND b.end > :dateTimeEnd
    ORDER BY b.start DESC
    """)
    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(@Param("bookerId") long bookerId,
                                                                          @Param("dateTimeStart") OffsetDateTime dateTimeStart,
                                                                          @Param("dateTimeEnd") OffsetDateTime dateTimeEnd);

    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.bookerId = :bookerId
    AND b.start >= :dateTime
    ORDER BY b.start DESC
    """)
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(@Param("bookerId") long bookerId,
                                                              @Param("dateTime") OffsetDateTime dateTime);

    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.bookerId = :bookerId
    AND b.status = :status
    ORDER BY b.start DESC
    """)
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(@Param("bookerId") long bookerId,
                                                          @Param("status") BookingStatus status);

    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.itemId IN :itemIds
    ORDER BY b.start DESC
    """)
    List<Booking> findByOwnerIdOrderByStartDesc(@Param("itemIds") List<Long> itemIds);

    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.itemId IN :itemIds
    AND b.end < :dateTime
    ORDER BY b.start DESC
    """)
    List<Booking> findByOwnerIdAndEndBeforeOrderByStartDesc(@Param("itemIds") List<Long> itemIds,
                                                            @Param("dateTime") OffsetDateTime dateTime);

    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.itemId IN :itemIds
    AND b.start <= :dateTimeStart
    AND b.end > :dateTimeEnd
    ORDER BY b.start DESC
    """)
    List<Booking> findByOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(@Param("itemIds") List<Long> itemIds,
                                                                          @Param("dateTimeStart") OffsetDateTime dateTimeStart,
                                                                          @Param("dateTimeEnd") OffsetDateTime dateTimeEnd);

    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.itemId IN :itemIds
    AND b.start > :dateTime
    ORDER BY b.start DESC
    """)
    List<Booking> findByOwnerIdAndStartAfterOrderByStartDesc(@Param("itemIds") List<Long> itemIds,
                                                             @Param("dateTime") OffsetDateTime dateTime);

    @Query("""
    SELECT b
    FROM Booking b
    WHERE b.itemId IN :itemIds
    AND b.status = :status
    ORDER BY b.start DESC
    """)
    List<Booking> findByOwnerIdAndStatusOrderByStartDesc(@Param("itemIds") List<Long> itemIds,
                                                         @Param("status") BookingStatus status);

    @Query("""
    SELECT new ru.project.booking.dto.BookingShortDto(b.id, b.start, b.end, b.bookerId, b.itemId)
    FROM Booking b
    WHERE b.itemId IN :itemIds
    """)
    List<BookingShortDto> getAllBookingForItems(@Param("itemIds") List<Long> itemIds);

    @Query("""
    SELECT new ru.project.booking.dto.BookingShortDto(b.id, b.start, b.end, b.bookerId, b.itemId)
    FROM Booking b
    WHERE b.bookerId = :userId
    AND b.itemId = :itemId
    AND b.end < :currentTime
    AND b.status = 'APPROVED'
    """)
    Optional<BookingShortDto> getBookingForComment(@Param("userId") long userId, @Param("itemId") long itemId, @Param("currentTime") OffsetDateTime currentTime);
}
