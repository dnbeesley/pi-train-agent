package org.beesley.pitrain.agent.controllers;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public abstract class BaseController {
  protected static GpioPinDigitalOutput makeBinaryOutput(GpioController gpioController, byte pinNumber) {
    Pin pin = parsePin(pinNumber);
    GpioPinDigitalOutput outputPin = gpioController.provisionDigitalOutputPin(pin);
    outputPin.setShutdownOptions(true, PinState.LOW);
    return outputPin;
  }

  protected static GpioPinPwmOutput makePwmOutput(GpioController gpioController, byte pinNumber) {
    Pin pin = parsePin(pinNumber);
    GpioPinPwmOutput outputPin = gpioController.provisionPwmOutputPin(pin);
    outputPin.setShutdownOptions(true, PinState.LOW);
    return outputPin;
  }
  
  protected static Pin parsePin(byte pinNumber) {
    switch (pinNumber) {
      case 1:
        return RaspiPin.GPIO_01;
      case 2:
        return RaspiPin.GPIO_02;
      case 3:
        return RaspiPin.GPIO_03;
      case 4:
        return RaspiPin.GPIO_04;
      case 5:
        return RaspiPin.GPIO_05;
      case 6:
        return RaspiPin.GPIO_06;
      case 7:
        return RaspiPin.GPIO_07;
      case 8:
        return RaspiPin.GPIO_08;
      case 9:
        return RaspiPin.GPIO_09;
      case 10:
        return RaspiPin.GPIO_10;
      case 11:
        return RaspiPin.GPIO_11;
      case 12:
        return RaspiPin.GPIO_12;
      case 13:
        return RaspiPin.GPIO_13;
      case 14:
        return RaspiPin.GPIO_14;
      case 15:
        return RaspiPin.GPIO_15;
      case 16:
        return RaspiPin.GPIO_16;
      case 17:
        return RaspiPin.GPIO_17;
      case 18:
        return RaspiPin.GPIO_18;
      case 19:
        return RaspiPin.GPIO_19;
      case 20:
        return RaspiPin.GPIO_20;
      default:
        return RaspiPin.GPIO_00;
    }
  }
}