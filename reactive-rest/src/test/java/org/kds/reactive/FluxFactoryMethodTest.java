package org.kds.reactive;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxFactoryMethodTest {

    private List<String> charList;
    private String [] charStringArray;

    @Before
    public void setUp() {
        charList = Arrays.asList("A", "B", "C", "D");
        charStringArray = new String [] {"A", "B", "C", "D"};
    }

    @Test
    public void testFluxUsingIterable() {
        Flux<String> namesFlux = Flux.fromIterable(charList)
                .log();

        StepVerifier.create(namesFlux)
                .expectNext("A", "B", "C", "D")
                .verifyComplete();
    }

    @Test
    public void testFluxUsingArray() {
        Flux<String> namesFlux = Flux.fromArray(charStringArray)
                .log();

        StepVerifier.create(namesFlux)
                .expectNext("A", "B", "C", "D")
                .verifyComplete();
    }

    @Test
    public void testFluxUsingStream() {
        Flux<String> namesFlux = Flux.fromStream(charList.stream())
                .log();

        StepVerifier.create(namesFlux)
                .expectNext("A", "B", "C", "D")
                .verifyComplete();
    }

    @Test
    public void testFluxUsingRange() {
        Flux<Integer> integerFlux = Flux.range(1, 10)
                .log();

        StepVerifier.create(integerFlux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }
}
