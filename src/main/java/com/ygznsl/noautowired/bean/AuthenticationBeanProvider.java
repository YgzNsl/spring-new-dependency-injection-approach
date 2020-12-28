package com.ygznsl.noautowired.bean;

import com.ygznsl.noautowired.service.AuthenticationService;
import com.ygznsl.noautowired.service.StoreService;
import com.ygznsl.noautowired.service.UserService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Component
@Setter(AccessLevel.PROTECTED)
public class AuthenticationBeanProvider extends BeanProvider
{

    private AuthenticationService authenticationService;
    private UserService userService;
    private StoreService storeService;

    @Override
    protected void onAfterLoad()
    {
        Beans.auth = this;
    }

}
