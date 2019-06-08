package org.kds.reactive.webclient;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;

public class WebClientDemo {

    private WebClient webClient;
    private static CountDownLatch countDownLatch;

    public WebClientDemo() {
        webClient = WebClient.create("http://localhost:8080");
        countDownLatch = new CountDownLatch(8);
    }

    private Flux<Integer> callFluxIntEndpointUsingRetrieve() {
        return webClient.get().uri("/flux/finite/stream")
                .retrieve()
                .bodyToFlux(Integer.class)
                .log();
    }

    private Flux<Integer> callFluxIntEndpointUsingExchange() {
        return webClient.get().uri("/flux/finite/stream")
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Integer.class))
                .log();
    }

    public static void main(String [] args) throws InterruptedException {

        WebClientDemo demo = new WebClientDemo();

        Flux<Integer> integerFlux = demo.callFluxIntEndpointUsingRetrieve();
        integerFlux.subscribe( item -> {
            System.out.println("*********** "+ item);
            countDownLatch.countDown();
        });

        Flux<Integer> integerFluxEx = demo.callFluxIntEndpointUsingExchange();
        integerFluxEx.subscribe( item -> {
            System.out.println("*********** "+ item);
            countDownLatch.countDown();
        });

        countDownLatch.await();
    }
}
