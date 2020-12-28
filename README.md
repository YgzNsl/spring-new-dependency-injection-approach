
# Spring dependency management — A new approach to wire beans



Spring is in many ways a great library. At the core of the Spring lies the concept of dependency injection. Basically, it means, we identify our dependencies to Spring, and then when we need one, it is created and supplied for us. This is achieved so easily, by using @Autowired annotation above a field, setter method or constructor.

While this technique is quite handy, it may result in bad code blocks in poorly designed large projects. In many enterprise companies, projects start with a little piece of code. But as the time goes on, projects get bigger and bigger. Inevitably, god classes occur everywhere in the project. More and more dependencies are needed and injected.

<iframe src="https://medium.com/media/f5c78ff8bc26a0c2b98c8120205caf2a" frameborder=0></iframe>

This is an example of a bad controller class in a Spring Boot project. 13 dependencies are injected to the class. Of course, if you have so much dependencies, you cannot (should not) use constructor injection. Compulsorily, field injection is used. Autowired annotation is placed over each field. This is actually a small class compared to real world examples. I have seen classes that inject more than 50 dependencies!

This is bad programming of course. Spring has no fault. It is very likely that you come across these problems in enterprise companies. In real world, people often do not volunteer to fix these problems in large-scale projects, as it is seen too risky and discouraged. However, the first (and best) solution would be to decompose this god class into smaller pieces so that each piece has minimal dependencies which is easy to maintain. But if this is not an option, I have another solution to overcome this problem.

In this article, I will propose a new approach to inject Spring beans to our classes. Only a Spring Boot project is necessary. We’ll also use [Lombok](https://projectlombok.org/) to avoid the necessity of getter and setter methods. When properly applied, we will get rid of all [@Autowired](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/annotation/Autowired.html) annotations (we’ll only use it once — yes, once!).

So, let’s get into work…

First, let’s create a package in our project named “bean”. In this package, we create a class:

<iframe src="https://medium.com/media/5a55e2b490ddf534fd146ff6b4b9baf8" frameborder=0></iframe>

This is an abstract class. We’ll use it to distinguish our bean providers from other Spring beans. 2 event methods are added in case there is a need. We’ll use *onAfterLoad()* method. But, *onBeforeLoad()* is added as well.

Next, we create 3 bean provider classes. Of course, the number of bean providers is completely up to you.

<iframe src="https://medium.com/media/effef8417e89608a703fea7a66437ce3" frameborder=0></iframe>

<iframe src="https://medium.com/media/d0ec6a7b00ce501f7c9fd45e75666481" frameborder=0></iframe>

<iframe src="https://medium.com/media/4f57fa05b30234d7991fc0a56045e85f" frameborder=0></iframe>

These classes are annotated as @Component. Which means, these are Spring beans. Getter methods will be created by Lombok. Also, Lombok will create setter methods as well, but setter methods will have *protected* as access modifier. This is to prevent the classes from other packages to change our beans.

As you can clearly see, in bean provider classes, we list the beans we need. Thanks to Lombok, we don’t write getter and setter methods. We only declare the dependency we need, and do nothing more! We’ll do the binding later. Also, notice that with this approach, we can group our related beans in different bean providers. And, last but not least, it is worth to mention that a bean can be declared in several bean providers (meaning, there is no limitation such as a bean can only be defined in one provider).

In all bean providers, *onAfterLoad()* method is overridden. This method will be invoked by us later, when all of the beans defined in the provider are loaded. In this method, we assign the bean providers to the field of same class with name *Beans*. The definition of this class is followed:

<iframe src="https://medium.com/media/9d5654f392bbcd53ae06ff0b23389834" frameborder=0></iframe>

This is the class where we declare all our bean providers and create their public static getter methods. The class is meant for static methods, so the constructor is private.

So far, we created bean provider classes, and a global Beans class that can be used to access bean providers statically from anywhere in the project. But, how are we going to load these beans? Let’s do the last and most important part of this technique.

Let’s proceed step by step. We create a class named *BeanInitializer*:

<iframe src="https://medium.com/media/c45c8a1f11a7e83ca7f1e37c0b2c95d4" frameborder=0></iframe>

We declare the class as [@Component](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/stereotype/Component.html), to ensure that is instantiated and executed at the very beginning of the application. Class has 2 fields, one to hold bean providers in a list, the other to hold a [*ListableBeanFactory](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/ListableBeanFactory.html)*. ListableBeanFactory is a class to access all Spring beans and create one if needed. We autowire this object in constructor. Note that, this is the only place where we use @Autowired annotation ever!

<iframe src="https://medium.com/media/4db12e221e24344a9da5a910f8150733" frameborder=0></iframe>

We create *init()* method and annotate as [@PostConstruct](https://docs.oracle.com/javaee/7/api/javax/annotation/PostConstruct.html). It will be invoked right after the instance is created.

<iframe src="https://medium.com/media/1111770a5d410bb80f4ed94ed657c71e" frameborder=0></iframe>

Now, we create a marker annotation. This annotation can be used in bean providers to skip the initialization of a field. Let’s continue coding on *BeanInitializer* class:

<iframe src="https://medium.com/media/a76c8fa1a7f7c4b6ca6519ede62902c9" frameborder=0></iframe>

After collecting all bean providers, it is time to load the beans declared in those providers. For that, we examine the fields of bean providers.

<iframe src="https://medium.com/media/0ab412f2b8a464203a9d1456cf8d0dca" frameborder=0></iframe>

This method examines a field in bean provider. Then, if a suitable bean is found, it is created to assign to that field.

<iframe src="https://medium.com/media/9b14a079ec13aeb04680c30555a879d2" frameborder=0></iframe>

These are the final codes of our new approach. We assign the bean created to the bean provider field via (prefferably) setter method invoke or field set with reflection.

## Conclusion

This may be a long way, I know :)

But this code works perfectly. The question is this: “What are the advantages we gained with this approach?”

Let’s go back to the *PurchaseController* we saw in the beginning:

<iframe src="https://medium.com/media/3e966c5b1a201ab11054d9d6b8923208" frameborder=0></iframe>

Now, we can access our beans using static getter methods. No autowiring! We used *@Autowired *annotation only at the *BeanInitializer* class. We got rid of all the field injections.

Simple, isn’t it?

What do you think of this technique? Is it a practical approach? Has it been done before? Or does it have some downsides that I can’t see?

Stay with Java.
