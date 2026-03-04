package com.example.This_App_Backend.config;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigDecimal; // Used for correct amount formatting

@Component
public class OzowSignatureUtil {

    public String generate_first_hash(
            String siteCode,
            String countryCode,
            String currencyCode,
            BigDecimal amount,
            String transactionReference,
            String successUrl,
            String cancelUrl,
            String notifyUrl,
            String privateKey,
            boolean isTest,

            //5 optional fields to be included, even if empty
            String optional1, String optional2, String optional3,
            String optional4, String optional5
    ) {
        //Initiation has string - order is CRITICAL, includes all fields
        String input = String.join("|",
                siteCode,
                countryCode,
                currencyCode,amount.toPlainString(),
                transactionReference,
                Boolean.toString(isTest).toLowerCase(),
                optional1,
                optional2,
                optional3,
                optional4,
                optional5,
                privateKey).toLowerCase();

        return hashSha512(input);
    }


    private String hashSha512(String text){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-512");

            //Hash input string as UTF-8 bytes
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            //convert byte array to hexidecimal string
            StringBuilder hexString = new StringBuilder();
            for(byte b : hash){
                //ensure output is 128 characters (512 bits) long
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().toLowerCase();
        } catch (RuntimeException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

//    public String generateHash(
//            String siteCode,
//            String countryCode,
//            String currencyCode,
//            String amount,
//            String transactionReference,
//            String successUrl,
//            String cancelUrl,
//            String notifyUrl,
//            String privateKey
//    ) {
//        String input = siteCode +
//                countryCode +
//                currencyCode +
//                amount +
//                transactionReference +
//                successUrl +
//                cancelUrl +
//                notifyUrl +
//                privateKey;
//
//        return DigestUtils.sha512Hex(input).toLowerCase();
//    }
}
