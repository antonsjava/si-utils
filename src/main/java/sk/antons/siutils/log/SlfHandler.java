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
package sk.antons.siutils.log;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import java.util.function.Function;
import org.slf4j.Logger;
import org.springframework.messaging.MessageHeaders;
import sk.antons.siutils.handler.MessageConsumer;

/**
 * API for logging in flows. It uses given slf4j logger instance so it is 
 * possible to configure logging more effective way.
 * <pre>
 * use form:
 *   .handler(SlfHandler.of(lg -> lg.debug(log, "message {}", lg.message())))
 * </pre>
 * @author antons
 */
public class SlfHandler extends MessageConsumer {

    @Override
    protected void accept(Message<?> message) throws MessagingException {
        if(lgfunction != null) lgfunction.apply(Lg.of(message));
    }
    

    Function<Lg, Lg> lgfunction;

    public SlfHandler(Function<Lg, Lg> lgfunction) {
        this.lgfunction = lgfunction;
    }

    public static SlfHandler of(Function<Lg, Lg> lgfunction) { return new SlfHandler(lgfunction); }
    
    public static class Lg {
        private Message<?> message;
        public Lg(Message<?> message) {
            this.message = message;
        }
        public static Lg of(Message<?> message) { return new Lg(message); }

        public Message<?> message() { return message; }
        public Object payload() { return message.getPayload(); }
        public MessageHeaders header() { return message.getHeaders(); }
        public Object header(Object key) { return message.getHeaders().get(key); }
        
        public Lg trace(Logger log, String message, Object... params) { log.trace(message, params); return this; }
        public Lg debug(Logger log, String message, Object... params) { log.debug(message, params); return this; }
        public Lg info(Logger log, String message, Object... params) { log.info(message, params); return this; }
        public Lg warn(Logger log, String message, Object... params) { log.warn(message, params); return this; }
        public Lg error(Logger log, String message, Object... params) { log.error(message, params); return this; }
    }
}
