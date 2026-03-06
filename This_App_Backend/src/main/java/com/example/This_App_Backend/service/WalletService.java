package com.example.This_App_Backend.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.This_App_Backend.Enuma.EscrowStatus;
import com.example.This_App_Backend.Enuma.WalletTransactionType;
import com.example.This_App_Backend.dto.WalletDTO.WalletBalanceDTO;
import com.example.This_App_Backend.dto.WalletDTO.WalletTransactionDTO;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.Payments;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.Wallet;
import com.example.This_App_Backend.entity.WalletTransactions;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.PaymentsRepository;
import com.example.This_App_Backend.repository.WalletRepository;
import com.example.This_App_Backend.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Value;

import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;

import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;



@Service
@Transactional
public class WalletService {

    @Autowired
    private WalletRepository walletRepo;

    @Autowired
    private WalletTransactionRepository walletTransactionRepo;

    @Autowired
    private PaymentsRepository paymentRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Value("${platform.commission.percent}")
    private int commissionPercent;

    @Value("${platform.delivery.cut}")
    private int deliveryCutPercent;

    //Called by driverOrderService when an order is marked as delivered
    //Splits the money between the store admin and the driver
    public void releaseEscrowForOrder(Long orderId){
        CustomerOrders order = orderRepo.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with Id: " + orderId));

        //Find the HELD payment - gaurd against double release
        Payments payment = paymentRepo.findByOrder_OrderIdAndEscrowStatus(orderId, EscrowStatus.HELD)
        .orElseThrow(() -> new RuntimeException("No held payment found for order " + orderId + " - already released?"));

        BigDecimal orderTotal = order.getTotalAmount();
        BigDecimal shippingFee = order.getShippingAmount() != null
                                ? order.getShippingAmount() : BigDecimal.ZERO;

        //Note: orderTotal now contains ONLY items cost (shipping stored separately)
        //Admin store earnings
        //Platform commission is taken from items total only (not from shipping)
        BigDecimal platformCut = orderTotal
                                .multiply(BigDecimal.valueOf(commissionPercent))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        //Store owner receives items total minus platform commission
        BigDecimal adminEarning = orderTotal.subtract(platformCut);

        //Driver earnings 
        //Driver recieves shipping fee minus platform delivery cut
        BigDecimal platformDeliveryCut = shippingFee
                                        .multiply(BigDecimal.valueOf(deliveryCutPercent))
                                        .divide(BigDecimal.valueOf(100), 2 , RoundingMode.HALF_UP);
        
        BigDecimal driverEarning = shippingFee.subtract(platformDeliveryCut);

        //Credit the store owners wallet
        User storeOwnerUser = order.getStore().getStoreOwner().getUser();
        creditWallet(storeOwnerUser, order, adminEarning,
            "Order #" + orderId + " delivered - store earnings"
        );

        //credit the drivers walled (only if a driver was assigned)
        if(order.getIsAssignedDriver() != null){
            creditWallet(order.getAssignedDriver(), order, driverEarning,
            "Order #" + orderId + " delivered - delivery earnings");
        }

        //Release escrow - mark payment as settled
        payment.setEscrowStatus(EscrowStatus.RELEASED);
        paymentRepo.save(payment);
    }

    //Return the wallet balance DTO for a given user (admin or driver)
    //Auto creates a waller if the user has never been credited before
    public WalletBalanceDTO getBalance(Long userId){
        Wallet wallet = getOrCreateWallet(userId);
        User user = wallet.getUser();

        WalletBalanceDTO dto = new WalletBalanceDTO();
        dto.setWalletId(wallet.getWalletId());
        dto.setBalance(wallet.getBalance());
        dto.setOwnerName(user.getFirstName() + " " + user.getLastName());
        dto.setOwnerType(user.getUserType().name());
        dto.setUpdatedAt(wallet.getUpdateAt());
        return dto;
    }

    //return the full transaction history for users wallet
    public List<WalletTransactionDTO> getTransactions(Long userId){
        Wallet wallet = getOrCreateWallet(userId);
        
        return walletTransactionRepo
                .findByWallet_WalletIdOrderByCreatedAtDesc(wallet.getWalletId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    //=========Internal Helper methods================

    //Find existing wallet or create a fresh one with zero balance
    private Wallet getOrCreateWallet(Long userId){
        return walletRepo.findByUser_UserId(userId)
                .orElseGet(() -> {
                    //Lazy wallet creation - no wallet until first query
                    Wallet w = new Wallet();
                    //User reference - borrow from any repo using userId
                    //fetch from wallet repo after save
                    User userProxy = new User();
                    return walletRepo.findByUser_UserId(userId)
                            .orElseThrow(() -> new RuntimeException(
                                "Wallet not found for user " + userId + 
                                "- wallet is created automatically on first delivery credit."));  
                });
    }

    //Add funds to wallet and record the transaction
    private void creditWallet(User user, CustomerOrders order,
                            BigDecimal amount, String description)
    { 
        Wallet wallet = walletRepo.findByUser_UserId(user.getUserId())
                    .orElseGet(() -> {
                        Wallet w = new Wallet();
                        w.setUser(user);
                        w.setBalance(BigDecimal.ZERO);
                        w.setBalance(BigDecimal.ZERO);
                        return walletRepo.save(w);
                    });
        
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepo.save(wallet);

        WalletTransactions tx = new WalletTransactions();
        tx.setWallet(wallet);
        tx.setOrder(order);
        tx.setAmount(amount);
        tx.setType(WalletTransactionType.CREDIT);
        tx.setDescription(description);
        walletTransactionRepo.save(tx);
    }

    private WalletTransactionDTO toDTO(WalletTransactions tx){
        WalletTransactionDTO dto = new WalletTransactionDTO();
        dto.setTransactionId(tx.getTransactionId());
        dto.setOrderId(tx.getOrder() != null ? tx.getOrder().getOrderId() : null);
        dto.setAmount(tx.getAmount());
        dto.setType(tx.getType().name());
        dto.setDescription(tx.getDescription());
        dto.setCreatedAt(tx.getCreatedAt());
        return dto;
    }
}
