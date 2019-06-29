package org.kds.reactive.router;

import org.kds.reactive.handler.HandlerFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

/**
 *  Router classes are responsible for mapping the incoming requests to
 *  the appropriate handler.
 */
@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> route(HandlerFunction handlerFunction) {

        return RouterFunctions
                // mapping
                .route(GET("/functional/flux").and(accept(MediaType.APPLICATION_JSON)),
                        // handler
                        handlerFunction::flux);
    }
}
