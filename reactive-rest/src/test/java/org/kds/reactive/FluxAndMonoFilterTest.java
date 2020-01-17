package org.kds.reactive;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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

    /**
     * This test method demonestrate the behaviour of the filter operator where
     * no event match to it's conditions. In that case Mono<Void> returns which will catch by the
     * switchIfEmpty() block and call secondMethod to provide the alternative.
     */
    @Test
    public void testFluxFilterWhenNonMatch() {
        Flux<String> stringFlux = Flux.fromIterable(charList)
                .filter(s -> s.startsWith("Z"))
                .switchIfEmpty(Mono.defer(() -> secondMethod()))
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("Z")
                .verifyComplete();
    }

    private static Mono<String> secondMethod() {
        return Mono.just("Z");
    }
}
