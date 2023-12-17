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

    <TRequest, TResponse> Mono<TResponse> invokeApi(String uri,
                                                    HttpMethod httpMethod,
                                                    TRequest request,
                                                    Class<TResponse> clazz,
                                                    Consumer<HttpHeaders> headers);

    <TResponse> Mono<TResponse> invokeApi(String uri,
                                          HttpMethod httpMethod,
                                          BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                                          Class<TResponse> clazz);

    <TResponse> Mono<TResponse> invokeApi(String uri,
                                          HttpMethod httpMethod,
                                          Class<TResponse> clazz,
                                          Consumer<HttpHeaders> headers);

    <TResponse> Mono<TResponse> invokeApi(String uri,
                                          HttpMethod httpMethod,
                                          Class<TResponse> clazz);

    <TResponse> Mono<TResponse> invokeApi(String uri,
                                          HttpMethod httpMethod,
                                          BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                                          Class<TResponse> clazz,
                                          Consumer<HttpHeaders> headers);

    <TRequest, TResponse> Mono<TResponse> invokeApi(String uri,
                                                    HttpMethod httpMethod,
                                                    TRequest request,
                                                    Class<TResponse> clazz);

    //------------------Async method--------------------------
    <TRequest, TResponse> CompletableFuture<TResponse> invokeApiAsync(String uri,
                                                                      HttpMethod httpMethod,
                                                                      TRequest request,
                                                                      Class<TResponse> clazz,
                                                                      Consumer<HttpHeaders> headers);

    <TResponse> CompletableFuture<TResponse> invokeApiAsync(String uri,
                                                            HttpMethod httpMethod,
                                                            Class<TResponse> clazz,
                                                            Consumer<HttpHeaders> headers);

    <TResponse> CompletableFuture<TResponse> invokeApiAsync(String uri,
                                                            HttpMethod httpMethod,
                                                            Class<TResponse> clazz);

    <TRequest, TResponse> CompletableFuture<TResponse> invokeApiAsync(String uri,
                                                                      HttpMethod httpMethod,
                                                                      TRequest request,
                                                                      Class<TResponse> clazz);
}
