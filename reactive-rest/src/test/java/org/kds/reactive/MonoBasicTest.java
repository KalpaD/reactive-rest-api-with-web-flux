package org.kds.reactive;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class MonoBasicTest {

    @Test
    public void testMono() {

        Mono<String> mono = Mono.just("A")
                .log();

        StepVerifier.create(mono)
                .expectNext("A")
                .verifyComplete();
    }

    @Test
    public void testMonoWithError() {
        Mono<Object> errorMono = Mono.error(new RuntimeException("Error Occured"))
                .log();

        StepVerifier.create(errorMono)
                .expectError(RuntimeException.class)
                .verify();
    }
}
