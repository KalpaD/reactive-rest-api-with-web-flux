package org.kds.reactive;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxAndMonoFilterTest {
    private static Logger logger = LoggerFactory.getLogger(FluxAndMonoFilterTest.class);

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
     * This test method demonstrate the behaviour of the filter operator where
     * no event match to it's conditions. In that case Mono<Void> returns which will catch by the
     * switchIfEmpty() block and call secondMethod to provide the alternative.
     */
    @Test
    public void testFluxFilterWhenNonMatch() {
        Flux<String> stringFlux = Flux.fromIterable(charList)
                .filter(s -> s.startsWith("Z"))
                .switchIfEmpty(secondMethod()) // switchIfEmpty is eager
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("Z")
                .verifyComplete();
    }

    private static Mono<String> secondMethod() {
        logger.info("Got a call to secondMethod");
        return Mono.just("Z");
    }
}
