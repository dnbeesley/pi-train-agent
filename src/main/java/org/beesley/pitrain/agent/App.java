package org.beesley.pitrain.agent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.beesley.pitrain.agent.controllers.GpioMotorController;
import org.beesley.pitrain.agent.controllers.MotorController;
import org.beesley.pitrain.models.MotorControl;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

public class App {
  public static void main(String[] args) throws IOException {
    InputStream propertiesStream =
        App.class.getClassLoader().getResourceAsStream("/application.properties");
    Properties properties = new Properties();
    properties.load(propertiesStream);
    final String layoutBaseUrl = properties.getProperty("pitrain.broker.layoutBaseUrl");
    final String wsEndpoint = properties.getProperty("pitrain.broker.wsEndpoint");

    WebSocketClient client = new StandardWebSocketClient();
    WebSocketStompClient stompClient = new WebSocketStompClient(client);
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    GpioController gpioController = GpioFactory.getInstance();
    MotorControl[] motorContols = getMotorControls(layoutBaseUrl);
    MotorController motorController = new GpioMotorController(gpioController, motorContols);
    StompSessionHandler sessionHandler = new PiTrainStompSessionHandler(motorController);
    stompClient.connect(wsEndpoint, sessionHandler);

    try (Scanner scanner = new Scanner(System.in)) {
      scanner.nextLine();
    }
  }

  private static MotorControl[] getMotorControls(String layoutBaseUrl) throws IOException {
    try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
      HttpUriRequest httpRequest = new HttpGet(layoutBaseUrl + "/motor-control");
      try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response.getEntity().getContent(), MotorControl[].class);
      }
    }
  }
}
