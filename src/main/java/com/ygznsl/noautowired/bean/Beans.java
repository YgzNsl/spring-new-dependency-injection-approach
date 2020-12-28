package com.ygznsl.noautowired.bean;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.ListableBeanFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Beans
{

    static ListableBeanFactory beanFactory = null;

    static AuthenticationBeanProvider auth = null;
    static SalesBeanProvider sales = null;
    static StatisticsBeanProvider stats = null;


    public static <T> T self(T bean)
    {
        return beanFactory.getBean((Class<T>) bean);
    }


    public static AuthenticationBeanProvider auth()
    {
        return auth;
    }

    public static SalesBeanProvider sales()
    {
        return sales;
    }

    public static StatisticsBeanProvider stats()
    {
        return stats;
    }

}
