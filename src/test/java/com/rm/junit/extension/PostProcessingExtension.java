package com.rm.junit.extension;

import lombok.Getter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import service.UserService;

import java.lang.reflect.Field;

public class PostProcessingExtension implements TestInstancePostProcessor {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        System.out.println("post processing extension");
        Field[] declaredFields = testInstance.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Getter.class)) {
                declaredField.set(testInstance, new UserService());
            }
        }
    }
}
