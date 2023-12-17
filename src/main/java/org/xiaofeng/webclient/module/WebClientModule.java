package org.xiaofeng.webclient.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.xiaofeng.webclient.service.WebClientService;
import org.xiaofeng.webclient.service.WebClientServiceImpl;
import reactor.netty.http.client.HttpClient;

public class WebClientModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WebClientService.class).to(WebClientServiceImpl.class);
    }

    @Provides
    public WebClient.Builder clientBuilder() {
        HttpClient httpClient = HttpClient.create().doOnRequest(
                ((httpClientRequest, connection) -> connection.addHandlerLast(new ReadTimeoutHandler(10))
                        .addHandlerLast(new WriteTimeoutHandler(10)).channel().config()
                        .setConnectTimeoutMillis(10000)));
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder()
                .codecs(clientCodecConfigurer -> {
                    clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(new ObjectMapper(), MediaType.APPLICATION_JSON));
                    clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(new ObjectMapper(), MediaType.APPLICATION_JSON));
                })
                .clientConnector(connector);
    }
}
