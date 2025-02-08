package org.omnifaces.test.cdi.viewscoped;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.omnifaces.test.Concurrency.testThreadSafety;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import org.junit.jupiter.api.Test;
import org.omnifaces.cdi.BeanStorage;

public class TestBeanStorage {

    @Test
    void testgetBeanThreadSafety() {
        var beanStorage = new BeanStorage(10);
        var outerBeanCounter = new AtomicInteger();
        var innerBeanCounter = new AtomicInteger();
        var outerBean = new Contextual<Object>() {

            Contextual<Object> innerBean = new Contextual<Object>() {
                @Override
                public Object create(CreationalContext<Object> context) {
                    innerBeanCounter.incrementAndGet();

                    return new Serializable() {
                        private static final long serialVersionUID = 1L;
                    };
                }

                @Override
                public void destroy(Object instance, CreationalContext<Object> context) {}
            };

            @Override
            public Object create(CreationalContext<Object> context) {
                outerBeanCounter.incrementAndGet();
                beanStorage.getBean(innerBean, context);

                return new Serializable() {
                    private static final long serialVersionUID = 1L;
                };
            }

            @Override
            public void destroy(Object instance, CreationalContext<Object> context) {}
        };

        var context = new CreationalContext<Object>() {
            @Override
            public void push(Object incompleteInstance) {}

            @Override
            public void release() {}
        };

        testThreadSafety(i -> beanStorage.getBean(outerBean, context));

        assertAll(
            () -> assertEquals(1, outerBeanCounter.get()),
            () -> assertEquals(1, innerBeanCounter.get())
        );
    }
}
