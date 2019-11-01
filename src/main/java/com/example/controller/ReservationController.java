package com.example.controller;

import com.example.model.Reservation;
import com.example.request.ReservationRequest;
import com.example.service.ReservationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value = "Booking Service", description = "Provides all operations related to a booking operation of the camp")
@RequestMapping("/reservation")
public class ReservationController {

  @Autowired
  private ReservationService reservationService;

  @ApiOperation(value = "Creates a Reservation", response = Reservation.class)
  @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Successfully created"),
    @ApiResponse(code = 400, message = "Request does not meet conditions")
  })
  @PostMapping("")
  public ResponseEntity<Reservation> saveReservation(@RequestBody ReservationRequest reservation){
    //reservationService.createReservation(null);
    return new ResponseEntity<>(new Reservation(), HttpStatus.CREATED);
  }

  @ApiOperation(value = "obtains a Reservation", response = Reservation.class)
  @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Successfully created" ),
    @ApiResponse(code = 404, message = "not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Reservation> findById(@PathVariable(name = "id") String id){
    Reservation reservation = reservationService.findById(id);
    return new ResponseEntity<>(reservation,HttpStatus.OK);
  }


  private Reservation toReservation(ReservationRequest reservationRequest){

  }

}
