package com.example.repository;

import com.example.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,String> {

  /**
   *  finds a reservation by a given email
   * @param email
   * @return
   */
  Optional<Reservation> findByEmail(String email);

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
   * @param date
   * @return boolean
   */
  Boolean existsByStartDateAfterAndEndDateBefore(LocalDateTime date);


}
