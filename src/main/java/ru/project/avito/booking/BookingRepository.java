package ru.project.avito.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.project.avito.booking.dto.BookingShortDto;
import ru.project.avito.booking.model.Booking;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
    SELECT b
    FROM Booking b
    JOIN FETCH b.item
    JOIN FETCH b.booker
    WHERE b.booker.id = :bookerId
    ORDER BY b.start DESC
    """)
    List<Booking> findByBookerIdOrderByStartDesc(@Param("bookerId") long bookerId);

    @Query("""
    SELECT b
    FROM Booking b
    JOIN FETCH b.item
    JOIN FETCH b.booker
    WHERE b.booker.id = :bookerId
    AND b.end < :dateTime
    ORDER BY b.start DESC
    """)
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(@Param("bookerId") long bookerId,
                                                             @Param("dateTime") OffsetDateTime dateTime);

    @Query("""
    SELECT b
    FROM Booking b
    JOIN FETCH b.item
    JOIN FETCH b.booker
    WHERE b.booker.id = :bookerId
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
    JOIN FETCH b.item
    JOIN FETCH b.booker
    WHERE b.booker.id = :bookerId
    AND b.start >= :dateTime
    ORDER BY b.start DESC
    """)
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(@Param("bookerId") long bookerId,
                                                              @Param("dateTime") OffsetDateTime dateTime);

    @Query("""
    SELECT b
    FROM Booking b
    JOIN FETCH b.item
    JOIN FETCH b.booker
    WHERE b.booker.id = :bookerId
    AND b.status = :status
    ORDER BY b.start DESC
    """)
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(@Param("bookerId") long bookerId,
                                                          @Param("status") BookingStatus status);

    @Query("""
    SELECT b
    FROM Booking b
    JOIN FETCH b.item
    JOIN FETCH b.booker
    WHERE b.item.owner.id = :ownerId
    ORDER BY b.start DESC
    """)
    List<Booking> findByOwnerIdOrderByStartDesc(@Param("ownerId") long ownerId);

    @Query("""
    SELECT b
    FROM Booking b
    JOIN FETCH b.item
    JOIN FETCH b.booker
    WHERE b.item.owner.id = :ownerId
    AND b.end < :dateTime
    ORDER BY b.start DESC
    """)
    List<Booking> findByOwnerIdAndEndBeforeOrderByStartDesc(@Param("ownerId") long ownerId,
                                                            @Param("dateTime") OffsetDateTime dateTime);

    @Query("""
    SELECT b
    FROM Booking b
    JOIN FETCH b.item
    JOIN FETCH b.booker
    WHERE b.item.owner.id = :ownerId
    AND b.start <= :dateTimeStart
    AND b.end > :dateTimeEnd
    ORDER BY b.start DESC
    """)
    List<Booking> findByOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(@Param("ownerId") long ownerId,
                                                                          @Param("dateTimeStart") OffsetDateTime dateTimeStart,
                                                                          @Param("dateTimeEnd") OffsetDateTime dateTimeEnd);

    @Query("""
    SELECT b
    FROM Booking b
    JOIN FETCH b.item
    JOIN FETCH b.booker
    WHERE b.item.owner.id = :ownerId
    AND b.start > :dateTime
    ORDER BY b.start DESC
    """)
    List<Booking> findByOwnerIdAndStartAfterOrderByStartDesc(@Param("ownerId") long ownerId,
                                                             @Param("dateTime") OffsetDateTime dateTime);

    @Query("""
    SELECT b
    FROM Booking b
    JOIN FETCH b.item
    JOIN FETCH b.booker
    WHERE b.item.owner.id = :ownerId
    AND b.status = :status
    ORDER BY b.start DESC
    """)
    List<Booking> findByOwnerIdAndStatusOrderByStartDesc(@Param("ownerId") long ownerId,
                                                         @Param("status") BookingStatus status);

    @Query("""
    SELECT new ru.project.avito.booking.dto.BookingShortDto(b.id, b.start, b.end, b.booker.id, b.item.id)
    FROM Booking b
    WHERE b.item.id IN :itemId
    AND b.status <> 'REJECTED'
    ORDER BY b.start DESC
    """)
    List<BookingShortDto> findByItemId(@Param("itemId") List<Long> itemId);

    @Query("""
    SELECT new ru.project.avito.booking.dto.BookingShortDto(b.id, b.start, b.end, b.booker.id, b.item.id)
    FROM Booking b
    WHERE b.booker.id = :bookerId
    AND b.item.id = :itemId
    AND b.end < :currentTime
    """)
    Optional<BookingShortDto> findByBookerId(@Param("bookerId") long bookerId, @Param("itemId") long itemId, @Param("currentTime") OffsetDateTime currentTime);
}
