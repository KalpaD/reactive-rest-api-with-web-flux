package org.kds.reactive.errorHandling;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Errors are first class citizens in reactive world
 * Any error in a reactive sequence is a terminal event.
 * Even if an error-handling operator is used, it does not allow the original sequence to continue.
 */
@Slf4j
public class ErrorHandlerTest {

     private Logger LOG = LoggerFactory.getLogger(ErrorHandlerTest.class);

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

    /**
     * This method translate the runtime exception to custom exception
     *
     * org.kds.reactive.errorHandling.CustomException: Error detected
     * at org.kds.reactive.errorHandling.ErrorHandlerTest.lambda$testCatchAndRethrow$3(ErrorHandlerTest.java:77)
     * .
     * .
     * .
     * at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
     * Caused by: java.lang.RuntimeException: ERROR
     *  at org.kds.reactive.errorHandling.ErrorHandlerTest.lambda$testCatchAndRethrow$2(ErrorHandlerTest.java:72)
     *  at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:107)
     * 	... 37 common frames omitted
     */
    @Test
    public void testCatchAndRethrow() {
        Flux<String> flux = Flux.just("A", "B")
                .map(a -> {
                    if (a.equals("B")) {
                        throw new RuntimeException("ERROR");
                    }
                    return a;
                })
                // mapping the original to custom one
                .onErrorMap(original -> new CustomException("Error detected", original))
                // just to see what is being emitted
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("A")
                // expect the Custom exception
                .expectError(CustomException.class)
                .verify();
    }

    /**
     * This demonstrates the scenario where we do a something on side about the error
     * bu we do not modify the error.
     *
     * [main] INFO reactor.Flux.PeekFuseable.1 - | onNext(A)
     *      [main] ERROR org.kds.reactive.errorHandling.ErrorHandlerTest - Something went wrong
     * [main] ERROR reactor.Flux.PeekFuseable.1 - | onError(java.lang.RuntimeException: ERROR)
     * [main] ERROR reactor.Flux.PeekFuseable.1 -
     *      java.lang.RuntimeException: ERROR
     * 	at org.kds.reactive.errorHandling.ErrorHandlerTest.lambda$testCatchAndReactOnSide$4(ErrorHandlerTest.java:118)
     *
     */
    @Test
    public void testCatchAndReactOnSide() {
        Flux<String> flux = Flux.just("A", "B")
                .map(a -> {
                    if (a.equals("B")) {
                        throw new RuntimeException("ERROR");
                    }
                    return a;
                })
                .doOnError(error -> {
                    LOG.error("Something went wrong");
                })
                // just to see what is being emitted
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("A")
                // the error still propagates
                .expectError(RuntimeException.class)
                .verify();
    }
}
