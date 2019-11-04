package com.upgrade.camp.test;

import com.upgrade.camp.CampApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = CampApplication.class
)
@AutoConfigureMockMvc
public class TestApplicationTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	public void contextLoads() {
		try{
			MvcResult result = mockMvc.perform(
					MockMvcRequestBuilders.get("/greet/asd")
							.accept(MediaType.APPLICATION_JSON)
			).andReturn();
			System.out.println(result.getResponse().getContentAsString());
		}catch(Exception e) {

		}
	}

}