package org.beesley.pitrain.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Properties;
import java.util.Scanner;
import org.beesley.pitrain.agent.controllers.SerialCommMotorController;
import org.beesley.pitrain.agent.controllers.SerialCommTurnOutController;
import org.beesley.pitrain.agent.controllers.TurnOutController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.beesley.pitrain.agent.controllers.MotorController;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) throws IOException {
    InputStream propertiesStream =
        App.class.getClassLoader().getResourceAsStream("application.properties");
    Properties properties = new Properties();
    properties.load(propertiesStream);
    final String wsEndpoint = properties.getProperty("pitrain.broker.wsEndpoint");

    WebSocketClient client = new StandardWebSocketClient();
    WebSocketStompClient stompClient = new WebSocketStompClient(client);
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    SerialPort serialPort =
        SerialPort.getCommPort(properties.getProperty("pitrain.controller.serialPort"));
    try {
      serialPort.openPort();
      MotorController motorController = new SerialCommMotorController(serialPort);
      TurnOutController turnOutController = new SerialCommTurnOutController(serialPort);
      StompSessionHandler sessionHandler =
          new PiTrainStompSessionHandler(motorController, turnOutController);
      stompClient.connect(wsEndpoint, sessionHandler);

      try (PipedOutputStream pipedOutputStream = new PipedOutputStream()) {
        try (PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream)) {

          serialPort.addDataListener(new SerialPortDataListener() {

            @Override
            public int getListeningEvents() {
              return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
              try {
                pipedOutputStream.write(event.getReceivedData());
              } catch (IOException e) {
                logger.error("Failed to pipe output message", e);
              }
            }

          });

          try (Scanner scanner = new Scanner(pipedInputStream)) {
            while (true) {
              logger.debug(scanner.nextLine());
            }
          }
        }
      }
    } finally {
      serialPort.closePort();
    }
  }
}
