package com.gameify.hook;

public interface GameifyHook {
    void register(String eventType, Runnable hook);
}
