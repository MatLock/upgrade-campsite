package integration.test;

import com.google.gson.*;
import com.google.gson.JsonDeserializer;
import com.upgrade.camp.CampApplication;
import com.upgrade.camp.controller.request.ReservationRequest;
import com.upgrade.camp.controller.response.AvailableDaysResponse;
import com.upgrade.camp.controller.response.ReservationResponse;
import com.upgrade.camp.model.Reservation;
import com.upgrade.camp.repository.ReservationRepository;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static java.lang.String.format;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = CampApplication.class
)
@AutoConfigureMockMvc
public class CampApplicationTests {

  private static final String EMAIL = "email@email.com";
  private static final String FULL_NAME = "full name";
  private static final String MODEL_CONSTRAINT_EX = "Camp reservation days cannot be greater than 3 days";
  private static final String OVERLAPS_EX = "Cannot book, due conflicts with other reservations";
  private static final String NOT_FOUND_EX = "Reservation not found";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ReservationRepository reservationRepository;
  private Gson gson;

  private ReservationRequest reservationRequest;

  @Before
  public void setUp(){
    gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class,new CustomJsonDeserializer())
                            .registerTypeAdapter(LocalDateTime.class,new CustomJsonSerializer())
                            .create();
    reservationRequest = ReservationRequest.builder()
                                           .startDate(LocalDateTime.now().plusDays(2))
                                           .endDate(LocalDateTime.now().plusDays(4))
                                           .email(EMAIL)
                                           .fullName(FULL_NAME)
                                           .build();
  }

  @After
  @SneakyThrows
  public void cleanUp(){
    reservationRepository.truncate();
    Thread.sleep(1000);
  }

  @Test
  @SneakyThrows
  public void checkAvailabilityWithoutQueryParamTest() {
    MvcResult result = checkAvailability();
    AvailableDaysResponse response = gson.fromJson(result.getResponse().getContentAsString(),AvailableDaysResponse.class);
	assertThat(response.getMessage(),is(nullValue()));
    assertThat(response.getResponse().size(),is(30));
    assertThat(response.getError(),is(Boolean.FALSE));
    assertThat(result.getResponse().getStatus(),is(HttpStatus.OK.value()));
  }

  @Test
  @SneakyThrows
  public void checkAvailabilityWithReservationsCreatedTest(){
    createReservation();
    MvcResult result = checkAvailability();
    AvailableDaysResponse response = gson.fromJson(result.getResponse().getContentAsString(),AvailableDaysResponse.class);
    assertThat(response.getMessage(),is(nullValue()));
    assertThat(response.getResponse().size(),is(28));
    assertThat(response.getError(),is(Boolean.FALSE));
    assertThat(result.getResponse().getStatus(),is(HttpStatus.OK.value()));
  }

  @Test
  @SneakyThrows
  public void testCreateReservationTest(){
    MvcResult result = createReservation();
    ReservationResponse reservationResponse = gson.fromJson(result.getResponse().getContentAsString(),ReservationResponse.class);
    Reservation r = reservationResponse.getResponse();
    assertThat(result.getResponse().getStatus(),is(HttpStatus.CREATED.value()));
    assertThat(r,is(notNullValue()));
    assertThat(r.getEmail(),is(EMAIL));
    assertThat(r.getFullName(),is(FULL_NAME));
    assertThat(r.getUid(),isA(String.class));
    assertThat(reservationResponse.getError(),is(Boolean.FALSE));
    assertThat(reservationResponse.getMessage(),is(nullValue()));
  }

  @Test
  @SneakyThrows
  public void createWithRequestNotValidTest(){
    reservationRequest.setEndDate(LocalDateTime.now().plusMonths(3));
    MvcResult result = createReservation();
    ReservationResponse reservationResponse = gson.fromJson(result.getResponse().getContentAsString(),ReservationResponse.class);
    assertThat(result.getResponse().getStatus(),is(HttpStatus.BAD_REQUEST.value()));
    assertThat(reservationResponse.getResponse(),is(nullValue()));
    assertThat(reservationResponse.getError(),is(Boolean.TRUE));
    assertThat(reservationResponse.getMessage(),is(MODEL_CONSTRAINT_EX));
  }

  @Test
  @SneakyThrows
  public void createWithReservationThatOverlapsWithAnotherTest(){
    createReservation();
    reservationRequest.setStartDate(LocalDateTime.now().plusDays(3));
    reservationRequest.setEndDate(LocalDateTime.now().plusDays(6));
    MvcResult result = createReservation();
    ReservationResponse reservationResponse = gson.fromJson(result.getResponse().getContentAsString(),ReservationResponse.class);
    assertThat(result.getResponse().getStatus(),is(HttpStatus.BAD_REQUEST.value()));
    assertThat(reservationResponse.getResponse(),is(nullValue()));
    assertThat(reservationResponse.getError(),is(Boolean.TRUE));
    assertThat(reservationResponse.getMessage(),is(OVERLAPS_EX));
  }

  @Test
  @SneakyThrows
  public void deleteReservationTest(){
    MvcResult creationResult = createReservation();
    String uid = gson.fromJson(creationResult.getResponse().getContentAsString(),ReservationResponse.class).getResponse().getUid();
    MvcResult result = deleteReservation(uid,EMAIL);
    assertThat(result.getResponse().getStatus(),is(HttpStatus.NO_CONTENT.value()));
  }

  @Test
  @SneakyThrows
  public void deleteReservationNotFound(){
    MvcResult result = deleteReservation(UUID.randomUUID().toString(),EMAIL);
    ReservationResponse reservationResponse =  gson.fromJson(result.getResponse().getContentAsString(),ReservationResponse.class);
    assertThat(result.getResponse().getStatus(),is(HttpStatus.NOT_FOUND.value()));
    assertThat(reservationResponse.getError(),is(Boolean.TRUE));
    assertThat(reservationResponse.getResponse(),is(nullValue()));
    assertThat(reservationResponse.getMessage(),is(NOT_FOUND_EX));
  }

  @Test
  @SneakyThrows
  public void findByIdTest(){
    MvcResult result = createReservation();
    ReservationResponse reservationResponse =  gson.fromJson(result.getResponse().getContentAsString(),ReservationResponse.class);
    String uid = reservationResponse.getResponse().getUid();
    MvcResult findByIdResult = findById(uid);
    ReservationResponse reservationResponseFound =  gson.fromJson(findByIdResult.getResponse().getContentAsString(),ReservationResponse.class);
    assertThat(reservationResponseFound.getResponse().getUid(),is(uid));
    assertThat(findByIdResult.getResponse().getStatus(),is(HttpStatus.OK.value()));
  }

  @Test
  @SneakyThrows
  public void concurrentTest(){
    CompletableFuture<MvcResult> future1 = CompletableFuture.supplyAsync(() -> createReservation());
    CompletableFuture<MvcResult> future2 = CompletableFuture.supplyAsync(() -> createReservation());
    assertsOnConcurrentTest(future1.get(),future2.get());
  }

  @SneakyThrows
  private MvcResult checkAvailability(){
    return mockMvc.perform(MockMvcRequestBuilders.get("/reservation")
                                                 .accept(MediaType.APPLICATION_JSON))
                                                 .andReturn();
  }

  @SneakyThrows
  private MvcResult findById(String uid){
    return mockMvc.perform(MockMvcRequestBuilders.get(format("/reservation/%s",uid))
                                                 .accept(MediaType.APPLICATION_JSON))
                                                 .andReturn();
    }

  @SneakyThrows
  private MvcResult createReservation(){
    return mockMvc.perform(MockMvcRequestBuilders.post("/reservation")
                                                 .content(gson.toJson(reservationRequest))
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .accept(MediaType.APPLICATION_JSON)).andReturn();
  }

  @SneakyThrows
  private MvcResult deleteReservation(String uid,String email){
    return mockMvc.perform(MockMvcRequestBuilders.delete(format("/reservation/%s",uid))
              .param("email",email)
              .accept(MediaType.APPLICATION_JSON))
              .andReturn();
  }

    /**
     * there is no way you can tell which is the successful one  and which is the failed one
     * so it is needed to contemplate both result
     * @param r1
     * @param r2
     */
  @SneakyThrows
  private void assertsOnConcurrentTest(MvcResult r1, MvcResult r2){
    int r1Status = r1.getResponse().getStatus();
    int r2Status = r2.getResponse().getStatus();
    if(r1Status == r2Status){
     fail("Concurrent call didn't work!!!");
    }
    if (r1Status == 201){
      assertSuccessful(r1);
      assertFailed(r2);
      return;
    }
    assertSuccessful(r2);
    assertFailed(r1);
  }

  @SneakyThrows
  private void assertSuccessful(MvcResult result){
    ReservationResponse reservationResponse =  gson.fromJson(result.getResponse().getContentAsString(),ReservationResponse.class);
    assertThat(reservationResponse.getError(),is(Boolean.FALSE));
    assertThat(reservationResponse.getResponse(),is(notNullValue()));
    assertThat(reservationResponse.getMessage(),is(nullValue()));
  }
  @SneakyThrows
  private  void assertFailed(MvcResult result){
    ReservationResponse reservationResponse2 =  gson.fromJson(result.getResponse().getContentAsString(),ReservationResponse.class);
    assertThat(reservationResponse2.getResponse(),is(nullValue()));
    assertThat(reservationResponse2.getError(),is(Boolean.TRUE));
    assertThat(reservationResponse2.getMessage(),is(OVERLAPS_EX));
  }
}

class CustomJsonSerializer implements JsonSerializer<LocalDateTime>{
  @Override
  public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext){
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return new JsonPrimitive(localDateTime.format(formatter));
  }
}

class CustomJsonDeserializer implements JsonDeserializer<LocalDateTime>{
  @Override
  public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(),formatter);
  }
}

