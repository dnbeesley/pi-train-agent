package org.beesley.pitrain.agent;

import java.lang.reflect.Type;
import org.beesley.pitrain.agent.controllers.MotorController;
import org.beesley.pitrain.models.MotorControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class PiTrainStompSessionHandler extends StompSessionHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(PiTrainStompSessionHandler.class);

  private final MotorController motorController;

  public PiTrainStompSessionHandler(MotorController motorController) {
    this.motorController = motorController;
  }

  @Override
  public Type getPayloadType(StompHeaders headers) {
    if (headers.getDestination().equals("/topic/motor-control")) {
      return MotorControl.class;
    } else {
      return Object.class;
    }
  }

  @Override
  public void handleFrame(StompHeaders headers, Object payload) {
    if (payload instanceof MotorControl) {
      motorController.setState((MotorControl) payload);
    } else {
      logger.info("Invalid destination: " + headers.getSubscription());
    }
  }

  @Override
  public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
    session.subscribe("/topic/motor-control", this);
  }

  @Override
  public void handleException(StompSession session, StompCommand command, StompHeaders headers,
      byte[] payload, Throwable exception) {
    logger.error("Error in handling webscokets", exception);
  }
}
