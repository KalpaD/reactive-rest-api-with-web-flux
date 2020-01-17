package org.kds.reactive;

import org.junit.Test;
import org.kds.reactive.exception.CustomException;
import org.kds.reactive.threading.SchedulerTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxAndMonoErrorHandlingTest {

    private static Logger LOG = LoggerFactory.getLogger(FluxAndMonoErrorHandlingTest.class);

    @Test
    public void fluxErrorHandlerTest() {
        Flux<String> fluxWithError = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Exception occurred")))
                .concatWith(Flux.just("D"))
                // this is important
                // errors are also considered as events
                .onErrorResume((e) -> {
                    LOG.error("Error occurred {}", e);
                    return Flux.just("default");
                });


        StepVerifier.create(fluxWithError.log())
                .expectSubscription()
                .expectNext("A", "B", "C")
                //.expectError(RuntimeException.class)
                //.verify();
                // the default value coming from onErrorResume block
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    public void fluxErrorHandlerWithOnErrorReturn() {
        Flux<String> fluxWithError = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Exception occurred")))
                // on error this block comes in to picture and return a fallback value.
                .onErrorReturn("fallback");


        StepVerifier.create(fluxWithError.log())
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectNext("fallback")
                .verifyComplete();
    }

    @Test
    public void fluxExceptionTranslationWithOnErrorMap() {
        Flux<String> fluxWithError = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Exception occurred")))
                .concatWith(Flux.just("D"))
                .onErrorMap((e) -> new CustomException("Translated exception"));

        StepVerifier.create(fluxWithError.log())
                .expectSubscription()
                .expectNext("A", "B", "C")
                .expectError(CustomException.class)
                .verify();
    }
}
