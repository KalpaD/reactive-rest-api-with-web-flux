package org.kds.reactive.webclient;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

public class WebClientTimeoutTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientTimeoutTest.class);

    private static String BASE_URL = "http://localhost:1080";

    private ClientAndServer mockServer;
    private WebClient webClient;

    @Before
    public void startMockServer() {
        mockServer = startClientAndServer(1080);

        // set up mock with a delay of 5 seconds
        mockServer.when(HttpRequest.request().withMethod("GET")
        .withPath("/accounts")).
                respond(HttpResponse.response()
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody("{ \"result\": \"ok\"}")
                        .withDelay(TimeUnit.MILLISECONDS, 5000));

        webClient = createWebClientWithConnectAndReadTimeOuts(2000, 3000);
    }

    private WebClient createWebClientWithConnectAndReadTimeOuts(int connectTimeOut, long readTimeOut) {
        // create reactor netty HTTP client
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(tcpClient -> {
                    tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeOut);
                    tcpClient = tcpClient.doOnConnected(conn -> conn
                            .addHandlerLast(new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS)));
                    return tcpClient;
                });
        // create a client http connector using above http client
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        // use this configured http connector to build the web client
        return WebClient.builder().clientConnector(connector).build();
    }

    private Mono<JsonNode> doGetWithCustomConnectAndReadTimeOut(URI uri) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    LOGGER.error("Error while calling endpoint {} with status code {}",
                            uri.toString(), clientResponse.statusCode());
                    throw new RuntimeException("Error while calling  accounts endpoint");
                }).bodyToMono(JsonNode.class);
    }

    private Mono<JsonNode> doGetWithDefaultConnectAndReadTimeOut(URI uri, long timeout) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    LOGGER.error("Error while calling endpoint {} with status code {}",
                    uri.toString(), clientResponse.statusCode());
                    throw new RuntimeException("Error while calling  accounts endpoint");
        }).bodyToMono(JsonNode.class)
                // setting the signal timeout
                .timeout(Duration.ofMillis(timeout));
    }

    @Test
    public void testWebClientWithCustomTimeOut() {
        URI uri = UriComponentsBuilder.fromUriString(BASE_URL + "/accounts").build().toUri();
        Mono<JsonNode> result = doGetWithCustomConnectAndReadTimeOut(uri);
        StepVerifier.create(result)
                .expectSubscription()
                .expectError(ReadTimeoutException.class)
                .verify();
    }

    @Test
    public void testWebClientWithDefaultTimeOut() {
        URI uri = UriComponentsBuilder.fromUriString(BASE_URL + "/accounts").build().toUri();
        // do the service call out with 3 seconds of signal timeout
        Mono<JsonNode> result = doGetWithDefaultConnectAndReadTimeOut(uri, 3000);
        StepVerifier.create(result)
                .expectSubscription()
                .expectError(TimeoutException.class)
                //.assertNext(jsonNode -> assertEquals("ok", jsonNode.get("result").textValue()))
                .verify();
    }

    @After
    public void stopMockServer() {
        mockServer.stop();
    }
}
