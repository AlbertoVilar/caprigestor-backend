package com.devmaster.goatfarm.authority.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    public TestController() {
    }

    @PostMapping("/login")
    public String testLogin(@RequestParam String username, @RequestParam String password) {
        return "Login test - Username: " + username + ", Password received (authentication disabled)";
    }
    
    @GetMapping("/generate-hash")
    public String generateHash(@RequestParam String password) {
        return "Hash generation disabled (authentication removed)";
    }
    
    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String email, @RequestParam String newPassword) {
        return "Password update disabled (authentication removed)";
    }
}