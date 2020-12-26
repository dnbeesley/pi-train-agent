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
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(final String[] args) throws IOException {
    final InputStream propertiesStream =
        App.class.getClassLoader().getResourceAsStream("application.properties");
    final Properties properties = new Properties();
    properties.load(propertiesStream);
    final String wsEndpoint = properties.getProperty("pitrain.broker.wsEndpoint");

    final WebSocketClient client = new StandardWebSocketClient();
    final WebSocketStompClient stompClient = new WebSocketStompClient(client);
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    final SerialPort serialPort =
        SerialPort.getCommPort(properties.getProperty("pitrain.controller.serialPort"));
    try {
      serialPort.openPort();
      final MotorController motorController = new SerialCommMotorController(serialPort);
      final TurnOutController turnOutController = new SerialCommTurnOutController(serialPort);
      final StompSessionHandler sessionHandler =
          new PiTrainStompSessionHandler(motorController, turnOutController);

      stompClient.connect(wsEndpoint, sessionHandler)
          .addCallback(new ListenableFutureCallback<StompSession>() {
            @Override
            public void onSuccess(final StompSession result) {
              logger.info("Connected to the broker.");
            }

            @Override
            public void onFailure(final Throwable ex) {
              logger.error("Error connecting to the broker.", ex);
              try {
                Thread.sleep(2000);
                stompClient.connect(wsEndpoint, sessionHandler).addCallback(this);
              } catch (final InterruptedException e) {
                e.printStackTrace();
              }
            }
          });

      try (final PipedOutputStream pipedOutputStream = new PipedOutputStream()) {
        try (final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream)) {

          serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
              return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(final SerialPortEvent event) {
              try {
                pipedOutputStream.write(event.getReceivedData());
              } catch (final IOException e) {
                logger.error("Failed to pipe output message", e);
              }
            }

          });

          try (final Scanner scanner = new Scanner(pipedInputStream)) {
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
