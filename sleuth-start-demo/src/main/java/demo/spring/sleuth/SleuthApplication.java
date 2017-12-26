package demo.spring.sleuth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@SpringBootApplication
public class SleuthApplication {

    private static final Logger log = LoggerFactory.getLogger(SleuthApplication.class);

    public static void main(String[] args) {
        new SpringApplicationBuilder(SleuthApplication.class).web(true).run(args);
    }

    @Bean
    RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(2000);
        clientHttpRequestFactory.setReadTimeout(3000);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override public boolean hasError(ClientHttpResponse response)
                    throws IOException {
                try {
                    return super.hasError(response);
                } catch (Exception e) {
                    return true;
                }
            }

            @Override public void handleError(ClientHttpResponse response)
                    throws IOException {
                try {
                    super.handleError(response);
                } catch (Exception e) {
                    log.error("Exception [" + e.getMessage() + "] occurred while trying to send the request", e);
                    throw e;
                }
            }
        });
        return restTemplate;
    }

    @Configuration
    @EnableDiscoveryClient
    static class CloudConfig {}
}
