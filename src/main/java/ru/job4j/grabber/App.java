package ru.job4j.grabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.service.DBService;
import ru.job4j.grabber.service.IDBService;

import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class for start Application.
 *
 * @author Alexander Bondarev(mailto:bondarew2507@gmail.com).
 * @since 22.02.2018.
 */
public class App implements Runnable {

    private static final long MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private final IDBService service;

    private VacanciesParser parser;

    private int frequency;

    public App(IDBService service) {
        this(1, service);
    }

    public App(int frequency, IDBService service) {
        this.frequency = frequency;
        this.service = service;
        this.parser = new VacanciesParser();
    }

    public void start() {
        service.add(parser.parse());
    }

    @Override
    public void run() {
        while (true) {
            this.start();
            if (parser.getLastParsingDate().before(new Timestamp(System.currentTimeMillis()))) {
                try {
                    Thread.sleep(MILLISECONDS_IN_DAY / frequency);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                    break;
                }
            }
        }
        this.service.close();
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new App(new DBService()));
    }
}
