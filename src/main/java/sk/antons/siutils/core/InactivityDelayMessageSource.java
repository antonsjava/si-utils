/*
 * Copyright 2023 Anton Straka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.antons.siutils.core;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.integration.IntegrationPatternType;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.Message;

/**
 * Makes delay after delegated message source returns null.
 * Useful if you have messagesource which produces many messages at once and 
 * then long time none. And you want to fetch messages often in time whend they 
 * are prepared. 
 * @author antons
 */
public class InactivityDelayMessageSource<T> implements MessageSource<T> {
    
    private MessageSource<T> delegate;
    private Duration delay;
    private LocalDateTime delayedTo;

    public InactivityDelayMessageSource(MessageSource<T> delegate) {
        this.delegate = delegate;
    }
    public InactivityDelayMessageSource inactivityDelay(Duration delay) { this.delay = delay; return this; }

    public static <P> InactivityDelayMessageSource<P> of(MessageSource<P> delegate) { return new InactivityDelayMessageSource(delegate); }

    @Override
    public Message<T> receive() {
        if(delayedTo != null) {
            if(delayedTo.isAfter(LocalDateTime.now())) {
                return null; // delayed message read
            } else {
                delayedTo = null;
            }
        }
        Message<T> message = delegate.receive();
        if(message == null) {
            delayedTo = LocalDateTime.now().plus(delay);
        }
        return message;
    }

    @Override
    public IntegrationPatternType getIntegrationPatternType() {
        return delegate.getIntegrationPatternType();
    }

    
}
