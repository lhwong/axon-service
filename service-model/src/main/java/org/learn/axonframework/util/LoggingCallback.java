package org.learn.axonframework.util;


import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class LoggingCallback<C, R> implements CommandCallback<C, R> {

    public static final LoggingCallback INSTANCE = new LoggingCallback();
    private static final Logger logger = LoggerFactory.getLogger(LoggingCallback.class);

    private LoggingCallback() {
    }

    /*@Override
    public void onSuccess(CommandMessage<? extends C> commandMessage, R result) {
        logger.info("Command successful: {} -> {}", commandMessage.getPayloadType().getSimpleName(), Objects.toString(result));
    }

    @Override
    public void onFailure(CommandMessage<? extends C> commandMessage, Throwable cause) {
        cause.printStackTrace();
        logger.info("Command failed: {} -> {}", commandMessage.getPayloadType().getSimpleName(),
                cause.getClass().getSimpleName());
    }*/

	@Override
	public void onResult(CommandMessage<? extends C> arg0, CommandResultMessage<? extends R> arg1) {
		// TODO Auto-generated method stub
		
	}

	
}