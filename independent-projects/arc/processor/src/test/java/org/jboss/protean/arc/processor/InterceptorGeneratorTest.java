/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.protean.arc.processor;

import static org.jboss.protean.arc.processor.Basics.index;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.protean.arc.processor.types.Baz;
import org.junit.Test;

public class InterceptorGeneratorTest {

    @Test
    public void testGenerator() throws IOException {

        Index index = index(MyInterceptor.class, MyBinding.class, Baz.class);
        BeanDeployment deployment = new BeanDeployment(index, null, null);
        deployment.init();

        InterceptorInfo myInterceptor = deployment.getInterceptors().stream()
                .filter(i -> i.getTarget().get().asClass().name().equals(DotName.createSimple(MyInterceptor.class.getName()))).findAny().orElse(null);
        assertNotNull(myInterceptor);
        assertEquals(10, myInterceptor.getPriority());
        assertEquals(1, myInterceptor.getBindings().size());
        assertNotNull(myInterceptor.getAroundInvoke());

        InterceptorGenerator generator = new InterceptorGenerator(new AnnotationLiteralProcessor(BeanProcessor.DEFAULT_NAME, true), TruePredicate.INSTANCE);

        deployment.getInterceptors().forEach(interceptor -> generator.generate(interceptor, ReflectionRegistration.NOOP));
        // TODO test generated bytecode
    }

    @Priority(10)
    @MyBinding
    @Interceptor
    static class MyInterceptor {

        @Inject
        Baz baz;

        @AroundInvoke
        Object superCoolAroundInvokeMethod(InvocationContext ctx) throws Exception {
            return ctx.proceed();
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @InterceptorBinding
    public @interface MyBinding {

    }

}
