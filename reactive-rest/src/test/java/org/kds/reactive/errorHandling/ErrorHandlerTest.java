package org.kds.reactive.errorHandling;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Errors are first class citizens in reactive world
 * Any error in a reactive sequence is a terminal event.
 * Even if an error-handling operator is used, it does not allow the original sequence to continue.
 */
public class ErrorHandlerTest {


    /**
     * When the emitted event contains B then this flux throws an error event
     * in that case flux returns static value C.
     */
    @Test
    public void testCatchAndReturnStaticValue() {
        Flux<String> flux = Flux.just("A", "B")
                .map(a -> {
                    if (a.equals("B")) {
                        throw new RuntimeException("ERROR");
                    }
                    return a;
                })
                .onErrorReturn("C")
                // just to see what is being emitted
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                // expect the fallback value
                .expectNext("A", "C")
                .verifyComplete();
    }


    @Test
    public void testCatchAndExecuteAlternativePath() {
        Flux<String> flux = Flux.just("A", "B")
                .map(a -> {
                    if (a.equals("B")) {
                        throw new RuntimeException("ERROR");
                    }
                    return a;
                })
                .onErrorResume(ErrorHandlerTest::fallbackMethod)
                // just to see what is being emitted
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                // expect the fallback value
                .expectNext("A", "RUNTIME_EX")
                .verifyComplete();
    }

    private static Mono<String> fallbackMethod(Throwable error) {
        if (error instanceof RuntimeException) {
            return Mono.just("RUNTIME_EX");
        } else {
            return Mono.just("NOT_RUNTIME_EX");
        }
    }


}
