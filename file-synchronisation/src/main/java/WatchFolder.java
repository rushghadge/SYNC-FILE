
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class WatchFolder {
    public Path newFile=null;
    public Path modifiedFile=null;
    public Path deletedFile=null;
    public final WatchKey watchKey;
    public final WatchService watchService;
    public final Path directory;
    private static Logger logger;

    public WatchFolder(String dir, Logger logger) throws IOException {
        WatchFolder.logger = logger;
        // STEP1: Get the path of the directory which you want to monitor.
        this.directory = Path.of(dir);
        // STEP2: Create a watch service
        this.watchService = FileSystems.getDefault().newWatchService();
        // STEP3: Register the directory with the watch service
        this.watchKey = directory.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
    }
    public void watchFolder() {

        try {
            Map<Path, String> fileEvent = new HashMap<>();
            for (WatchEvent<?> event : this.watchKey.pollEvents()) {
                Path fileName = (Path) event.context();
                // STEP6: Check type of event.
                WatchEvent.Kind<?> kind = event.kind();

                // STEP7: Perform necessary action with the event
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    this.newFile = fileName;
                    fileEvent.put(fileName, "CREATE");
                }
                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    this.deletedFile = fileName;
                }
                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    if (!fileEvent.containsKey(fileName) || !fileEvent.get(fileName).equals("CREATE")) {
                        this.modifiedFile = fileName;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unknown error", e);
        }

    }
}