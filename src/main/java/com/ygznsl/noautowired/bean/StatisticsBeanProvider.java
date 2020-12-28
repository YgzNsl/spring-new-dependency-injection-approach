package com.ygznsl.noautowired.bean;

import com.ygznsl.noautowired.service.AdService;
import com.ygznsl.noautowired.service.EmailService;
import com.ygznsl.noautowired.service.NotificationService;
import com.ygznsl.noautowired.service.SearchService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Component
@Setter(AccessLevel.PROTECTED)
public class StatisticsBeanProvider extends BeanProvider
{

    private AdService adService;
    private EmailService emailService;
    private NotificationService notificationService;
    private SearchService searchService;

    @Override
    protected void onAfterLoad()
    {
        Beans.stats = this;
    }

}
