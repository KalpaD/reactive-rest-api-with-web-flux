package org.kds.reactive;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Supplier;

public class MonoFactoryMethodTest {

    @Test
    public void testMonoUsingJustOrEmpty() {

        Mono<Object> mono = Mono.justOrEmpty(null)
                .log();

        StepVerifier.create(mono)
                .verifyComplete();
    }

    @Test
    public void testMonoUsingSupplier() {

        Supplier<String> supplier = () -> "A";

        Mono<String> mono = Mono.fromSupplier(supplier)
                .log();

        StepVerifier.create(mono)
                .expectNext("A")
                .verifyComplete();
    }
}
