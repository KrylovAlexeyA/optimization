package ru.sberbank;

import org.HdrHistogram.Histogram;
import org.asynchttpclient.AsyncHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.sberbank.optdemo1.AsyncHttpClientFactory;

import java.util.concurrent.ExecutionException;

@SpringBootApplication
@EnableScheduling
public class Optdemo1Application {
    private static Histogram histogram = new Histogram(3600000000000L, 3);


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        SpringApplication.run(Optdemo1Application.class, args);
        AsyncHttpClient client = AsyncHttpClientFactory.create(new AsyncHttpClientFactory.AsyncHttpClientConfig());
        //Вызываем сами себя 1000 раз , чтобы закэшировать 1000 элементов
        for (int i = 0; i < 1000; i++) {
            client.prepareGet("http://localhost:8080/demo1/quotes?days=" + i).execute().get();
            System.out.println(i);
        }
        //Меряем latency и выводим гистограмму
        for (int i = 0; i < 100; i++) {
            long startTime = System.nanoTime();
            client.prepareGet("http://localhost:8080/demo1/quotes?days=" + i).execute().get();
            long endTime = System.nanoTime();
            histogram.recordValue(endTime - startTime);
            System.out.println(i);
        }
        System.out.println("Recorded latencies [in usec] for Create+Close of a DatagramSocket:");
        histogram.outputPercentileDistribution(System.out, 1000000.0);

    }

    @Scheduled(cron = "* 1/60 * * * *") // every day at one AM
//    @Scheduled(fixedDelay = 1000) // once a second
    public void readApplovin() throws InterruptedException {
        Thread.sleep(1);
//		System.out.println("I'm alive!");
    }
}
