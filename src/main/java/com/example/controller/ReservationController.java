package com.example.controller;

import com.example.controller.exception.BadRequestException;
import com.example.model.Reservation;
import com.example.request.ReservationRequest;
import com.example.response.ReservationResponse;
import com.example.service.ReservationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    Reservation reservation = toReservation(reservationRequest);
    reservationService.createReservation(reservation);
    return new ResponseEntity<>(new ReservationResponse(reservation,null,Boolean.FALSE), HttpStatus.CREATED);
  }

  @ApiOperation(value = "Obtains a Reservation", response = ReservationResponse.class)
  @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Successfully created" ),
    @ApiResponse(code = 404, message = "not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Reservation> findById(@PathVariable(name = "id") String id){
    Reservation reservation = reservationService.findById(id);
    return new ResponseEntity<>(reservation,HttpStatus.OK);
  }

  private void validate(ReservationRequest reservationRequest){
    if(StringUtils.isBlank(reservationRequest.getEmail()) ||
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
                      .build();

  }

}
