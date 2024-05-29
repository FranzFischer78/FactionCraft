package com.infamousmisadventures.factioncraft.platform.services;

import com.infamousmisadventures.factioncraft.event.FCEvent;

public interface IEventBus {
    void post(FCEvent event);
}
