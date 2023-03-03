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

import org.springframework.integration.IntegrationPatternType;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.Message;

/**
 * Batch is continuous sequence of non null messages given by delegated message 
 * source. (sequence null, msg, msg, null, msg, null has two batches)
 * 
 * This builder allows you to define action called before or after such batch.
 * Action can be called as synchronous (default) or as asynchronous (new created 
 * thread). All throwables from action code are ignored. 
 * 
 * Start of batch is detected by non null message followed by null message. 
 * Action is called before first batch message is returned.
 * (null, null, (action) msg, msg, null, (action) msg, null, null)
 * 
 * End of batch is detected when delegated mesage source return null after non 
 * null message, So mesage source must be triggered after batch to obtain null 
 * message. Action is called before first non null messafe after batch is 
 * returned
 * (null, null, msg, msg, (action) null, msg, (action) null, null)
 * 
 * 
 * @author antons
 */
public class MessageSourceBatchAction<T> {
    
    private MessageSource<T> delegate;
    private Runnable action;
    private boolean async = false;

    boolean lastNull = true;

    public MessageSourceBatchAction(MessageSource<T> delegate) {
        this.delegate = delegate;
    }
    
    public static <P> MessageSourceBatchAction<P> of(MessageSource<P> delegate) { return new MessageSourceBatchAction(delegate); }
    
    public MessageSourceBatchAction action(Runnable action) { this.action = action; return this; }
    public MessageSourceBatchAction async(boolean async) { this.async = async; return this; }

    public MessageSource<T> before() {
        return BeforeBatchActionMessageSource.of(delegate)
                .action(action)
                .async(async);
    }
    
    public MessageSource<T> after() {
        return AfterBatchActionMessageSource.of(delegate)
                .action(action)
                .async(async);
    }





    /**
     * Batch is continuous sequence of non null messages given by message source.
     * (sequence null, msg, msg, null, msg, null has two batches)
     * 
     * This wrapper allows you to define action called before such sequence is 
     * started. 
     * (sequence null, (action) msg, msg, null, (action) msg, null)
     * 
     * Action can be called synchronous (default) or asynchronous (new created thread).
     * All throwables from action code are ignored. 
     * 
     * @author antons
     */
    private static class BeforeBatchActionMessageSource<T> implements MessageSource<T> {
        
        private MessageSource<T> delegate;
        private Runnable action;
        private boolean async = false;

        boolean lastNull = true;

        public BeforeBatchActionMessageSource(MessageSource<T> delegate) {
            this.delegate = delegate;
        }
        public BeforeBatchActionMessageSource action(Runnable action) { this.action = action; return this; }
        public BeforeBatchActionMessageSource async(boolean async) { this.async = async; return this; }

        public static <P> BeforeBatchActionMessageSource<P> of(MessageSource<P> delegate) { return new BeforeBatchActionMessageSource(delegate); }

        @Override
        public Message<T> receive() {
            Message<T> message = delegate.receive();
            if(action != null) {
                if(message != null) {
                    if(lastNull) {
                        if(async) {
                            try {
                                Thread t = new Thread(action);
                                t.start();
                            } catch(Throwable e) {
                            }
                        } else {
                            try {
                                action.run();
                            } catch(Throwable e) {
                            }
                        }
                    }
                }
            }
            lastNull = message == null;
            return message;
        }

        @Override
        public IntegrationPatternType getIntegrationPatternType() {
            return delegate.getIntegrationPatternType();
        }
    }


    /**
     * Batch is continuous sequence of non null messages given by message source.
     * (sequence null, msg, msg, null, msg, null has two batches)
     * 
     * This wrapper allows you to define action called after such sequence is 
     * finished. End of batch is detected when delegated mesage source return null, 
     * so mesage source must be triggered after batch to obtain null message. 
     * (sequence null, msg, msg, (action) null, msg, (action) null)
     * 
     * Action can be called synchronous (default) or asynchronous (new created thread).
     * All throwables from action code are ignored. 
     * 
     * @author antons
     */
    private static class AfterBatchActionMessageSource<T> implements MessageSource<T> {
        
        private MessageSource<T> delegate;
        private Runnable action;
        private boolean async = false;

        boolean lastNull = true;

        public AfterBatchActionMessageSource(MessageSource<T> delegate) {
            this.delegate = delegate;
        }
        public AfterBatchActionMessageSource action(Runnable action) { this.action = action; return this; }
        public AfterBatchActionMessageSource async(boolean async) { this.async = async; return this; }
        
        public static <P> AfterBatchActionMessageSource<P> of(MessageSource<P> delegate) { return new AfterBatchActionMessageSource(delegate); }

        @Override
        public Message<T> receive() {
            Message<T> message = delegate.receive();
            if(action != null) {
                if(message == null) {
                    if(!lastNull) {
                        if(async) {
                            try {
                                Thread t = new Thread(action);
                                t.start();
                            } catch(Throwable e) {
                            }
                        } else {
                            try {
                                action.run();
                            } catch(Throwable e) {
                            }
                        }
                    }
                }
            }
            lastNull = message == null;
            return message;
        }

        @Override
        public IntegrationPatternType getIntegrationPatternType() {
            return delegate.getIntegrationPatternType();
        }

        
    }
}
