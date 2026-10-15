package com.example.This_App_Backend.service;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
public class PayfastService {
    
    @Value("${payfast.merchant-id}")
    private String merchantId;

    @Value("${payfast.merchant-key}")
    private String merchantKey;

    @Value("${payfast.passphrase}")
    private String passphrase;

    @Value("${payfast.sandbox}")
    private boolean sandbox;

    @Value("${payfast.return.url}")
    private String returnUrl;

    @Value("${payfast.cancel.url}")
    private String cancelUrl;

    @Value("${payfast.notify.url}")
    private String notifyUrl;

    //Build payfast payment Url for redirect
    public String buildPaymentUrl(
        String pendingCheckoutId,
        BigDecimal amount,
        String itemName,
        String customerFirstName,
        String customerLastName,
        String customerEmail
    ) throws Exception{
        //Parameters must be in exact order for correct signature generation
        Map<String, String> params = new LinkedHashMap<>();
        params.put("merchant_id", merchantId);
        params.put("merchant_key", merchantKey);
        params.put("return_url", returnUrl);
        params.put("cancel_url", cancelUrl);
        params.put("notify_url", notifyUrl);
        params.put("name_first", customerFirstName);
        params.put("name_last", customerLastName);
        params.put("email_address", customerEmail);
        params.put("m_payment_id", pendingCheckoutId); // Unique ID for the transaction
        params.put("amount", String.format(Locale.US, "%.2f", amount));
        params.put("item_name", itemName);

        String signature = generateSignature(params);
        params.put("signature", signature);

        //Build query string for redirect url
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()){
            if(queryString.length() > 0) queryString.append("&");
            queryString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            queryString.append("=");
            queryString.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        String host = sandbox
        ? "https://sandbox.payfast.co.za/eng/process"
        : "https://www.payfast.co.za/eng/process";

        return host + "?" + queryString.toString();
    }

/**
     * Verifies the ITN notification PayFast POSTs to your notify_url.
     * Returns true if the signature and amount are valid.
     */
    public boolean verifyITN(Map<String, String> itnParams, BigDecimal expectedAmount){
        try{
            String receivedSignature    = itnParams.get("signature");
            String recievedStatus       = itnParams.get("payment_status");
            String recievedAmount       = itnParams.get("amount_gross");

            if (!"COMPLETE".equalsIgnoreCase(recievedStatus)){
                return false; // Payment not completed
            }

            //Amount check - compare with tolerance for floating point
            BigDecimal itnAmount = new BigDecimal(recievedAmount);
            if(itnAmount.compareTo(expectedAmount) != 0){
                return false;
            }

            Map<String, String> checkParams = new LinkedHashMap<>(itnParams);
            checkParams.remove("signature");

            String computedSignature = generateSignatureFromRaw(checkParams);
            return computedSignature.equalsIgnoreCase(receivedSignature);
        } catch( Exception e){
            return false;
        }
    }

    //Private helper methods
    private String generateSignature(Map<String, String> params) throws Exception {
        return generateSignatureFromRaw(params);
    }

    private String generateSignatureFromRaw (Map<String, String> params) throws Exception{
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if(sb.length() > 0) {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        //Appendd passphrase
        sb.append("&passphrase=").append(URLEncoder.encode(passphrase, StandardCharsets.UTF_8));
        
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for(byte b : hashBytes){
            hexString.append(String.format("%02x",b));
        }
        return hexString.toString();
    }
}
