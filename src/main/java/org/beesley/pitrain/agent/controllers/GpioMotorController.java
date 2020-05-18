package org.beesley.pitrain.agent.controllers;

import java.util.HashMap;
import java.util.Map;
import org.beesley.pitrain.agent.PiTrainStompSessionHandler;
import org.beesley.pitrain.models.MotorControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;

public class GpioMotorController extends BaseController implements MotorController {
  private static final Logger logger = LoggerFactory.getLogger(PiTrainStompSessionHandler.class);

  private final Map<Integer, GpioPinDigitalOutput> forwardPinMap =
      new HashMap<Integer, GpioPinDigitalOutput>();
  private final Map<Integer, GpioPinDigitalOutput> reversePinMap =
      new HashMap<Integer, GpioPinDigitalOutput>();
  private final Map<Integer, GpioPinPwmOutput> speedPinMap =
      new HashMap<Integer, GpioPinPwmOutput>();

  public GpioMotorController(GpioController gpioController, MotorControl[] motorControls) {
    for (MotorControl control : motorControls) {
      forwardPinMap.put(control.getId(), makeBinaryOutput(gpioController, control.getForwardPin()));
      reversePinMap.put(control.getId(), makeBinaryOutput(gpioController, control.getReversePin()));
      speedPinMap.put(control.getId(), makePwmOutput(gpioController, control.getSpeedPin()));
    }
  }

  @Override
  public void setState(MotorControl motorControl) {
    logger.info("ID:" + motorControl.getId());
    logger.info("Speed: " + motorControl.getSpeed());
    logger.info("Reversed: " + motorControl.isReversed());

    if (motorControl.isReversed()) {
      forwardPinMap.get(motorControl.getId()).low();
      reversePinMap.get(motorControl.getId()).high();
    } else {
      forwardPinMap.get(motorControl.getId()).high();
      reversePinMap.get(motorControl.getId()).low();
    }

    speedPinMap.get(motorControl.getId()).setPwm(motorControl.getSpeed());
  }
}
