package com.ygznsl.noautowired.controller;

import com.ygznsl.noautowired.bean.Beans;
import com.ygznsl.noautowired.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PurchaseController
{

    @Autowired
    private AdService adService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private BasketService basketService;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @RequestMapping
    public void someMethod()
    {
        final UserService userService = Beans
                .auth()
                .getUserService();

        final ProductService productService = Beans
                .sales()
                .getProductService();

        final NotificationService notificationService = Beans
                .stats()
                .getNotificationService();
    }

}
