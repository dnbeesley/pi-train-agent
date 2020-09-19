package org.beesley.pitrain.agent;

import java.io.IOException;
import java.lang.reflect.Type;
import org.beesley.pitrain.agent.controllers.MotorController;
import org.beesley.pitrain.agent.controllers.TurnOutController;
import org.beesley.pitrain.models.MotorControl;
import org.beesley.pitrain.models.TurnOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class PiTrainStompSessionHandler extends StompSessionHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(PiTrainStompSessionHandler.class);

  private final MotorController motorController;
  private final TurnOutController turnOutController;

  public PiTrainStompSessionHandler(MotorController motorController, TurnOutController turnOutController) {
    this.motorController = motorController;
    this.turnOutController = turnOutController;
  }

  @Override
  public Type getPayloadType(StompHeaders headers) {
    if (headers.getDestination().equals("/topic/motor-control")) {
      return MotorControl.class;
    } else if (headers.getDestination().equals("/topic/turn-out")) {
      return TurnOut.class;
    } else {
      return Object.class;
    }
  }

  @Override
  public void handleFrame(StompHeaders headers, Object payload) {
    if (payload instanceof MotorControl) {
      try {
        this.motorController.setState((MotorControl) payload);
      } catch (IOException e) {
        logger.error("Error sending command to controller.", e);
      }
    } else if (payload instanceof TurnOut) {
      try {
        this.turnOutController.setState((TurnOut) payload);
      } catch (IOException e) {
        logger.error("Error sending command to controller.", e);
      }
    } else {
      logger.info("Invalid destination: " + headers.getSubscription());
    }
  }

  @Override
  public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
    session.subscribe("/topic/motor-control", this);
    session.subscribe("/topic/turn-out", this);
  }

  @Override
  public void handleException(StompSession session, StompCommand command, StompHeaders headers,
      byte[] payload, Throwable exception) {
    logger.error("Error in handling webscokets", exception);
  }
}
