package com.di.architecture;

import com.di.annotations.Configuration;
import com.di.model.events.Event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class EventBus {
    private final List<EventListenerInvoker> invokers = new ArrayList<>();
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public void register(Object bean, Method... eventListeners) {
        if (eventListeners.length > 0) {
            invokers.add(new EventListenerInvoker(bean, eventListeners));
        }
    }

    public void publish(Event event) {
        invokers.forEach(it -> executorService.submit(() -> it.invoke(event)));
    }

    private record EventListenerInvoker(Object bean, Method... eventListeners) {
        void invoke(Event event) {
            for (var method : eventListeners) {
                // Check if the event type matches the listener's first parameter type
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                    try {
                        method.invoke(bean, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
