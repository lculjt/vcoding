package com.vcoding.globaltrend.infrastructure.external.validation;

import com.vcoding.globaltrend.config.GlobalTrendSourceProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URI;
import java.time.Duration;
import java.util.function.Consumer;

@Component
public class SourceHttpClient {
    private final RestClient restClient;

    public SourceHttpClient(GlobalTrendSourceProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getHttp().getConnectTimeoutMillis()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getHttp().getReadTimeoutMillis()));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public HttpResult get(URI uri, Consumer<HttpHeaders> headersCustomizer) {
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(uri)
                    .headers(headersCustomizer)
                    .retrieve()
                    .toEntity(String.class);
            return new HttpResult(
                    response.getStatusCode().value(),
                    response.getBody(),
                    response.getHeaders(),
                    null
            );
        } catch (RestClientResponseException exception) {
            return new HttpResult(
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString(),
                    exception.getResponseHeaders() == null ? new HttpHeaders() : exception.getResponseHeaders(),
                    exception.getMessage()
            );
        } catch (RestClientException exception) {
            return new HttpResult(0, null, new HttpHeaders(), exception.getMessage());
        }
    }

    /**
     * 第三方 HTTP 调用结果。失败时也会返回对象，避免 connector 直接处理不同异常类型。
     *
     * @param statusCode HTTP 状态码，网络异常且没有响应时为 0
     * @param body 响应体文本，网络异常时可能为空
     * @param headers 响应头，网络异常时为空集合
     * @param errorMessage RestClient 捕获到的异常摘要
     */
    public record HttpResult(int statusCode, String body, HttpHeaders headers, String errorMessage) {
        public boolean is2xxSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
