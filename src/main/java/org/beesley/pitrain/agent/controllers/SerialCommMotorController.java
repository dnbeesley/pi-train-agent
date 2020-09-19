package org.beesley.pitrain.agent.controllers;

import java.io.IOException;
import org.beesley.pitrain.models.MotorControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fazecast.jSerialComm.SerialPort;

public class SerialCommMotorController implements MotorController {
  private static final Logger logger = LoggerFactory.getLogger(SerialCommMotorController.class);
  private final SerialPort serialPort;

  public SerialCommMotorController(SerialPort serialPort) {
    this.serialPort = serialPort;
  }

  @Override
  public synchronized void setState(MotorControl motorControl) throws IOException {
    logger.info("ID:" + motorControl.getId());
    logger.info("Speed: " + motorControl.getSpeed());
    logger.info("Reversed: " + motorControl.isReversed());

    byte value0 = (byte) (motorControl.getSpeed() / ((byte) 0x40));
    if (motorControl.isReversed()) {
      value0 += 0x04;
    }

    byte value1 = (byte) (motorControl.getSpeed() % ((byte) 0x40));
    byte[] output = new byte[] {0x01, motorControl.getChannel(), value0, value1, (byte) 0xFF};
    logger.debug("Writing {} to serial port.", output);
    this.serialPort.writeBytes(output, output.length);
    while (this.serialPort.bytesAwaitingWrite() > 0) {
    }
  }
}
