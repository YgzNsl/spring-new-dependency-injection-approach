package com.ygznsl.noautowired.bean;

import com.ygznsl.noautowired.service.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Component
@Setter(AccessLevel.PROTECTED)
public class SalesBeanProvider extends BeanProvider
{

    private BasketService basketService;
    private CampaignService campaignService;
    private DiscountService discountService;
    private PaymentService paymentService;
    private ProductService productService;
    private WalletService walletService;

    @Override
    protected void onAfterLoad()
    {
        Beans.sales = this;
    }

}
