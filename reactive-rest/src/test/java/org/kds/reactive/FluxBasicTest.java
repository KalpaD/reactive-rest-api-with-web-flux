package org.kds.reactive;

import org.junit.Test;
import reactor.core.publisher.Flux;

public class FluxBasicTest {

    @Test
    public void testFlux() {
        Flux<String> basicFlux = Flux.just("A", "B", "C");

        // Subscribing to the Flux is the only way to get the emitted events from a flux.
        basicFlux.subscribe(System.out::println);
    }

    @Test
    public void testFluxWithError() {
        Flux<String> basicFlux = Flux.just("A", "B", "C")
                // adding error event to the event stream
                .concatWith(Flux.error(new RuntimeException("Error Occurred")));

        // Note the use of subscriber which is capable of consuming error
        basicFlux.subscribe(System.out::println, System.err::println);
    }

    @Test
    public void testFluxWithErrorWithLog() {
        Flux<String> basicFlux = Flux.just("A", "B", "C")
                // adding error event to the event stream
                .concatWith(Flux.error(new RuntimeException("Error Occurred")))
                // adding a logger
                .log();

        // Note the use of subscriber which is capable of consuming error
        basicFlux.subscribe(System.out::println, System.err::println);

         // The event stream is as follows as this
         /*
         onSubscribe(FluxConcatArray.ConcatArraySubscriber)
         request(unbounded)  // note the request for events with unbounded
         onNext(A)
         A
         onNext(B)
         B
         onNext(C)
         C
         onError(java.lang.RuntimeException: Error Occurred)
         */
    }

    @Test
    public void testFluxWithErrorAndEventAfterThat() {
        Flux<String> basicFlux = Flux.just("A", "B", "C")
                // adding error event to the event stream
                .concatWith(Flux.error(new RuntimeException("Error Occurred")))
                // Adding D event after error happen
                // but this will not be there in the results since Flux event stream will be eneded
                // when error happens
                .concatWith(Flux.just("D"))
                // adding a logger
                .log();

        basicFlux.subscribe(System.out::println, System.err::println);
    }

    @Test
    public void testFluxWithOnCompleteConsumer() {
        Flux<String> basicFlux = Flux.just("A", "B", "C")
                .concatWith(Flux.just("D"));

        basicFlux.subscribe(System.out::println, // consumer
                 System.err::println, // error consumer
                 () -> System.out.println("CompleteEventOccurred")); // complete consumer
    }

}
