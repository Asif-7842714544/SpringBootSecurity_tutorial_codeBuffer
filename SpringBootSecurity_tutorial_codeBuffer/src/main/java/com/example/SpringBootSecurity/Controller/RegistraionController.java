package com.example.SpringBootSecurity.Controller;


import com.example.SpringBootSecurity.Entity.User;
import com.example.SpringBootSecurity.Events.RegistrationCompleteEvent;
import com.example.SpringBootSecurity.Model.PasswordModel;
import com.example.SpringBootSecurity.Model.UserModel;
import com.example.SpringBootSecurity.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistraionController {

    @Autowired
    UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @GetMapping("/hello")
    public String helloworld() {
        return "Hello Asif";
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
        return "saved successfully";
    }

    @GetMapping("/VerifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String res = userService.validateVerificationToken(token);
        if (res.equalsIgnoreCase("valid")) {
            return "user verified successfully";
        }
        return "bad user";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {
        User user = userService.findUserByEmail(passwordModel.getEmail());
        String url = "";
        if (user != null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            url = passwordResetTokenMail(user, applicationUrl(request), token);
        }
        return url;
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token, @RequestBody PasswordModel passwordModel) {
        String res = userService.validatePasswordResetToken(token);
        if (!res.equalsIgnoreCase("valid")) {
            return "Invalid password reset token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if (user.isPresent()) {
            userService.changepassword(user.get(), passwordModel.getNewPassword());
            return "password reset successful";
        } else {
            return "inavild token";
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel) {
        User user = userService.findUserByEmail(passwordModel.getEmail());
        if (userService.checIfOldPasswordisCorrect(user, passwordModel.getOldPassword())) {
            userService.changepassword(user, passwordModel.getNewPassword());
            return "password changed successfully";
        } else {
            return "invalid oldPassword";
        }

    }


    private String passwordResetTokenMail(User user, String applcationUrl, String token) {
        String url = applcationUrl
                + "/savePassword?token="
                + token;

        log.info("Click the link to reset your password: {}", url);
        return url;
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
