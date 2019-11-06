package com.upgrade.camp.controller;

import com.upgrade.camp.aspect.LogExecutionTime;
import com.upgrade.camp.controller.exception.BadRequestException;
import com.upgrade.camp.controller.response.AvailableDaysResponse;
import com.upgrade.camp.model.Reservation;
import com.upgrade.camp.controller.request.ReservationRequest;
import com.upgrade.camp.controller.response.ReservationResponse;
import com.upgrade.camp.service.ReservationService;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@Api(value = "Booking Service", description = "Provides all operations related to a booking operation of the camp")
@RequestMapping("/reservation")
public class ReservationController {

  @Autowired
  private ReservationService reservationService;

  @ApiOperation(value = "Creates a Reservation", response = ReservationResponse.class)
  @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Successfully created"),
    @ApiResponse(code = 400, message = "Request does not meet conditions")
  })
  @PostMapping("")
  @LogExecutionTime
  public ResponseEntity<ReservationResponse> saveReservation(@RequestBody ReservationRequest reservationRequest){
    validate(reservationRequest);
    Reservation reservation = toReservation(reservationRequest);
    reservationService.createReservation(reservation);
    return new ResponseEntity<>(new ReservationResponse(reservation,null,Boolean.FALSE), HttpStatus.CREATED);
  }

  @ApiOperation(value = "Check available days for booking", response = AvailableDaysResponse.class)
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Ok"),
  })
  @GetMapping("/availability")
  @LogExecutionTime
  public ResponseEntity<AvailableDaysResponse> checkAvailability
          (@ApiParam(value = "start date of filter")@RequestParam(name = "startDate", required = false)
           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
           @ApiParam(value = "end date of filter")@RequestParam(name = "endDate", required = false)
           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate){
    LocalDateTime start = startDate == null ? getLocalDateTimeWith(12,0,0,0) : startDate.atTime(12,0, 0);
    LocalDateTime end = endDate == null ? start.plusMonths(1): endDate.atTime(12,0, 0);
    List<LocalDateTime> availableDays = reservationService.findAvailability(start.plusDays(1),end.plusDays(1));
    return new ResponseEntity<>(new AvailableDaysResponse(availableDays,null,Boolean.FALSE), HttpStatus.OK);
  }

  @ApiOperation(value = "Obtains a Reservation", response = ReservationResponse.class)
  @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Successfully created" ),
    @ApiResponse(code = 404, message = "Not found")
  })
  @GetMapping("/{id}")
  @LogExecutionTime
  public ResponseEntity<ReservationResponse> findById(@ApiParam(value = "Booking ID") @PathVariable(name = "id") String id){
    Reservation reservation = reservationService.findById(id);
    return new ResponseEntity<>(new ReservationResponse(reservation,null,Boolean.FALSE),HttpStatus.OK);
  }

  @ApiOperation(value = "Update a Reservation", response = ReservationResponse.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 204, message = "Not Found"),
    @ApiResponse(code = 400, message = "Bad Request")
  })
  @PutMapping("/{id}")
  @LogExecutionTime
  public ResponseEntity<ReservationResponse> updateReservation(@RequestBody ReservationRequest request,
       @ApiParam(value = "Booking ID") @PathVariable(name = "id") String id){
    validate(request);
    Reservation reservation = reservationService.updateReservation(request,id);
    return new ResponseEntity<>(new ReservationResponse(reservation,null,Boolean.FALSE),HttpStatus.OK);
  }

  @ApiOperation(value = "Deletes a Reservation")
  @ApiResponses(value = {
    @ApiResponse(code = 204, message = "No Content"),
    @ApiResponse(code = 404, message = "Not Found")
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  @LogExecutionTime
  public void deleteReservation(@ApiParam(value = "Booking ID") @PathVariable(name = "id") String id,
                                @ApiParam(value = "Email of the owner") @RequestParam(name = "email",required = false) String email){
    reservationService.deleteReservation(id,email);
  }

  private void validate(ReservationRequest reservationRequest){
    if(StringUtils.isBlank(reservationRequest.getEmail()) || StringUtils.isBlank(reservationRequest.getFullName()) ||
       reservationRequest.getStartDate() == null || reservationRequest.getEndDate() == null){
       throw new BadRequestException("All fields are mandatory");
    }
  }

  private Reservation toReservation(ReservationRequest reservationRequest){
    LocalDateTime startDate = getLocalDateTimeWith(reservationRequest.getStartDate(),12,0,1,0);
    LocalDateTime endDate = getLocalDateTimeWith(reservationRequest.getEndDate(),12,0,0,0);
    return Reservation.builder()
                      .bookingDate(LocalDateTime.now())
                      .startDate(startDate)
                      .endDate(endDate)
                      .email(reservationRequest.getEmail())
                      .uid(UUID.randomUUID().toString())
                      .fullName(reservationRequest.getFullName())
                      .build();

  }

  private LocalDateTime getLocalDateTimeWith(Integer hour,Integer min,Integer seconds,Integer nano){
    return LocalDateTime.now().withHour(hour).withMinute(min).withSecond(seconds).withNano(nano);
  }

  private LocalDateTime getLocalDateTimeWith(LocalDateTime target,Integer hour,Integer min,Integer seconds,Integer nano){
    return target.withHour(hour).withMinute(min).withSecond(seconds).withNano(nano);
  }

}
