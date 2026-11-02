package com.companyos.backend.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OtpService — manages short-lived one-time passwords for local email
 * verification.
 *
 * Security properties:
 *  • 6-digit codes generated with SecureRandom
 *  • 10-minute expiry
 *  • One-time use (entry removed on first successful match)
 *  • New OTP immediately invalidates the previous one
 *  • Max 5 wrong attempts before the OTP is locked (caller must resend)
 *  • Resend enforces a 60-second cooldown
 *  • Raw OTP is never stored after this method returns it — callers pass it
 *    directly to EmailService; no re-reading is possible
 *
 * NOTE: In-memory storage is intentional for now. On a multi-instance
 *       deployment this should be replaced with Redis.
 */
@Service
public class OtpService {

    private static final int  OTP_EXPIRY_MINUTES        = 10;
    private static final int  OTP_MAX_ATTEMPTS           = 5;
    private static final long OTP_RESEND_COOLDOWN_SECS   = 60;

    private static final class OtpEntry {
        final String        otp;           // plain 6-digit string (never logged)
        final LocalDateTime expiry;
        LocalDateTime       lastResent;    // null on first generate
        int                 attempts;      // wrong-attempt counter
        boolean             consumed;

        OtpEntry(String otp, LocalDateTime expiry) {
            this.otp       = otp;
            this.expiry    = expiry;
            this.attempts  = 0;
            this.consumed  = false;
            this.lastResent = null;
        }
    }

    private final ConcurrentHashMap<String, OtpEntry> store       = new ConcurrentHashMap<>();
    private final SecureRandom                         secureRandom = new SecureRandom();

    // ─────────────────────────────────────────────────────────────────────────
    // Generate
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generate a new 6-digit OTP for {@code email}, overwriting any previous
     * entry. Returns the raw OTP so the caller can pass it to EmailService.
     *
     * <p>The OTP is NOT logged here. Callers must not log the return value
     * in production code.
     */
    public String generateOtp(String email) {
        String key  = normalise(email);
        String otp  = String.format("%06d", secureRandom.nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        // Overwrite any previous OTP — old code is immediately invalid
        store.put(key, new OtpEntry(otp, expiry));

        return otp;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Resend
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generate a fresh OTP, enforcing the resend cooldown.
     *
     * @throws IllegalStateException  if called within the cooldown window
     */
    public String generateResendOtp(String email) {
        String   key   = normalise(email);
        OtpEntry entry = store.get(key);

        if (entry != null && entry.lastResent != null) {
            LocalDateTime earliest = entry.lastResent.plusSeconds(OTP_RESEND_COOLDOWN_SECS);
            if (LocalDateTime.now().isBefore(earliest)) {
                long remaining = java.time.temporal.ChronoUnit.SECONDS
                        .between(LocalDateTime.now(), earliest);
                throw new IllegalStateException(
                        "Please wait " + remaining + " seconds before requesting a new code.");
            }
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        OtpEntry newEntry = new OtpEntry(otp, expiry);
        newEntry.lastResent = LocalDateTime.now();
        store.put(key, newEntry);

        return otp;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Verify
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verify a submitted OTP code.
     *
     * @throws IllegalArgumentException if the code is wrong or not found
     * @throws IllegalStateException    if the code has expired or max attempts reached
     */
    public void verifyOtp(String email, String submitted) {
        String   key   = normalise(email);
        OtpEntry entry = store.get(key);

        if (entry == null) {
            throw new IllegalArgumentException(
                    "No verification code found for this email. Please request a new code.");
        }

        if (entry.consumed) {
            throw new IllegalStateException(
                    "This code has already been used. Please request a new code.");
        }

        if (LocalDateTime.now().isAfter(entry.expiry)) {
            store.remove(key);
            throw new IllegalStateException(
                    "Verification code has expired. Please request a new code.");
        }

        if (entry.attempts >= OTP_MAX_ATTEMPTS) {
            store.remove(key);
            throw new IllegalStateException(
                    "Too many incorrect attempts. Please request a new code.");
        }

        if (!entry.otp.equals(submitted.trim())) {
            entry.attempts++;
            int remaining = OTP_MAX_ATTEMPTS - entry.attempts;
            throw new IllegalArgumentException(
                    remaining > 0
                        ? "Incorrect code. " + remaining + " attempt(s) remaining."
                        : "Incorrect code. You have used all attempts. Please request a new code.");
        }

        // Valid — consume immediately (one-time use)
        entry.consumed = true;
        store.remove(key);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns true if a non-expired, non-consumed OTP exists for this email. */
    public boolean hasPendingOtp(String email) {
        OtpEntry entry = store.get(normalise(email));
        if (entry == null || entry.consumed) return false;
        if (LocalDateTime.now().isAfter(entry.expiry)) {
            store.remove(normalise(email));
            return false;
        }
        return true;
    }

    private static String normalise(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
