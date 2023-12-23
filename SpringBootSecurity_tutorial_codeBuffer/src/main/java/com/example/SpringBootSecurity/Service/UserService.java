package com.example.SpringBootSecurity.Service;


import com.example.SpringBootSecurity.Entity.PasswordResetToken;
import com.example.SpringBootSecurity.Entity.User;
import com.example.SpringBootSecurity.Entity.VerificationToken;
import com.example.SpringBootSecurity.Model.UserModel;
import com.example.SpringBootSecurity.Repository.PasswordResetTokenRepository;
import com.example.SpringBootSecurity.Repository.UserRepository;
import com.example.SpringBootSecurity.Repository.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;

@Service
@Component
@Slf4j
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    VerificationTokenRepository tokenRepository;

    @Autowired
    PasswordResetTokenRepository resetTokenRepository;

    @Autowired(required = true)
    private PasswordEncoder passwordEncoder;

    public User registerUser(UserModel userModel) {
        User user = new User();
        user.setEmail(userModel.getEmail());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userRepository.save(user);
        return user;
    }

    public void saveVerificationTokenForUser(String token, User user) {
        VerificationToken userToken = new VerificationToken(user, token);
        tokenRepository.save(userToken);
    }

    public String validateVerificationToken(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) return "invalid";
        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if (verificationToken.getExpirationTime().getTime() - cal.getTime().getTime() <= 0) {
            tokenRepository.delete(verificationToken);
            return "Token Expired";

        }
        user.setEnabled(true);
        userRepository.save(user);
        return "Valid";
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user, token);
        resetTokenRepository.save(passwordResetToken);
    }

    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken = resetTokenRepository.findByToken(token);
        if (passwordResetToken == null) return "invalid";
        User user = passwordResetToken.getUser();
        Calendar cal = Calendar.getInstance();
        if (passwordResetToken.getExpirationTime().getTime() - cal.getTime().getTime() <= 0) {
            resetTokenRepository.delete(passwordResetToken);
            return "Token Expired";

        }
        return "Valid";
    }

    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(resetTokenRepository.findByToken(token).getUser());
    }

    public void changepassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean checIfOldPasswordisCorrect(User user, String oldPassword) {
        log.info(user.getPassword() + " " + oldPassword);
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
}
