package com.companyos.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ============================================================
    // GOOGLE OTP
    // ============================================================

    public void sendGoogleOtp(String to, String otp) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject("CompanyOS - Google Verification Code");

            helper.setText(
                    buildGoogleOtpEmailBody(otp),
                    true
            );

            mailSender.send(message);

        } catch (MailAuthenticationException e) {

            throw new RuntimeException(
                    "Gmail SMTP authentication failed. " +
                    "Check MAIL_USERNAME and MAIL_PASSWORD. " +
                    "If using Gmail, use a Google App Password."
            );

        } catch (MailSendException e) {

            throw new RuntimeException(
                    "Gmail SMTP could not send the email. " +
                    "Actual error: " + getRootCauseMessage(e),
                    e
            );

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Could not create Google OTP email: " +
                    e.getMessage(),
                    e
            );
        }
    }


    // ============================================================
    // EMAIL VERIFICATION
    // ============================================================

    public void sendVerificationEmail(
            String to,
            String code) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(
                    "CompanyOS - Email Verification"
            );

            helper.setText(
                    buildVerificationEmailBody(code),
                    true
            );

            mailSender.send(message);

        } catch (MailAuthenticationException e) {

            throw new RuntimeException(
                    "Gmail SMTP authentication failed. " +
                    "Check MAIL_USERNAME and MAIL_PASSWORD. " +
                    "If using Gmail, use a Google App Password.",
                    e
            );

        } catch (MailSendException e) {

            throw new RuntimeException(
                    "Gmail SMTP could not send the verification email. " +
                    "Actual error: " +
                    getRootCauseMessage(e),
                    e
            );

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Could not create verification email: " +
                    e.getMessage(),
                    e
            );
        }
    }


    // ============================================================
    // PASSWORD RESET OTP
    // ============================================================

    public void sendPasswordResetOtp(
            String to,
            String code) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(mailFrom);
            helper.setTo(to);

            helper.setSubject(
                    "CompanyOS - Password Reset Code"
            );

            helper.setText(
                    buildPasswordResetEmailBody(code),
                    true
            );

            mailSender.send(message);

        } catch (MailAuthenticationException e) {

            throw new RuntimeException(
                    "Gmail SMTP authentication failed. " +
                    "Check MAIL_USERNAME and MAIL_PASSWORD. " +
                    "For Gmail, use a Google App Password.",
                    e
            );

        } catch (MailSendException e) {

            /*
             * IMPORTANT:
             * Do not hide the real SMTP error.
             * This lets us see whether the problem is:
             *
             * - authentication
             * - connection
             * - Gmail rejection
             * - invalid recipient
             * - TLS
             * - network
             */

            throw new RuntimeException(
                    "Gmail SMTP could not send the password reset email. " +
                    "Actual error: " +
                    getRootCauseMessage(e),
                    e
            );

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Could not create password reset email: " +
                    e.getMessage(),
                    e
            );
        }
    }


    // ============================================================
    // GOOGLE OTP EMAIL BODY
    // ============================================================

    private String buildGoogleOtpEmailBody(String otp) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>

                <body style="
                    font-family: Arial, sans-serif;
                    background: #f4f6f9;
                    padding: 30px;
                ">

                    <div style="
                        max-width: 520px;
                        margin: auto;
                        background: white;
                        padding: 35px;
                        border-radius: 8px;
                    ">

                        <h2 style="color:#1a56db;">
                            ◆ CompanyOS
                        </h2>

                        <h3>
                            Google Verification
                        </h3>

                        <p>
                            Your CompanyOS Google verification code is:
                        </p>

                        <div style="
                            font-size: 36px;
                            font-weight: bold;
                            letter-spacing: 8px;
                            color: #1a56db;
                            background: #eff6ff;
                            padding: 15px;
                            text-align: center;
                            border-radius: 6px;
                        ">
                            %s
                        </div>

                        <p style="color:#6b7280;">
                            This code expires in 5 minutes.
                        </p>

                        <p style="color:#6b7280;">
                            If you did not request this code,
                            you can safely ignore this email.
                        </p>

                    </div>

                </body>
                </html>
                """.formatted(otp);
    }


    // ============================================================
    // EMAIL VERIFICATION BODY
    // ============================================================

    private String buildVerificationEmailBody(
            String code) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>

                <body style="
                    font-family: Arial, sans-serif;
                    background: #f4f6f9;
                    padding: 30px;
                ">

                    <div style="
                        max-width: 520px;
                        margin: auto;
                        background: white;
                        padding: 35px;
                        border-radius: 8px;
                    ">

                        <h2 style="color:#1a56db;">
                            ◆ CompanyOS
                        </h2>

                        <h3>
                            Verify your email
                        </h3>

                        <p>
                            Use the verification code below
                            to verify your CompanyOS account.
                        </p>

                        <div style="
                            font-size: 36px;
                            font-weight: bold;
                            letter-spacing: 8px;
                            color: #1a56db;
                            background: #eff6ff;
                            padding: 15px;
                            text-align: center;
                            border-radius: 6px;
                        ">
                            %s
                        </div>

                        <p style="color:#6b7280;">
                            This code expires in 5 minutes.
                        </p>

                    </div>

                </body>
                </html>
                """.formatted(code);
    }


    // ============================================================
    // PASSWORD RESET EMAIL BODY
    // ============================================================

    private String buildPasswordResetEmailBody(
            String code) {

        return """
                <!DOCTYPE html>
                <html>

                <head>
                    <meta charset="UTF-8">

                    <style>

                        body {
                            font-family: Arial, sans-serif;
                            background: #f4f6f9;
                            margin: 0;
                            padding: 0;
                        }

                        .container {
                            max-width: 520px;
                            margin: 40px auto;
                            background: #ffffff;
                            border-radius: 8px;
                            padding: 40px;
                            box-shadow:
                                0 2px 8px
                                rgba(0,0,0,0.08);
                        }

                        .logo {
                            font-size: 22px;
                            font-weight: bold;
                            color: #1a56db;
                            margin-bottom: 24px;
                        }

                        .code {
                            font-size: 36px;
                            font-weight: bold;
                            letter-spacing: 8px;
                            color: #1a56db;
                            background: #eff6ff;
                            border-radius: 6px;
                            padding: 16px 24px;
                            display: inline-block;
                            margin: 20px 0;
                        }

                        .footer {
                            font-size: 12px;
                            color: #9ca3af;
                            margin-top: 32px;
                        }

                    </style>

                </head>

                <body>

                    <div class="container">

                        <div class="logo">
                            ◆ CompanyOS
                        </div>

                        <h2 style="color:#111827;">
                            Password Reset
                        </h2>

                        <p style="color:#6b7280;">
                            Use the verification code below
                            to reset your CompanyOS password.
                        </p>

                        <div class="code">
                            %s
                        </div>

                        <p style="color:#6b7280;">
                            This code expires in 5 minutes
                            and can only be used once.
                        </p>

                        <p style="color:#6b7280;">
                            If you did not request a password reset,
                            you can safely ignore this email.
                        </p>

                        <div class="footer">
                            © 2026 CompanyOS ·
                            Build. Learn. Improve. Grow.
                        </div>

                    </div>

                </body>

                </html>
                """.formatted(code);
    }


    // ============================================================
    // ROOT CAUSE HELPER
    // ============================================================

    private String getRootCauseMessage(
            Throwable throwable) {

        Throwable root = throwable;

        while (root.getCause() != null) {
            root = root.getCause();
        }

        String message = root.getMessage();

        if (message == null || message.isBlank()) {
            return root.getClass().getSimpleName();
        }

        return message;
    }
}