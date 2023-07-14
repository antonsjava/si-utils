/*
 *
 */
package sk.antons.siutils.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.messaging.Message;

/**
 * Reads files from given directory one by one.
 * @author antons
 */
public class FileByFileMessageSource implements MessageSource<File> {
    public static final String FILE = "FileByFileMessageSource.FILE";


    private File inputDirectory;
    private File backupDirectory;
    private Comparator<File> comparator = FileNameComparator.instance();

    private File currentFile = null;
    private boolean processing = false;
    private LocalDateTime processingFrom = null;
    private boolean locked = false;

    public FileByFileMessageSource(String directory) { this.inputDirectory = createDirectory(directory); }
    public static FileByFileMessageSource of(String directory) { return new FileByFileMessageSource(directory); }

    public FileByFileMessageSource backupDirectory(String directory) { this.backupDirectory = createDirectory(directory); return this; }
    public FileByFileMessageSource comparator(Comparator<File> value) { this.comparator = value; return this; }

    public boolean isProcessing() { return processing; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean value) { this.locked = value; }


    /**
     * Reads one file from given folder. You can specify order of files. (by
     * default they are sorted by name.)
     *
     * Each returned file must be confirmed before another file is provided.
     * @return message with file or null if previous file is not confirmed or
     * no file exists.
     */
    @Override
    public Message<File> receive() {
        File f = nextFile();
        if(f == null) return null;
        return getMessageBuilderFactory()
                        .withPayload(f)
                        .copyHeaders(Map.of(
                                FILE, f
                            ))
                        .setCorrelationId(f.getName())
                        .build();
    }

    /**
     * Delete last provided file from directory and allow next file to be provided.
     */
    public void delete() {
        try {
            if((currentFile != null) && currentFile.exists()) { currentFile.delete(); }
        } catch(Exception e) {
            throw new IllegalStateException(e);
        } finally {
            cleanFile();
        }
    }

    /**
     * Move last provided file to specified backup directory and allow next file to be provided.
     */
    public void move() {
        if(backupDirectory == null) throw new IllegalStateException("unable to move file, backupDirectory is null");
        try {
            if((currentFile != null) && currentFile.exists()) {
                File destination = new File(backupDirectory.getAbsolutePath()
                        +  "/" + currentFile.getName());
                    Files.move(currentFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch(Exception e) {
            throw new IllegalStateException(e);
        } finally {
            cleanFile();
        }
    }

    /**
     * Allow next file to be provided.
     */
    public void reset() {
        cleanFile();
    }

    /**
     * Stets state to locked. no reading is done.
     */
    public void lock() {
        this.locked = true;
    }

    /**
     * Stets state to unlocked. reading is allowed.
     */
    public void unlock() {
        this.locked = false;
    }

    /**
     * Returns state of message source
     */
    public State state() {
        State state = new State();
        File file = this.currentFile;
        state.currentFile = file == null ? null : file.getName();
        state.locked = this.locked;
        state.processing = this.processing;
        state.processingFrom = this.processingFrom;
        try {
            File[] ff = inputDirectory.listFiles();
            if(ff != null) {
                Arrays.sort(ff, this.comparator);
                state.preparedFiles = new ArrayList<>();
                for(File file1 : ff) {
                    state.preparedFiles.add(file1.getName());
                }
            }
        } catch(Exception e) {
        }
        return state;
    }





    private synchronized File nextFile() {
        if(processing) return null;
        if(locked) return null;
        File[] children = inputDirectory.listFiles();
        if(children == null) return null;
        if(children.length == 0) return null;
        Arrays.sort(children, comparator);
        this.processing = true;
        this.processingFrom = LocalDateTime.now();
        this.currentFile = children[0];
        return this.currentFile;
    }


    private MessageBuilderFactory messageBuilderFactory = null;
    protected synchronized MessageBuilderFactory getMessageBuilderFactory() {
        if (this.messageBuilderFactory == null) { messageBuilderFactory = new DefaultMessageBuilderFactory(); }
        return this.messageBuilderFactory;
    }

    private void cleanFile() {
        this.currentFile = null;
        this.processingFrom = null;
        this.processing = false;
    }


    private static File createDirectory(String directory) {
        if(directory == null) throw new NullPointerException("unablde to create directory - null");
        File f = new File(directory);
        if(f.exists()) {
            if(!f.isDirectory()) throw new IllegalStateException("not a directory " + directory);
        } else {
            boolean created = f.mkdirs();
            if(!created) throw new IllegalStateException("unable to create directory " + directory);
        }
        return f;
    }

    public static class FileNameComparator implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {
            String n1 = f1 == null ? "" : f1.getName();
            if(n1 == null) n1 = "";
            String n2 = f2 == null ? "" : f2.getName();
            if(n2 == null) n2 = "";
            return n1.compareTo(n2);
        }

        public static FileNameComparator instance() { return new FileNameComparator(); }

    }

    public static class State {
        boolean locked;
        boolean processing;
        LocalDateTime processingFrom;
        String currentFile;
        List<String> preparedFiles;

        public boolean isLocked() { return locked; }
        public boolean isProcessing() { return processing; }
        public LocalDateTime getProcessingFrom() { return processingFrom; }
        public String getCurrentFile() { return currentFile; }
        public List<String> getPreparedFiles() { return preparedFiles; }


    }

}
