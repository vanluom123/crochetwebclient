package org.xiaofeng.webclient.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import org.xiaofeng.webclient.type.HttpMethod;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface WebClientService {
    WebClient.Builder builder();

    Mono<String> invokeApi(String uri,
                           HttpMethod httpMethod,
                           String request,
                           Consumer<HttpHeaders> headers);

    Mono<String> invokeApi(String uri,
                           HttpMethod httpMethod,
                           BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                           Consumer<HttpHeaders> headers);

    Mono<String> invokeApi(String uri,
                           HttpMethod httpMethod,
                           String request);

    Mono<String> invokeApi(String uri,
                           HttpMethod httpMethod,
                           BodyInserter<?, ? super ClientHttpRequest> bodyInserter);

    Mono<String> invokeApi(String uri,
                           HttpMethod httpMethod,
                           Consumer<HttpHeaders> headers);

    Mono<String> invokeApi(String uri,
                           HttpMethod httpMethod);

    //------------------Async method--------------------------
    CompletableFuture<String> invokeApiAsync(String uri,
                                             HttpMethod httpMethod,
                                             String request,
                                             Consumer<HttpHeaders> headers);

    CompletableFuture<String> invokeApiAsync(String uri,
                                             HttpMethod httpMethod,
                                             BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                                             Consumer<HttpHeaders> headers);

    CompletableFuture<String> invokeApiAsync(String uri,
                                             HttpMethod httpMethod,
                                             String request);

    CompletableFuture<String> invokeApiAsync(String uri,
                                             HttpMethod httpMethod,
                                             BodyInserter<?, ? super ClientHttpRequest> bodyInserter);

    CompletableFuture<String> invokeApiAsync(String uri,
                                             HttpMethod httpMethod,
                                             Consumer<HttpHeaders> headers);

    CompletableFuture<String> invokeApiAsync(String uri,
                                             HttpMethod httpMethod);
}
