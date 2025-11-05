/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.putoi.backend.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 *
 * @author alfia
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendVerificationEmail(String to, String otp) {
        String subject = "Verifikasi Email Anda";
        String body = String.format(
                "Halo,\n\nTerima kasih telah mendaftar. Kode verifikasi email Anda adalah: %s\n\nKode ini berlaku selama 10 menit.",
                otp
        );

        sendEmail(to, subject, body);
    }

    public void sendResetPasswordEmail(String to, String resetLink) {
        String subject = "Reset Password";
        String body = String.format(
                "Halo,\n\nKami menerima permintaan untuk mereset password akun Anda. "
                + "Klik tautan berikut untuk mengganti password (masa berlaku 1 jam):\n\n%s\n\n"
                + "Jika Anda tidak meminta ini, abaikan email ini.\n\nSalam,\nTim Anda",
                resetLink
        );

        sendEmail(to, subject, body);
    }

    public void sendPasswordChangedNotification(String to, String name) {
        String subject = "Password Berhasil Diubah";
        String body = String.format(
                "Halo %s,\n\nPassword akun Anda telah berhasil diubah.\n"
                + "Jika bukan Anda yang melakukan perubahan ini, segera hubungi tim support.\n\nSalam,\nTim Anda",
                name
        );
        sendEmail(to, subject, body);
    }
}
