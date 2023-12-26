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
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * WebClientServiceImpl class
 */
public class WebClientServiceImpl implements WebClientService {
    private final Logger logger = LoggerFactory.getLogger(WebClientServiceImpl.class);

    private final WebClient.Builder clientBuilder;

    /**
     * Constructor
     *
     * @param clientBuilder WebClient.Builder
     */
    @Inject
    public WebClientServiceImpl(WebClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    /**
     * Builder
     *
     * @return WebClient.Builder
     */
    @Override
    public WebClient.Builder builder() {
        return clientBuilder;
    }

    /**
     * Get client
     *
     * @return WebClient
     */
    private WebClient client() {
        return clientBuilder.build();
    }

    /**
     * Converts a Reactor Mono to a Java CompletableFuture.
     *
     * @param mono The Reactor Mono to be converted.
     * @return A CompletableFuture representing the result of the Mono.
     */
    private CompletableFuture<String> toCompletableFuture(Mono<String> mono) {
        CompletableFuture<String> future = new CompletableFuture<>();
        mono.subscribe(future::complete, future::completeExceptionally);
        return future;
    }

    /**
     * Executes a request using a WebClient and handles 4xx and 5xx errors.
     *
     * @param spec The WebClient.RequestHeadersSpec representing the request to be executed.
     * @return A Mono representing the response body.
     */
    private Mono<String> executeRequest(WebClient.RequestHeadersSpec<?> spec) {
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
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .doOnError(error -> logger.error("Error occurred: {}", error.getMessage()));
    }

    /**
     * Executes a request with a body using a WebClient.
     *
     * @param uri        The URI of the request.
     * @param httpMethod The HTTP method of the request (e.g., POST, PUT).
     * @param request    The request body.
     * @param headers    A consumer to set additional HTTP headers.
     * @return A Mono representing the response body.
     * @throws IllegalArgumentException If the provided HTTP method is not supported.
     */
    private Mono<String> executeWithBody(String uri,
                                         HttpMethod httpMethod,
                                         String request,
                                         Consumer<HttpHeaders> headers) {
        WebClient.RequestBodySpec bodySpec = switch (httpMethod) {
            case POST -> client().post().uri(uri);
            case PUT -> client().put().uri(uri);
            default -> throw new IllegalArgumentException("Unsupported request type: " + httpMethod);
        };

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec
                .headers(headers)
                .body(Mono.just(request), request.getClass());

        return executeRequest(headersSpec);
    }

    /**
     * Executes a request with a body using a WebClient.
     *
     * @param uri           The URI of the request.
     * @param httpMethod    The HTTP method of the request (e.g., POST, PUT).
     * @param bodyInserter  The BodyInserter to insert the request body.
     * @param headers       A consumer to set additional HTTP headers.
     * @return A Mono representing the response body.
     * @throws IllegalArgumentException If the provided HTTP method is not supported.
     */
    private Mono<String> executeWithBody(String uri,
                                         HttpMethod httpMethod,
                                         BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                                         Consumer<HttpHeaders> headers) {
        WebClient.RequestBodySpec bodySpec = switch (httpMethod) {
            case POST -> client().post().uri(uri);
            case PUT -> client().put().uri(uri);
            default -> throw new IllegalArgumentException("Unsupported request type: " + httpMethod);
        };

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec
                .headers(headers)
                .body(bodyInserter);

        return executeRequest(headersSpec);
    }

    /**
     * Executes a request with a body using a WebClient.
     *
     * @param uri        The URI of the request.
     * @param httpMethod The HTTP method of the request (e.g., POST, PUT).
     * @param request    The request body.
     * @return A Mono representing the response body.
     * @throws IllegalArgumentException If the provided HTTP method is not supported.
     */
    private Mono<String> executeWithBody(String uri,
                                         HttpMethod httpMethod,
                                         String request) {
        WebClient.RequestBodySpec bodySpec = switch (httpMethod) {
            case POST -> client().post().uri(uri);
            case PUT -> client().put().uri(uri);
            default -> throw new IllegalArgumentException("Unsupported request type: " + httpMethod);
        };

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec
                .body(Mono.just(request), request.getClass());

        return executeRequest(headersSpec);
    }

    /**
     * Executes a request with a body using a WebClient.
     *
     * @param uri           The URI of the request.
     * @param httpMethod    The HTTP method of the request (e.g., POST, PUT).
     * @param bodyInserter  The BodyInserter to insert the request body.
     * @return A Mono representing the response body.
     * @throws IllegalArgumentException If the provided HTTP method is not supported.
     */
    private Mono<String> executeWithBody(String uri,
                                         HttpMethod httpMethod,
                                         BodyInserter<?, ? super ClientHttpRequest> bodyInserter) {
        WebClient.RequestBodySpec bodySpec = switch (httpMethod) {
            case POST -> client().post().uri(uri);
            case PUT -> client().put().uri(uri);
            default -> throw new IllegalArgumentException("Unsupported request type: " + httpMethod);
        };

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec
                .body(bodyInserter);

        return executeRequest(headersSpec);
    }

    /**
     * Gets the WebClient.RequestHeadersSpec based on the provided URI and HTTP method.
     *
     * @param uri        The URI of the request.
     * @param httpMethod The HTTP method of the request (e.g., POST, PUT, GET, DELETE).
     * @return The configured WebClient.RequestHeadersSpec.
     * @throws IllegalArgumentException If the provided HTTP method is not supported.
     */
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

    /**
     * Executes a request without a request body using a WebClient.
     *
     * @param uri           The URI of the request.
     * @param httpMethod    The HTTP method of the request (e.g., POST, PUT, GET, DELETE).
     * @param headers       A consumer to set additional HTTP headers.
     * @return A Mono representing the response body.
     * @throws IllegalArgumentException If the provided HTTP method is not supported.
     */
    private Mono<String> executeWithoutBody(String uri,
                                            HttpMethod httpMethod,
                                            Consumer<HttpHeaders> headers) {
        WebClient.RequestHeadersSpec<?> headersSpec = getHeadersSpec(uri, httpMethod);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = headersSpec.headers(headers);
        return executeRequest(requestHeadersSpec);
    }

    /**
     * Executes a request without a request body using a WebClient.
     *
     * @param uri        The URI of the request.
     * @param httpMethod The HTTP method of the request (e.g., POST, PUT, GET, DELETE).
     * @return A Mono representing the response body.
     * @throws IllegalArgumentException If the provided HTTP method is not supported.
     */
    private Mono<String> executeWithoutBody(String uri,
                                            HttpMethod httpMethod) {
        WebClient.RequestHeadersSpec<?> headersSpec = getHeadersSpec(uri, httpMethod);
        return executeRequest(headersSpec);
    }

    /**
     * Invokes an API request with a request body using a WebClient.
     *
     * @param uri        The URI of the API.
     * @param httpMethod The HTTP method of the API request (e.g., POST, PUT).
     * @param request    The request body.
     * @param headers    A consumer to set additional HTTP headers.
     * @return A Mono representing the response body.
     */
    @Override
    public Mono<String> invokeApi(String uri,
                                  HttpMethod httpMethod,
                                  String request,
                                  Consumer<HttpHeaders> headers) {
        return executeWithBody(uri, httpMethod, request, headers);
    }

    /**
     * Invokes an API request with a custom body using a WebClient.
     *
     * @param uri           The URI of the API.
     * @param httpMethod    The HTTP method of the API request (e.g., POST, PUT).
     * @param bodyInserter  The BodyInserter to insert the request body.
     * @param headers       A consumer to set additional HTTP headers.
     * @return A Mono representing the response body.
     */
    @Override
    public Mono<String> invokeApi(String uri,
                                  HttpMethod httpMethod,
                                  BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                                  Consumer<HttpHeaders> headers) {
        return executeWithBody(uri, httpMethod, bodyInserter, headers);
    }

    /**
     * Invokes an API request with a request body using a WebClient.
     *
     * @param uri        The URI of the API.
     * @param httpMethod The HTTP method of the API request (e.g., POST, PUT).
     * @param request    The request body.
     * @return A Mono representing the response body.
     */
    @Override
    public Mono<String> invokeApi(String uri,
                                  HttpMethod httpMethod,
                                  String request) {
        return executeWithBody(uri, httpMethod, request);
    }

    /**
     * Invokes an API request with a custom body using a WebClient.
     *
     * @param uri           The URI of the API.
     * @param httpMethod    The HTTP method of the API request (e.g., POST, PUT).
     * @param bodyInserter  The BodyInserter to insert the request body.
     * @return A Mono representing the response body.
     */
    @Override
    public Mono<String> invokeApi(String uri,
                                  HttpMethod httpMethod,
                                  BodyInserter<?, ? super ClientHttpRequest> bodyInserter) {
        return executeWithBody(uri, httpMethod, bodyInserter);
    }

    /**
     * Invokes an API request without a request body using a WebClient.
     *
     * @param uri        The URI of the API.
     * @param httpMethod The HTTP method of the API request (e.g., GET, DELETE).
     * @param headers    A consumer to set additional HTTP headers.
     * @return A Mono representing the response body.
     */
    @Override
    public Mono<String> invokeApi(String uri,
                                  HttpMethod httpMethod,
                                  Consumer<HttpHeaders> headers) {
        return executeWithoutBody(uri, httpMethod, headers);
    }

    /**
     * Invokes an API request without a request body using a WebClient.
     *
     * @param uri        The URI of the API.
     * @param httpMethod The HTTP method of the API request (e.g., GET, DELETE).
     * @return A Mono representing the response body.
     */
    @Override
    public Mono<String> invokeApi(String uri,
                                  HttpMethod httpMethod) {
        return executeWithoutBody(uri, httpMethod);
    }

    //------------------Async method--------------------------
    /**
     * Invokes an API request with a request body using a WebClient.
     *
     * @param uri        The URI of the API.
     * @param httpMethod The HTTP method of the API request (e.g., POST, PUT).
     * @param request    The request body.
     * @param headers    A consumer to set additional HTTP headers.
     * @return A Mono representing the response body.
     */
    @Override
    public CompletableFuture<String> invokeApiAsync(String uri,
                                                    HttpMethod httpMethod,
                                                    String request,
                                                    Consumer<HttpHeaders> headers) {
        return toCompletableFuture(executeWithBody(uri, httpMethod, request, headers));
    }

    /**
     * Invokes an API request with a custom body using a WebClient.
     *
     * @param uri           The URI of the API.
     * @param httpMethod    The HTTP method of the API request (e.g., POST, PUT).
     * @param bodyInserter  The BodyInserter to insert the request body.
     * @param headers       A consumer to set additional HTTP headers.
     * @return A Mono representing the response body.
     */
    @Override
    public CompletableFuture<String> invokeApiAsync(String uri,
                                                    HttpMethod httpMethod,
                                                    BodyInserter<?, ? super ClientHttpRequest> bodyInserter,
                                                    Consumer<HttpHeaders> headers) {
        return toCompletableFuture(executeWithBody(uri, httpMethod, bodyInserter, headers));
    }

    /**
     * Invokes an API request with a custom body using a WebClient.
     *
     * @param uri           The URI of the API.
     * @param httpMethod    The HTTP method of the API request (e.g., POST, PUT).
     * @return A Mono representing the response body.
     */
    @Override
    public CompletableFuture<String> invokeApiAsync(String uri,
                                                    HttpMethod httpMethod,
                                                    String request) {
        return toCompletableFuture(executeWithBody(uri, httpMethod, request));
    }

    /**
     * Invokes an API request with a custom body using a WebClient.
     *
     * @param uri           The URI of the API.
     * @param httpMethod    The HTTP method of the API request (e.g., POST, PUT).
     * @param bodyInserter  The BodyInserter to insert the request body.
     * @return A Mono representing the response body.
     */
    @Override
    public CompletableFuture<String> invokeApiAsync(String uri,
                                                    HttpMethod httpMethod,
                                                    BodyInserter<?, ? super ClientHttpRequest> bodyInserter) {
        return toCompletableFuture(executeWithBody(uri, httpMethod, bodyInserter));
    }

    /**
     * Invokes an API request with a custom body using a WebClient.
     *
     * @param uri           The URI of the API.
     * @param httpMethod    The HTTP method of the API request (e.g., POST, PUT).
     * @param headers       A consumer to set additional HTTP headers.
     * @return A Mono representing the response body.
     */
    @Override
    public CompletableFuture<String> invokeApiAsync(String uri,
                                                    HttpMethod httpMethod,
                                                    Consumer<HttpHeaders> headers) {
        return toCompletableFuture(executeWithoutBody(uri, httpMethod, headers));
    }

    /**
     * Invokes an API request with a custom body using a WebClient.
     *
     * @param uri           The URI of the API.
     * @param httpMethod    The HTTP method of the API request (e.g., POST, PUT).
     * @return A Mono representing the response body.
     */
    @Override
    public CompletableFuture<String> invokeApiAsync(String uri,
                                                    HttpMethod httpMethod) {
        return toCompletableFuture(executeWithoutBody(uri, httpMethod));
    }
}
