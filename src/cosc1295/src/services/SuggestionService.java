package cosc1295.src.services;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SuggestionService {
    private static final Logger logger = Logger.getLogger(SuggestionService.class.getName());

    private final ExecutorService executor;

    public SuggestionService() {
        executor = Executors.newSingleThreadExecutor();
    }

    public <T> T runForResult(Callable<T> analyzer) {
        try {
            return executor.submit(analyzer).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.WARNING, "SuggestionService.runForResult : " + e.getMessage());
            return null;
        }
    }

    public void die() {
        executor.shutdown();
    }
}
