package org.kds.reactive.errorHandling;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Errors are first class citizens in reactive world
 * Any error in a reactive sequence is a terminal event.
 * Even if an error-handling operator is used, it does not allow the original sequence to continue.
 */
public class ErrorHandlerTest {

     private Logger LOG = LoggerFactory.getLogger(ErrorHandlerTest.class);

    /**
     * When the emitted event contains B then this flux throws an error event
     * in that case flux returns static value C.
     */

    @Test
    public void testError() {
        Flux<String> flux = Flux.just("A", "B")
                .map(a -> {
                    if (a.equals("B")) {
                        throw new RuntimeException("ERROR");
                    }
                    return a;
                })
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("A")
                .expectError()
                .verify();
    }

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


    @Test
    public void testCatchAndHandleHierarchicalLevel1Errors() {
        String input = "A";

        Mono<String> mono = ErrorHandlerTest.doDbOperation(input)
                .flatMap(result -> Mono.just(result.toLowerCase()))
                .zipWhen(ErrorHandlerTest::doAnotherOperation, (res1, res2) -> res1)
                .onErrorMap(DbException.class, (err) -> new CustomException("DB error translated to application error", err))
                .onErrorMap(OtherOperationException.class, (err) -> new CustomException("Other operation error translated to application error", err));

        StepVerifier.create(mono)
                .expectSubscription()
                // expect the Custom exception
                .expectErrorMessage("DB error translated to application error")
                .verify();
    }


    @Test
    public void testCatchAndHandleHierarchicalLevel2Errors() {
        String input = "B";

        Mono<String> mono = ErrorHandlerTest.doDbOperation(input)
                .flatMap(result -> Mono.just(result.toLowerCase()))
                .zipWhen(ErrorHandlerTest::doAnotherOperation, (res1, res2) -> res1)
                .onErrorMap(DbException.class, (err) -> new CustomException("DB error translated to application error", err))
                .onErrorMap(OtherOperationException.class, (err) -> new CustomException("Other operation error translated to application error", err));

        StepVerifier.create(mono)
                .expectSubscription()
                // expect the Custom exception
                .expectErrorMessage("Other operation error translated to application error")
                .verify();
    }

    private static Mono<String> doDbOperation(String input) {
        return Mono.just(input)
                .map(a -> {
                    if (input.equals("A")) {
                        throw new DbException("DB operation failed");
                    }
                    return a;
                });
    }

    private static Mono<String> doAnotherOperation(String input) {
        return Mono.just(input).
                map(a -> {
                    if (input.equals("b")) {
                        throw new OtherOperationException("Other operation failed");
                    }
                    return a;
                });
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

    /**
     * When SignalType Cancel emitted the doFinally() block get executed
     * [parallel-1] INFO org.kds.reactive.errorHandling.ErrorHandlerTest - event number 1 emitted
     * [parallel-2] INFO org.kds.reactive.errorHandling.ErrorHandlerTest - event number 2 emitted
     * [parallel-3] INFO org.kds.reactive.errorHandling.ErrorHandlerTest - event number 3 emitted
     * [parallel-3] INFO reactor.Flux.Array.1 - | cancel()
     * [parallel-3] INFO org.kds.reactive.errorHandling.ErrorHandlerTest - Signal Type :CANCEL
     * [parallel-3] INFO org.kds.reactive.errorHandling.ErrorHandlerTest - Final number of events emitted 3
     */
    @Test
    public void testUsingFinallyBlock() {
        AtomicInteger atomicInteger = new AtomicInteger(0);

        Flux<String> flux = Flux.just("A", "B", "C", "D")
                .log()
                .delayElements(Duration.ofMillis(1000))
                .doOnNext(element -> {
                    int i = atomicInteger.incrementAndGet();
                    LOG.info("event number {} emitted ", i);
                })
                .doFinally(type -> {
                    // doFinally consumes a SignalType for the type of termination
                    LOG.info("Signal Type :" + type.name());
                    if (type == SignalType.CANCEL) {
                        LOG.info("Final number of events emitted {}", atomicInteger.get());
                    }
                    // we take only three events, then a cancel signal should be emitted.
                }).take(3);

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("A", "B", "C")
                .thenCancel()
                .verify();
    }

    @Test
    public void testHandlingCheckedExceptions() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .log()
                .map(element -> {
                    try {
                        return generateCheckedException(element);
                    } catch (IOException e) {
                        // convert the checked exception to runtime (unchecked exception)
                       throw Exceptions.propagate(e);
                    }
                });

        flux.subscribe(
                event -> LOG.info("event received {}", event),
                error -> {
                    if (Exceptions.unwrap(error) instanceof IOException) {
                        LOG.error("Something went wrong during I/O operation");
                    } else {
                        LOG.error("Something went wrong");
                    }
                });
    }

    private static String generateCheckedException(String input) throws IOException {
        if (input.equals("C")) {
            throw new IOException("Failed IO");
        }
        return input;
    }
}
