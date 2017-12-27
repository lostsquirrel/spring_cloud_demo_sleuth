package demo.spring.sleuth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RestController
public class SleuthController {

    private static Logger log = LoggerFactory.getLogger(SleuthController.class);

    @Autowired
    Tracer tracer;
    @Autowired
    RestTemplate restTemplate;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${my.next.services}")
    private String nextServices;
    @Value("${server.port}")
    private int port;

    @RequestMapping("/")
    public String home() {
        log.info("Handling home");
        log.debug("debug in home");
        return "Hello World";
    }

    @RequestMapping("/foo")
    public String foo(@RequestParam(value = "timeout", defaultValue = "100")Long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("client: Baggage for [key] is [" + tracer.getCurrentSpan().getBaggageItem("key") + "]");
        StringBuilder sb = new StringBuilder().append(String.format("Hello from %s", appName));
        if (!StringUtils.isEmpty(nextServices)) {
            sb.append(", response");
            Arrays.asList(nextServices.split(";")).forEach((item)  -> {
                String[] _item = item.split(",");
                String k = _item[0];
                String v = _item[1];
                log.info("Hello from {}. Calling {}", appName, k);
                String url = String.format("http://%s:%s/%s", k, port, v);
                String serviceX = restTemplate.getForObject(url, String.class);
                log.info("Got response from {} [{}]", k, serviceX);
                sb.append(String.format(" from %s [%s]", k, serviceX));
                try {
                    TimeUnit.MILLISECONDS.sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }



        return sb.toString();
    }

    @RequestMapping("/baz")
    public String baz(@RequestParam(value = "timeout", defaultValue = "100")Long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String msg = String.format("Hello from %s", appName);
        log.info(msg);
        log.info("{}: Baggage for [key] is [{}]", appName, tracer.getCurrentSpan().getBaggageItem("key"));
        return msg;
    }

    @RequestMapping("/readtimeout")
    public String connectionTimeout() throws InterruptedException {
        Span span = this.tracer.createSpan("second_span");
        Thread.sleep(500);
        try {
            log.info("Calling a missing service");
            restTemplate.getForObject(String.format("http://localhost:%s/blowup", port), String.class);
            return "Should blow up";
        } catch (Exception e) {
            log.error("Exception occurred while trying to send a request to a missing service", e);
            throw e;
        } finally {
            this.tracer.close(span);
        }
    }

    @RequestMapping("/blowup")
    public Callable<String> blowUp() throws InterruptedException {
        return () -> {
            Thread.sleep(4000);
            throw new RuntimeException("Should blow up");
        };
    }

}
