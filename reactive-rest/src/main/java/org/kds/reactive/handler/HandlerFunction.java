package org.kds.reactive.handler;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *  This is a sample handler function in the functional reactive eco-system
 *  Note that this class is annotated with @Component
 */
@Component
public class HandlerFunction {

    /**
     * Note that this method takes ServerRequest object as input param and
     * return a Mono<ServerResponse>
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> flux(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Flux.just(1, 2, 3, 4).log(), Integer.class);
    }
}
