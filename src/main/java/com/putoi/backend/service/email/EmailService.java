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
        String subject = "Verifikasi Email Akun Putoi";
        String body = String.format(
                "Halo,\n\nTerima kasih telah mendaftar di Putoi.\n\n"
                + "Untuk menyelesaikan proses pendaftaran, silakan masukkan kode verifikasi berikut:\n\n"
                + "Kode Verifikasi: %s\n\n"
                + "Kode ini berlaku selama 10 menit. Jika Anda tidak melakukan pendaftaran, abaikan email ini.\n\n"
                + "Terima kasih,\nTim Putoi",
                otp
        );

        sendEmail(to, subject, body);
    }

    public void sendResetPasswordEmail(String to, String resetLink) {
        String subject = "Permintaan Reset Password Putoi";
        String body = String.format(
                "Halo,\n\nKami menerima permintaan untuk mereset password akun Putoi Anda.\n\n"
                + "Silakan klik tautan berikut untuk mengganti password Anda:\n%s\n\n"
                + "Tautan ini berlaku selama 1 jam. Jika Anda tidak meminta reset password ini, harap abaikan email ini.\n\n"
                + "Terima kasih,\nTim Putoi",
                resetLink
        );

        sendEmail(to, subject, body);
    }

    public void sendPasswordChangedNotification(String to, String name) {
        String subject = "Pemberitahuan: Password Berhasil Diubah";
        String body = String.format(
                "Halo %s,\n\nKami ingin memberitahukan bahwa password akun Putoi Anda telah berhasil diperbarui.\n\n"
                + "Jika Anda tidak melakukan perubahan ini, segera hubungi tim support kami untuk memastikan keamanan akun Anda.\n\n"
                + "Terima kasih,\nTim Putoi",
                name
        );

        sendEmail(to, subject, body);
    }
}
