package org.kds.reactive.controller;

import org.kds.reactive.mapper.FormattedNameResponseMapper;
import org.kds.reactive.model.FormatNameRequest;
import org.kds.reactive.model.FormattedNameResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Duration;

@RestController
public class FluxAndMonoController {

    @PostMapping("/format")
    public Mono<FormattedNameResponse> format(@Valid @RequestBody Mono<FormatNameRequest> request) {
        return request
                .map(FormattedNameResponseMapper::fromFormatNameRequest)
                .onErrorResume(WebExchangeBindException.class, ex -> Mono.just(FormattedNameResponseMapper.fromWebExchangeBindException(ex)));
    }

    @GetMapping("/flux")
    public Flux<Integer> flux() {
        return Flux.just(1, 2, 3, 4)
                .delayElements(Duration.ofSeconds(1))
                .log();
    }

    @GetMapping(value = "/fluxstream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Long> fluxStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .log();
    }

    @GetMapping(value = "/flux/finite/stream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Integer> fluxFiniteStream() {
        return Flux.just(1, 2, 3, 4)
                .delayElements(Duration.ofSeconds(1))
                .log();
    }

    @GetMapping(value = "/flux/exception")
    public Flux<Integer> exceptionHandling() {
        return Flux.just(1, 2, 3, 4)
                // add error with exception error.
                .concatWith(Mono.error(new RuntimeException("Runtime Error Occurred")))
                .log();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
