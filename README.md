# si-utils
 
 Spring integration utilities

 Just helper utilities for spring integration use them if you want.

## log
 Logging is horrible. My main problem is that messages are logged using 
 same logger as spring logs .....hard to read debug messages.

 You can specify your slf4j logger instance if you want.

~~~
  private static Logger xlog = LoggerFactory.getLogger("flow.xml");
  .handle(SlfHandler.of(lg -> lg.debug(xlog, "xml file {} processing start ", lg.header(FileHeaders.FILENAME))))
~~~

## on/off message source

 Sometimes it is useful to stop message source activity. (As poller is constantly reading).

 You can customize message in this way
~~~
  return OnOffMessageSource.of(originalsource)
            .condition(() -> fs.isDestinationFolderEmpty());
~~~

## RealHeadDirectoryScanner

 if You reads large directories you can use this one 

~~~
  RealHeadDirectoryScanner scanner = new RealHeadDirectoryScanner(processBatchSize);
  scanner.setFilter(compositeFilter);
~~~

## AdhocMessageConsumer

 If You need to define adhoc message handler, which just process message data and 
 returns message to flow.

~~~
  .handle(AdhocMessageConsumer.consumeBy(m -> activeXmlCounter.increase()))  
~~~

## InactivityDelayMessageSource

 If You need to delay message source request after pulling null message from internal 
 message source.

~~~
   InactivityDelayMessageSource.of(source)
        .inactivityDelay(Duration.ofMinutes(1))
~~~

## MessageSourceBatchAction

 Batch is continuous sequence of non null messages given by delegated message 
 source. (sequence null, msg, msg, null, msg, null has two batches)
 
 This builder allows you to define action called before or after such batch.
 Action can be called as synchronous (default) or as asynchronous (new created 
 thread). All throwables from action code are ignored. 
 
 Start of batch is detected by non null message followed by null message. 
 Action is called before first batch message is returned.
 (null, null, (action) msg, msg, null, (action) msg, null, null)
 
 End of batch is detected when delegated mesage source return null after non 
 null message, So mesage source must be triggered after batch to obtain null 
 message. Action is called before first non null messafe after batch is 
 returned
 (null, null, msg, msg, (action) null, msg, (action) null, null)


~~~
  MessageSourceBatchAction.of(source)
       .async(true)
       .action(() -> {
               try { Thread.sleep(1000); } catch(Exception e) {}
               solr.softCommit();
            })
       .after();
~~~
