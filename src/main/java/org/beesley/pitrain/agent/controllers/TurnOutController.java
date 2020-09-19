package org.beesley.pitrain.agent.controllers;

import java.io.IOException;
import org.beesley.pitrain.models.TurnOut;

public interface TurnOutController {
  void setState(TurnOut motorControl) throws IOException;
}
