package br.com.caelum.vraptor.vraptor2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import javax.servlet.ServletException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.core.MethodInfo;
import br.com.caelum.vraptor.http.ParametersProvider;
import br.com.caelum.vraptor.interceptor.DogAlike;
import br.com.caelum.vraptor.resource.DefaultResource;
import br.com.caelum.vraptor.resource.DefaultResourceMethod;
import br.com.caelum.vraptor.resource.ResourceMethod;

public class ExecuteAndViewInterceptorTest {

    private Mockery mockery;
    private ParametersProvider provider;
    private MethodInfo info;
    private InterceptorStack stack;
    private MethodInfo parameters;

    @Before
    public void setup() throws NoSuchMethodException {
        this.mockery = new Mockery();
        this.provider = mockery.mock(ParametersProvider.class);
        this.info = mockery.mock(MethodInfo.class);
        this.stack = mockery.mock(InterceptorStack.class);
        this.parameters = mockery.mock(MethodInfo.class);
    }

    @Test
    public void shouldInvokeTheMethodAndNotProceedWithInterceptorStack() throws SecurityException,
            NoSuchMethodException, IOException, InterceptionException {
        ExecuteAndViewInterceptor interceptor = new ExecuteAndViewInterceptor(info, parameters);
        final ResourceMethod method = new DefaultResourceMethod(new DefaultResource(DogAlike.class), DogAlike.class.getMethod("bark"));
        final DogAlike auau = mockery.mock(DogAlike.class);
        mockery.checking(new Expectations() {
            {
                one(auau).bark();
                one(stack).next(method, auau);
                one(parameters).getParameters(); will(returnValue(new Object[]{}));
            }
        });
        interceptor.intercept(stack, method, auau);
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldThrowMethodExceptionIfThereIsAnInvocationException() throws IOException, SecurityException,
            NoSuchMethodException {
        ExecuteAndViewInterceptor interceptor = new ExecuteAndViewInterceptor(info, parameters);
        ResourceMethod method = new DefaultResourceMethod(new DefaultResource(DogAlike.class), DogAlike.class.getMethod("bark"));
        final DogAlike auau = mockery.mock(DogAlike.class);
        final RuntimeException exception = new RuntimeException();
        mockery.checking(new Expectations() {
            {
                one(auau).bark();
                will(throwException(exception));
                one(parameters).getParameters(); will(returnValue(new Object[]{}));
            }
        });
        try {
            interceptor.intercept(stack, method, auau);
            Assert.fail();
        } catch (InterceptionException e) {
            MatcherAssert.assertThat((RuntimeException) e.getCause(), Matchers.is(Matchers.equalTo(exception)));
            mockery.assertIsSatisfied();
        }
    }
    
    @Test
    public void shouldUseTheProvidedArguments() throws SecurityException, NoSuchMethodException, InterceptionException, IOException {
        ExecuteAndViewInterceptor interceptor = new ExecuteAndViewInterceptor(info, parameters);
        final ResourceMethod method = new DefaultResourceMethod(new DefaultResource(DogAlike.class), DogAlike.class.getMethod("bark", int.class));
        final DogAlike auau = mockery.mock(DogAlike.class);
        mockery.checking(new Expectations() {
            {
                one(auau).bark(3);
                one(stack).next(method, auau);
                one(parameters).getParameters(); will(returnValue(new Object[]{3}));
            }
        });
        interceptor.intercept(stack, method, auau);
        mockery.assertIsSatisfied();
    }
    
    @org.vraptor.annotations.Component
    interface OldDog {
        public void bark();
        public String barkResponse();
    }

    @Test
    public void shouldForwardIfUsingAnOldComponent() throws SecurityException, NoSuchMethodException, InterceptionException, IOException, ServletException {
        ExecuteAndViewInterceptor interceptor = new ExecuteAndViewInterceptor(info, parameters);
        final ResourceMethod method = new DefaultResourceMethod(new DefaultResource(OldDog.class), OldDog.class.getMethod("bark"));
        final OldDog auau = mockery.mock(OldDog.class);
        mockery.checking(new Expectations() {
            {
                one(auau).bark();
                one(stack).next(method, auau);
                one(parameters).getParameters(); will(returnValue(new Object[]{}));
            }
        });
        interceptor.intercept(stack, method, auau);
        assertThat((String)info.getResult(), is(equalTo("ok")));
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldForwardWithResultIfUsingAnOldComponent() throws SecurityException, NoSuchMethodException, InterceptionException, IOException, ServletException {
        ExecuteAndViewInterceptor interceptor = new ExecuteAndViewInterceptor(info, parameters);
        final ResourceMethod method = new DefaultResourceMethod(new DefaultResource(OldDog.class), OldDog.class.getMethod("barkResponse"));
        final OldDog auau = mockery.mock(OldDog.class);
        mockery.checking(new Expectations() {
            {
                one(auau).barkResponse(); will(returnValue("response"));
                one(stack).next(method, auau);
                one(parameters).getParameters(); will(returnValue(new Object[]{}));
            }
        });
        interceptor.intercept(stack, method, auau);
        assertThat((String)info.getResult(), is(equalTo("response")));
        mockery.assertIsSatisfied();
    }

}
