package mgh14.search.live.service.messaging;

/**
 * Actions that can be taken by the application
 * cycle loop
 */
public enum CycleAction {
  START_SERVICE,
  PAUSE,
  RESUME,
  STOP,
  NEXT,
  PREVIOUS,
  SAVE,
  SHUTDOWN,
  NOP
}
