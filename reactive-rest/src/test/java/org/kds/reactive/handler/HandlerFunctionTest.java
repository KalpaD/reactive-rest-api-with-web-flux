package org.kds.reactive.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
//@WebFluxTest annotation only scans the classes with @RestController and @Controller hence it
// can not support the test cases in this class, hence we will have to use @SpringBootTest annotation.
// because HandlerFunction use @Component and RouterFunctionConfig use @Configuration
@SpringBootTest
// but just using @SpringBootTest will not be able to autowire the WebTestClient, hence
@AutoConfigureWebTestClient
public class HandlerFunctionTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testFluxEndpoint() {
        Flux<Integer> responseFlux = webTestClient.get().uri("/functional/flux")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(responseFlux)
                .expectSubscription()
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .expectNext(4)
                .verifyComplete();

    }
}
