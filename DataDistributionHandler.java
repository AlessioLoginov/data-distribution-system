import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataDistributionHandler implements Handler {

    private final Client client;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public DataDistributionHandler(Client client) {
        this.client = client;
    }

    @Override
    public Duration timeout() {
        return Duration.ofSeconds(1); // Пример задержки
    }

    @Override
    public void performOperation() {
        while (true) { // Пример бесконечного цикла для чтения и отправки данных
            Event event = client.readData();
            for (Address recipient : event.recipients()) {
                executor.submit(() -> {
                    Result result;
                    do {
                        result = client.sendData(recipient, event.payload());
                        if (result == Result.REJECTED) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(timeout().toMillis());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    } while (result != Result.ACCEPTED);
                });
            }
        }
    }
}
