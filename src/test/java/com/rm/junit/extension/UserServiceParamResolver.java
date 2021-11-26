package com.rm.junit.extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import service.UserService;

public class UserServiceParamResolver implements ParameterResolver {

    @Override
    // вызывается вначале, когда DI framework определяет, подходит ли этот параметр нашего метода под параметр Resolver'a, который в последующем предоставит нам
    // объект этого типа, если он подходит то вызывается resolveParameter() throws ParameterResolutionException и возвращаем объект,
    // если вернули false, то параметр не вернется
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == UserService.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(UserService.class)); // Namespace - ключ для store, Store - hashmap
        return store.getOrComputeIfAbsent(UserService.class, it -> new UserService());
        // получаем по ключу значение (key - UserService.class) (value - запуск механизма который вернет UserService)
    }
}
