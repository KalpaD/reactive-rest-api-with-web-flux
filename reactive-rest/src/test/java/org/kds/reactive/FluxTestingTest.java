package org.kds.reactive;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxTestingTest {

    @Test
    public void testFluxSuccessWithStepVerifier() {
        Flux<String> basicFlux = Flux.just("A", "B", "C")
                .log();

        // note the use of StepVerifier provided by the reactor test package
        StepVerifier.create(basicFlux)
                .expectNext("A") //
                .expectNext("B")
                .expectNext("C")
                .verifyComplete(); // it takes care of subscribing to the flux
    }

    @Test
    public void testFluxErrorWithStepVerifier() {
        Flux<String> basicFlux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Error Occurred")))
                .log();

        StepVerifier.create(basicFlux)
                .expectNext("A")
                .expectNext("B")
                .expectNext("C")
                .expectError(RuntimeException.class)// verify the exception.
                .verify(); //
    }

    @Test
    public void testFluxEventNumberWithStepVerifier() {
        Flux<String> basicFlux = Flux.just("A", "B", "C")
                .log();

        StepVerifier.create(basicFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

}
