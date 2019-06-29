package org.kds.reactive;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxAndMonoFilterTest {

    private List<String> charList;

    @Before
    public void setUp() {
        charList = Arrays.asList("A", "B", "C", "D");
    }

    @Test
    public void testFluxFilter() {
        Flux<String> stringFlux = Flux.fromIterable(charList)
                .filter(s -> s.startsWith("A"))
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("A")
                .verifyComplete();

    }
}
