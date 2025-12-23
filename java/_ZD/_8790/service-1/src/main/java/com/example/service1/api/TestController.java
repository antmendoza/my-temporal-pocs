package com.example.service1.api;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("v1/test")
public class TestController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("tracing")
    public String testTracing() {
        log.info("Starting Service 1 -> Service 2 call");
        String url = "http://localhost:8082/v1/test/tracing";
        String response = restTemplate.getForObject(url, String.class);
        String s = "Initial traceId = " + MDC.get("traceId") + " \n ";
        s += "Response from Service , traceId= " + response;
        log.info(s);
        return s;
    }
}
