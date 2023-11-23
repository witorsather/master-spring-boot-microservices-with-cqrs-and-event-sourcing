package com.springbank.user.query.api.handlers;

import com.springbank.user.core.events.UserRegisteredEvent;
import com.springbank.user.core.events.UserRemovedEvent;
import com.springbank.user.core.events.UserUpdatedEvent;

public class UserEventHandlerImpl implements UserEventHandler {
    /**
     * @param event
     */
    @Override
    public void on(UserRegisteredEvent event) {

    }

    /**
     * @param event
     */
    @Override
    public void on(UserUpdatedEvent event) {

    }

    /**
     * @param event
     */
    @Override
    public void on(UserRemovedEvent event) {

    }
}
