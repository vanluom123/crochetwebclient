package org.xiaofeng.webclient.service;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import org.xiaofeng.webclient.common.ClientErrorException;
import org.xiaofeng.webclient.common.ServerErrorException;
import org.xiaofeng.webclient.type.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WebClientServiceImpl implements WebClientService {
    private final Logger logger = LoggerFactory.getLogger(WebClientServiceImpl.class);

    private final WebClient.Builder clientBuilder;

    @Inject
    public WebClientServiceImpl(WebClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Override
    public WebClient.Builder builder() {
        return clientBuilder;
    }

    private WebClient client() {
        return clientBuilder.build();
    }

    private <T> CompletableFuture<T> toCompletableFuture(Mono<T> mono) {
        CompletableFuture<T> future = new CompletableFuture<>();
        mono.subscribe(
                future::complete,
                future::completeExceptionally
        );
        return future;
    }

    private <T> Mono<T> executeRequest(WebClient.RequestHeadersSpec<?> spec, Class<T> clazz) {
        return spec
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody ->
                                        Mono.error(new ClientErrorException(errorBody)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody ->
                                        Mono.error(new ServerErrorException(errorBody)))
                )
                .bodyToMono(clazz)
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> logger.error("Error occurred: {}", error.getMessage()));
    }

    private <TRequest, TResponse> Mono<TResponse> executeWithBody(String uri,
                                                                  HttpMethod httpMethod,
                                                                  TRequest request,
                                                                  Class<TResponse> clazz,
                                                                  Consumer<HttpHeaders> headers) {
        WebClient.RequestBodySpec bodySpec = switch (httpMethod) {
            case POST -> client().post().uri(uri);
            case PUT -> client().put().uri(uri);
            default -> throw new IllegalArgumentException("Unsupported request type: " + httpMethod);
        };

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec
                .headers(headers)
                .body(Mono.just(request), request.getClass());

        return executeRequest(headersSpec, clazz);
    }

    private <TResponse> Mono<TResponse> executeWithBody(String uri,
                                                        HttpMethod httpMethod,
                                                        BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                                                        Class<TResponse> clazz,
                                                        Consumer<HttpHeaders> headers) {
        WebClient.RequestBodySpec bodySpec = switch (httpMethod) {
            case POST -> client().post().uri(uri);
            case PUT -> client().put().uri(uri);
            default -> throw new IllegalArgumentException("Unsupported request type: " + httpMethod);
        };

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec
                .headers(headers)
                .body(bodyInserter);

        return executeRequest(headersSpec, clazz);
    }

    private <TRequest, TResponse> Mono<TResponse> executeWithBody(String uri,
                                                                  HttpMethod httpMethod,
                                                                  TRequest request,
                                                                  Class<TResponse> clazz) {
        WebClient.RequestBodySpec bodySpec = switch (httpMethod) {
            case POST -> client().post().uri(uri);
            case PUT -> client().put().uri(uri);
            default -> throw new IllegalArgumentException("Unsupported request type: " + httpMethod);
        };

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec
                .body(Mono.just(request), request.getClass());

        return executeRequest(headersSpec, clazz);
    }

    private <TResponse> Mono<TResponse> executeWithBody(String uri,
                                                        HttpMethod httpMethod,
                                                        BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                                                        Class<TResponse> clazz) {
        WebClient.RequestBodySpec bodySpec = switch (httpMethod) {
            case POST -> client().post().uri(uri);
            case PUT -> client().put().uri(uri);
            default -> throw new IllegalArgumentException("Unsupported request type: " + httpMethod);
        };

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec
                .body(bodyInserter);

        return executeRequest(headersSpec, clazz);
    }

    private WebClient.RequestHeadersSpec<?> getHeadersSpec(String uri, HttpMethod httpMethod) {
        WebClient.RequestHeadersSpec<?> headersSpec = switch (httpMethod) {
            case POST -> client().post().uri(uri);
            case PUT -> client().put().uri(uri);
            case GET -> client().get().uri(uri);
            case DELETE -> client().delete().uri(uri);
            default -> throw new IllegalArgumentException("Unsupported request type: " + httpMethod);
        };

        return headersSpec;
    }

    private <TResponse> Mono<TResponse> executeWithoutBody(String uri,
                                                           HttpMethod httpMethod,
                                                           Class<TResponse> clazz,
                                                           Consumer<HttpHeaders> headers) {
        WebClient.RequestHeadersSpec<?> headersSpec = getHeadersSpec(uri, httpMethod);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = headersSpec.headers(headers);
        return executeRequest(requestHeadersSpec, clazz);
    }

    private <TResponse> Mono<TResponse> executeWithoutBody(String uri,
                                                           HttpMethod httpMethod,
                                                           Class<TResponse> clazz) {
        WebClient.RequestHeadersSpec<?> headersSpec = getHeadersSpec(uri, httpMethod);
        return executeRequest(headersSpec, clazz);
    }

    @Override
    public <TRequest, TResponse> Mono<TResponse> invokeApi(String uri,
                                                           HttpMethod httpMethod,
                                                           TRequest request,
                                                           Class<TResponse> clazz,
                                                           Consumer<HttpHeaders> headers) {
        return executeWithBody(uri, httpMethod, request, clazz, headers);
    }

    @Override
    public <TResponse> Mono<TResponse> invokeApi(String uri,
                                                 HttpMethod httpMethod,
                                                 BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                                                 Class<TResponse> clazz,
                                                 Consumer<HttpHeaders> headers) {
        return executeWithBody(uri, httpMethod, bodyInserter, clazz, headers);
    }

    @Override
    public <TRequest, TResponse> Mono<TResponse> invokeApi(String uri,
                                                           HttpMethod httpMethod,
                                                           TRequest request,
                                                           Class<TResponse> clazz) {
        return executeWithBody(uri, httpMethod, request, clazz);
    }

    @Override
    public <TResponse> Mono<TResponse> invokeApi(String uri,
                                                 HttpMethod httpMethod,
                                                 BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                                                 Class<TResponse> clazz) {
        return executeWithBody(uri, httpMethod, bodyInserter, clazz);
    }

    @Override
    public <TResponse> Mono<TResponse> invokeApi(String uri,
                                                 HttpMethod httpMethod,
                                                 Class<TResponse> clazz,
                                                 Consumer<HttpHeaders> headers) {
        return executeWithoutBody(uri, httpMethod, clazz, headers);
    }

    @Override
    public <TResponse> Mono<TResponse> invokeApi(String uri,
                                                 HttpMethod httpMethod,
                                                 Class<TResponse> clazz) {
        return executeWithoutBody(uri, httpMethod, clazz);
    }

    //------------------Async method--------------------------
    @Override
    public <TRequest, TResponse> CompletableFuture<TResponse> invokeApiAsync(String uri,
                                                                             HttpMethod httpMethod,
                                                                             TRequest request,
                                                                             Class<TResponse> clazz,
                                                                             Consumer<HttpHeaders> headers) {
        return toCompletableFuture(executeWithBody(uri, httpMethod, request, clazz, headers));
    }

    @Override
    public <TRequest, TResponse> CompletableFuture<TResponse> invokeApiAsync(String uri,
                                                                             HttpMethod httpMethod,
                                                                             TRequest request,
                                                                             Class<TResponse> clazz) {
        return toCompletableFuture(executeWithBody(uri, httpMethod, request, clazz));
    }

    @Override
    public <TResponse> CompletableFuture<TResponse> invokeApiAsync(String uri,
                                                                   HttpMethod httpMethod,
                                                                   Class<TResponse> clazz,
                                                                   Consumer<HttpHeaders> headers) {
        return toCompletableFuture(executeWithoutBody(uri, httpMethod, clazz, headers));
    }

    @Override
    public <TResponse> CompletableFuture<TResponse> invokeApiAsync(String uri,
                                                                   HttpMethod httpMethod,
                                                                   Class<TResponse> clazz) {
        return toCompletableFuture(executeWithoutBody(uri, httpMethod, clazz));
    }
}
