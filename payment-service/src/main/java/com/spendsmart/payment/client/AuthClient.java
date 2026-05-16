package com.spendsmart.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", path = "/api/user")
public interface AuthClient {

    @PostMapping("/internal/upgrade")
    void upgradeUser(@RequestParam("email") String email);
}
