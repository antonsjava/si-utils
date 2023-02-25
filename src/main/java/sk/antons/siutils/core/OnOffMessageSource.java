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

import java.util.function.BooleanSupplier;
import org.springframework.integration.IntegrationPatternType;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.Message;

/**
 * Delegate MessageSource functionality to given messageSource only if given
 * condition return true; (if there is no condition functionality is delegated 
 * always)
 * @author antons
 */
public class OnOffMessageSource<T> implements MessageSource<T> {
    
    private MessageSource<T> delegate;
    private BooleanSupplier condition;

    public OnOffMessageSource(MessageSource<T> delegate) {
        this.delegate = delegate;
    }
    public OnOffMessageSource condition(BooleanSupplier condition) { this.condition = condition; return this; }

    public static <P> OnOffMessageSource<P> of(MessageSource<P> delegate) { return new OnOffMessageSource(delegate); }

    @Override
    public Message<T> receive() {
        return ((condition == null) || condition.getAsBoolean()) ? delegate.receive() : null;
    }

    @Override
    public IntegrationPatternType getIntegrationPatternType() {
        return delegate.getIntegrationPatternType();
    }
 

    
}
