package org.kds.reactive;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static reactor.core.scheduler.Schedulers.parallel;

public class FluxAndMonoTransformTest {

    private List<String> charList;

    @Before
    public void setUp() {
        charList = Arrays.asList("A", "B", "C", "D");
    }

    @Test
    public void testTransformFlux() {

        Flux<String> stringFlux = Flux.fromIterable(charList)
                .map(String::toLowerCase)
                .log();

        StepVerifier.create(stringFlux)
                .expectNext("a", "b", "c", "d")
                .verifyComplete();

    }

    @Test
    public void testTransformFluxWithFlatMap() {
        Flux<String> stringFlux = Flux.fromIterable(charList)
                // note that here every event create another Flux
                // hence the result is events within events
                // this is where flatMap comes in handy and it flatten the event stream to
                // total 8 events.
                .flatMap(s -> Flux.fromIterable(convertToList(s)))
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(8)
                .verifyComplete();

    }

    private List<String> convertToList(String s) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Arrays.asList(s, s+s.toLowerCase());
    }

    @Test
    public void testTransformFluxWithFlatMapParallel() {
        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F"))
                // window method split the events in to chunks by given size and emit
                .window(2) // Flux<Flux<String>> -> (A,B), (C,D), (E, F)
                .flatMap((s) ->
                        s.map(this::convertToList).subscribeOn(parallel()))
                        .flatMap(Flux::fromIterable)
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();

    }

    @Test
    public void testTransformFluxWithConcatMap() {
        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F"))
                // window method split the events in to chunks by given size and emit
                .window(2) // Flux<Flux<String>> -> (A,B), (C,D), (E, F)
                // does the same job as flatMap but in a mannet that sequentially and preserving order using concatenation.
                // but this makes things bit slow
                .concatMap((s) ->
                        s.map(this::convertToList).subscribeOn(parallel()))
                .flatMap(Flux::fromIterable)
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();

    }

    @Test
    public void testTransformFluxWithFlatMapSequential() {
        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F"))
                // window method split the events in to chunks by given size and emit
                .window(2) // Flux<Flux<String>> -> (A,B), (C,D), (E, F)
                // this does the same thing as flatMap while preserving the order
                // and keep up the speed
                .flatMapSequential((s) ->
                        s.map(this::convertToList).subscribeOn(parallel()))
                .flatMap(Flux::fromIterable)
                .log();

        StepVerifier.create(stringFlux)
                .expectNextCount(12)
                .verifyComplete();
    }
}
