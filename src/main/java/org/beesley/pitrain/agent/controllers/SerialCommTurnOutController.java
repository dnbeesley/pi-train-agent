package org.beesley.pitrain.agent.controllers;

import java.io.IOException;
import org.beesley.pitrain.models.TurnOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fazecast.jSerialComm.SerialPort;

public class SerialCommTurnOutController implements TurnOutController {
  private static final Logger logger = LoggerFactory.getLogger(SerialCommTurnOutController.class);
  private final SerialPort serialPort;

  public SerialCommTurnOutController(final SerialPort serialPort) {
    this.serialPort = serialPort;
  }

  @Override
  public void setState(final TurnOut turnOut) throws IOException {
    final byte[] output = new byte[] {0x02, turnOut.isTurnedOut()? turnOut.getTurnOutPin() : turnOut.getForwardPin(), (byte) 0xFF};
    logger.debug("Writing {} to serial port.", output);
    this.serialPort.writeBytes(output, output.length);
    while (this.serialPort.bytesAwaitingWrite() > 0) {
    }
  }
}
