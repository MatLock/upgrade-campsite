package com.upgrade.camp.repository;

import com.upgrade.camp.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,String> {


  /**
   * Only for Integration Test
   */
  @Transactional
  @Modifying
  @Query(value = "DELETE FROM RESERVATION",nativeQuery = true)
  void truncate();
  /**
   *  finds a reservation by its ID
   * @param id
   * @return
   */
  Optional<Reservation> findById(String id);

  /**
   * check wether a given date overlaps with other reservations or not
   * @param startDate, endDate
   * @return boolean
   */
  @Query(value = "SELECT COUNT(*) FROM RESERVATION r WHERE (START_DATE BETWEEN :startDate AND :endDate) OR (END_DATE BETWEEN :startDate AND :endDate)", nativeQuery = true)
  Long countReservationThatOverlapsWith(@Param("startDate")LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


  /**
   * Obtains all reservation for two given dates
   * @param startDate
   * @param endDate
   * @return
     */
  @Query(value = "SELECT r FROM RESERVATION r WHERE (START_DATE BETWEEN :startDate AND :endDate) OR (END_DATE BETWEEN :startDate AND :endDate)")
  List<Reservation> getAllReservationBetween(@Param("startDate")LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);



}
