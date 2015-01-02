package mgh14.search.live.service.messaging;

/**
 * Actions that can be taken by the application's
 * command executor class
 */
public enum CycleAction {
  START_SERVICE,
  PAUSE,
  RESUME,
  NEXT,
  PREVIOUS,
  SAVE,
  DELETE_RESOURCES,
  SHUTDOWN,
  NOP
}
