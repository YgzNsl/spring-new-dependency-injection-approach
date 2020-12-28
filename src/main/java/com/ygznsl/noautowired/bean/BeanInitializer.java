package com.ygznsl.noautowired.bean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@Component
public class BeanInitializer
{

    private static final Logger logger = LogManager.getLogger(BeanInitializer.class);

    private final List<BeanProvider> beanProviders = new LinkedList<>();
    private final ListableBeanFactory beanFactory;

    @Autowired
    public BeanInitializer(ListableBeanFactory beanFactory)
    {
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    private void init()
    {
        // First, collect all bean providers
        collectBeanProviders();

        // Next, fill those bean providers
        autowireBeanProviders();

        // Assign bean factory to Beans class (in order to use self() method)
        Beans.beanFactory = beanFactory;
    }

    private void collectBeanProviders()
    {
        final String[] beanNames = beanFactory.getBeanNamesForType(BeanProvider.class);

        for (String beanName : beanNames)
        {
            final BeanProvider beanProvider = beanFactory.getBean(beanName, BeanProvider.class);
            beanProviders.add(beanProvider);
        }
    }

    private void autowireBeanProviders()
    {
        for (BeanProvider beanProvider : beanProviders)
        {
            // Call onBeforeLoad() method of provider
            beanProvider.onBeforeLoad();

            loadBeanProvider(beanProvider);

            // Call onAfterLoad() method of provider
            beanProvider.onAfterLoad();
        }
    }

    private void loadBeanProvider(BeanProvider provider)
    {
        final Class<? extends BeanProvider> type = provider.getClass();
        final Field[] fields = type.getDeclaredFields();

        // Iterate through all fields of bean provider
        for (Field field : fields)
        {
            final int mod = field.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod) || Modifier.isTransient(mod))
            {
                // If this field is static, final or transient; skip initialization
                continue;
            }

            final SkipInitialization skipAnnotation = field.getDeclaredAnnotation(SkipInitialization.class);
            if (skipAnnotation != null)
            {
                // If this field has @SkipInitialization annotation; skip initialization
                continue;
            }

            loadBeanProviderField(provider, type, field);
        }
    }

    private void loadBeanProviderField(BeanProvider provider, Class<? extends BeanProvider> providerType, Field field)
    {
        final Class<?> type = field.getType();

        // Find the candidate bean names for the bean type
        final String[] candidateBeanNames = beanFactory.getBeanNamesForType(type);

        String beanName = null;
        if (candidateBeanNames.length == 0)
        {
            logger.warn("No beans found for field {} in provider {}. Skipping", field.getName(), providerType.getName());
        }
        else if (candidateBeanNames.length == 1)
        {
            // If there are 1 candidate name, that's it!
            beanName = candidateBeanNames[0];
        }
        else
        {
            // If there are more than 1 candidate name,
            // then look for a @Qualifier annotation to decide the bean name
            final Qualifier qualifier = field.getDeclaredAnnotation(Qualifier.class);
            if (qualifier == null)
            {
                logger.warn("Multiple beans found for field {} in provider {}. Consider declaring @Qualifier.",
                        field.getName(), providerType.getName());
            }
            else
            {
                beanName = qualifier.value();
            }
        }

        if (beanName == null)
        {
            return;
        }

        final Object bean;

        try
        {
            // Actually initialize the bean
            bean = beanFactory.getBean(beanName);
        }
        catch (Exception ex)
        {
            logger.warn("Unable to load bean \"{}\" for field {} in provider {}. Possibly not found.",
                    new Object[]{beanName, field.getName(), providerType.getName()});

            return;
        }

        // At this point, everything is ready.
        // We only have to assign the bean to field.
        autowireBeanViaSetterMethod(provider, providerType, field, type, beanName, bean);
    }

    // Returns the camel case setter method name of a field
    private String findSetterMethodName(String fieldName)
    {
        return "set"
                + fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH)
                + fieldName.substring(1);
    }

    private void autowireBeanViaSetterMethod(BeanProvider provider, Class<? extends BeanProvider> providerType,
            Field field, Class<?> fieldType, String beanName, Object bean)
    {
        try
        {
            final String setterMethodName = findSetterMethodName(field.getName());
            final Method setterMethod = providerType.getDeclaredMethod(setterMethodName, fieldType);

            // At this point, a setter method is found.
            // Use it to assign bean.
            invokeSetterMethod(provider, providerType, field, setterMethod, beanName, bean);
        }
        catch (NoSuchMethodException ex)
        {
            // No setter method found.
            // Directly assign field via reflection.
            autowireBeanViaFieldSet(provider, providerType, field, beanName, bean);
        }
    }

    private void invokeSetterMethod(BeanProvider provider, Class<? extends BeanProvider> providerType,
            Field field, Method setterMethod, String beanName, Object bean)
    {
        try
        {
            setterMethod.invoke(provider, bean);
        }
        catch (Exception ex)
        {
            logger.warn(String.format("Error while autowiring bean \"%s\" for field %s in provider %s.",
                    beanName, field.getName(), providerType.getName()), ex);
        }
    }

    private void autowireBeanViaFieldSet(BeanProvider provider, Class<? extends BeanProvider> providerType,
            Field field, String beanName, Object bean)
    {
        try
        {
            if (!field.isAccessible())
            {
                field.setAccessible(true);
            }

            field.set(provider, bean);
        }
        catch (Exception ex)
        {
            logger.warn(String.format("Error while autowiring bean \"%s\" for field %s in provider %s.",
                    beanName, field.getName(), providerType.getName()), ex);
        }
    }

}
