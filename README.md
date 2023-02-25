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
