//package com.ceylonbank.webbasedbankingsystem.controller;
//
//import com.ceylonbank.webbasedbankingsystem.entity.User;
//import com.ceylonbank.webbasedbankingsystem.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//
//@Controller
//public class SignupController {
//
//    @Autowired
//    private UserService userService;
//
//    @GetMapping("/signup")
//    public String showSignupPage(Model model) {
//        model.addAttribute("user", new User());
//        return "signup";
//    }
//
//    @PostMapping("/signup")
//    public String processSignup(@ModelAttribute User user, Model model) {
//        try {
//            userService.createCustomer(user);
//            return "redirect:/login?success=true";
//        } catch (IllegalArgumentException e) {
//            model.addAttribute("error", e.getMessage());
//            model.addAttribute("user", user); // Preserve form data
//            return "signup";
//        }
//    }
//}

package com.ceylonbank.webbasedbankingsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignupController {
    @GetMapping("/signup")
    public String signupStep1() {
        return "signup";
    }

    @GetMapping("/signup-step2")
    public String signupStep2() {
        return "signup-step2";
    }

    @GetMapping("/signup-step3")
    public String signupStep3() {
        return "signup-step3";
    }
}