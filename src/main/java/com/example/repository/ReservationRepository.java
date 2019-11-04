package com.example.repository;

import com.example.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,String> {

  /**
   *  finds a reservation by its ID
   * @param id
   * @return
   */
  Optional<Reservation> findById(String id);

  /**
   *  retrieves all the reservations by a given time range
   * @param start
   * @param end
   * @return
   */
  List<Reservation>  findByStartDateBetween(LocalDateTime start, LocalDateTime end);

  /**
   * check wether a given date overlaps with other reservations or not
   * @param startDate, endDate
   * @return boolean
   */
  @Query(value = "SELECT COUNT(r) FROM RESERVATION r WHERE (START_DATE BETWEEN :startDate AND :endDate) OR (END_DATE BETWEEN :startDate AND :endDate)")
  Boolean reservationOverlapsWith(@Param("startDate")LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


  /**
   * Obtains all reservation for two given dates
   * @param startDate
   * @param endDate
   * @return
     */
  @Query(value = "SELECT r FROM RESERVATION r WHERE (START_DATE BETWEEN :startDate AND :endDate) OR (END_DATE BETWEEN :startDate AND :endDate)")
  List<Reservation> getAllReservationBetween(@Param("startDate")LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);



}
