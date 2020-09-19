package org.beesley.pitrain.agent.controllers;

import java.io.IOException;
import org.beesley.pitrain.models.MotorControl;

public interface MotorController {
  void setState(MotorControl motorControl) throws IOException;
}
