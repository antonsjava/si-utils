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
package sk.antons.siutils.file;

import java.io.File;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import sk.antons.siutils.handler.MessageConsumer;

/**
 * Releases file from AcceptOnceFileListFilter instance.
 * @author antons
 */
public class AcceptOnceFileReleaser extends MessageConsumer {
    
    private AcceptOnceFileListFilter<File> acceptOnceFilter;
    
    public AcceptOnceFileReleaser(AcceptOnceFileListFilter<File> acceptOnceFilter) {
        this.acceptOnceFilter = acceptOnceFilter;
    }
        
    public static AcceptOnceFileReleaser of(AcceptOnceFileListFilter acceptOnceFilter) { return new AcceptOnceFileReleaser(acceptOnceFilter); }

    @Override
    protected void accept(Message<?> message) throws MessagingException {
        File file = (File)message.getHeaders().get(FileHeaders.ORIGINAL_FILE);
        if(file != null) acceptOnceFilter.remove(file);
    }
    
}
