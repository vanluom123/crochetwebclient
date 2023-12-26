package org.xiaofeng.webclient.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.xiaofeng.webclient.service.WebClientService;
import org.xiaofeng.webclient.service.WebClientServiceImpl;
import reactor.netty.http.client.HttpClient;

/**
 * WebClientModule class
 */
public class WebClientModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WebClientService.class).to(WebClientServiceImpl.class);
    }

    /**
     * Creates a WebClient.Builder with custom configurations.
     *
     * @return The configured WebClient.Builder.
     */
    @Provides
    public WebClient.Builder clientBuilder() {
        HttpClient httpClient = HttpClient.create().doOnRequest(
                ((httpClientRequest, connection) -> connection.addHandlerLast(new ReadTimeoutHandler(10))
                        .addHandlerLast(new WriteTimeoutHandler(10)).channel().config()
                        .setConnectTimeoutMillis(10000)));
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder()
                .clientConnector(connector);
    }
}
