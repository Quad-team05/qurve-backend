package com.qurve.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Public policy pages used by Google Play must not require authentication. */
@Controller
public class PublicPageController {

    @GetMapping({"/account-deletion", "/account-deletion/"})
    public String accountDeletion() {
        return "redirect:/account-deletion.html";
    }

    @GetMapping({"/privacy-policy", "/privacy-policy/"})
    public String privacyPolicy() {
        return "redirect:/privacy-policy.html";
    }
}
