/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.runtime.manager.service;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.talend.sdk.component.runtime.manager.test.Serializer.roundTrip;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.xbean.propertyeditor.PropertyEditorRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.cache.LocalCache;
import org.talend.sdk.component.api.service.configuration.Configuration;
import org.talend.sdk.component.api.service.configuration.LocalConfiguration;
import org.talend.sdk.component.api.service.injector.Injector;
import org.talend.sdk.component.runtime.manager.reflect.ParameterModelService;
import org.talend.sdk.component.runtime.manager.reflect.ReflectionService;
import org.talend.sdk.component.runtime.manager.serialization.DynamicContainerFinder;

import lombok.Data;

class InjectorImplTest {

    private Injector injector;

    @BeforeEach
    void init() {
        final Map<Class<?>, Object> services = new HashMap<>(2);
        services.put(LocalCache.class, new LocalCacheService("LocalCacheServiceTest"));
        services
                .put(LocalConfiguration.class,
                        new LocalConfigurationService(Collections.singletonList(new LocalConfiguration() {

                            @Override
                            public String get(final String key) {
                                return "test.foo.name".equals(key) ? "ok" : "ko";
                            }

                            @Override
                            public Set<String> keys() {
                                return singleton("foo.name");
                            }
                        }), "test"));
        final PropertyEditorRegistry propertyEditorRegistry = new PropertyEditorRegistry();
        injector = new InjectorImpl("LocalCacheServiceTest",
                new ReflectionService(new ParameterModelService(propertyEditorRegistry), propertyEditorRegistry),
                services);
        DynamicContainerFinder.LOADERS.put("LocalCacheServiceTest", Thread.currentThread().getContextClassLoader());
        DynamicContainerFinder.SERVICES.put(Injector.class, injector);
    }

    @AfterEach
    void destroy() {
        DynamicContainerFinder.LOADERS.remove("LocalCacheServiceTest");
        DynamicContainerFinder.SERVICES.remove(Injector.class);
    }

    @Test
    void serialize() throws IOException, ClassNotFoundException {
        assertNotNull(roundTrip(injector));
    }

    @Test
    void inject() {
        final Injected instance = new Injected();
        injector.inject(instance);
        assertNotNull(instance.cache);
    }

    @Test
    void invalidConfigurationInjectionSupplier() {
        assertThrows(IllegalArgumentException.class, () -> injector.inject(new InvalidInjectedConfig1()));
    }

    @Test
    void invalidConfigurationInjectionDirectConfig() {
        assertThrows(IllegalArgumentException.class, () -> injector.inject(new InvalidInjectedConfig2()));
    }

    @Test
    void configurationInjection() {
        final Supplier<MyConfig> config = injector.inject(new InjectedConfig()).config;
        assertNotNull(config);
        final MyConfig configuration = config.get();
        assertEquals("ok", configuration.getValue());
    }

    public static class Injected {

        @Service
        private LocalCache cache;
    }

    public static class InvalidInjectedConfig1 {

        @Configuration("foo")
        private Supplier config;
    }

    public static class InvalidInjectedConfig2 {

        @Configuration("foo")
        private MyConfig config;
    }

    public static class InjectedConfig {

        @Configuration("foo")
        private Supplier<MyConfig> config;
    }

    @Data
    public static class MyConfig {

        @Option("name")
        private String value;
    }
}
