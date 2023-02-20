package utilities;
import org.apache.log4j.*;


public class Log {

    public static Logger getLogger(String className) {
        Logger logger = Logger.getLogger(className+".class");

        ConsoleAppender console = new ConsoleAppender();
        final String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.ERROR);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);
        Logger.getRootLogger().addAppender(new MyAppender());
        Logger.getRootLogger().setAdditivity(false);

        return logger;
    }

}
