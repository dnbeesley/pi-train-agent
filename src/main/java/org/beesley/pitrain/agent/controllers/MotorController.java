package org.beesley.pitrain.agent.controllers;

import org.beesley.pitrain.models.MotorControl;

public interface MotorController {
  void setState(MotorControl motorControl);
}
