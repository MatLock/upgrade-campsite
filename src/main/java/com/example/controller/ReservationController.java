package com.example.controller;

import com.example.controller.exception.BadRequestException;
import com.example.controller.response.AvailableDaysResponse;
import com.example.controller.response.BasicResponse;
import com.example.model.Reservation;
import com.example.controller.request.ReservationRequest;
import com.example.controller.response.ReservationResponse;
import com.example.service.ReservationService;
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
  @GetMapping("")
  public ResponseEntity<AvailableDaysResponse> checkAvailability
          (@ApiParam(value = "start date of filter")@RequestParam(name = "startDate", required = false)
           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
           @ApiParam(value = "end date of filter")@RequestParam(name = "endDate", required = false)
           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate){
    LocalDateTime start = startDate == null ? LocalDateTime.now() : startDate.atTime(12,00, 00);
    LocalDateTime end = endDate == null ? start.plusMonths(1): endDate.atTime(12,00, 00);
    List<LocalDateTime> availableDays = reservationService.findAvailability(start.plusDays(1),end.plusDays(1));
    return new ResponseEntity<>(new AvailableDaysResponse(availableDays,null,Boolean.FALSE), HttpStatus.CREATED);
  }

  @ApiOperation(value = "Obtains a Reservation", response = ReservationResponse.class)
  @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Successfully created" ),
    @ApiResponse(code = 404, message = "Not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Reservation> findById(@ApiParam(value = "Booking ID") @PathVariable(name = "id") String id){
    Reservation reservation = reservationService.findById(id);
    return new ResponseEntity<>(reservation,HttpStatus.OK);
  }

  @ApiOperation(value = "Update a Reservation", response = ReservationResponse.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 204, message = "Not Found"),
    @ApiResponse(code = 400, message = "Bad Request")
  })
  @PutMapping("/{id}")
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
    LocalDateTime startDate = reservationRequest.getStartDate().withHour(12).withMinute(0).withSecond(1);
    LocalDateTime endDate = reservationRequest.getEndDate().withHour(12).withMinute(0).withSecond(0);
    return Reservation.builder()
                      .bookingDate(LocalDateTime.now())
                      .startDate(startDate)
                      .endDate(endDate)
                      .email(reservationRequest.getEmail())
                      .uid(UUID.randomUUID().toString())
                      .fullName(reservationRequest.getFullName())
                      .build();

  }

}
