package br.com.caelum.vraptor.vraptor2;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.vraptor.annotations.Component;
import org.vraptor.annotations.Logic;

import br.com.caelum.vraptor.resource.DefaultResourceAndMethodLookup;
import br.com.caelum.vraptor.resource.DefaultResourceMethod;
import br.com.caelum.vraptor.resource.Resource;
import br.com.caelum.vraptor.resource.ResourceAndMethodLookup;
import br.com.caelum.vraptor.resource.ResourceMethod;

/**
 * A VRaptor 2 method lookup algorithm. Uses all public methods annotated with
 * Logic.
 * 
 * @author Guilherme Silveira
 */
public class VRaptor2MethodLookup implements ResourceAndMethodLookup {

    private final Resource resource;
    private final DefaultResourceAndMethodLookup delegate;

    public VRaptor2MethodLookup(Resource r) {
        this.delegate = new DefaultResourceAndMethodLookup(r);
        this.resource = r;
    }

    public ResourceMethod methodFor(String id, String methodName) {
        Class<?> type = resource.getType();
        if(!type.isAnnotationPresent(Component.class)) {
            return delegate.methodFor(id, methodName);
        }
        Component component = type.getAnnotation(Component.class);
        String componentName = component.value();
        if(componentName.equals("")) {
            componentName = type.getSimpleName();
        }
        for (Method method : type.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            Logic logic = method.getAnnotation(Logic.class);
            String logicName = (logic==null || logic.value()==null || logic.value().length==0) ? method.getName() : logic.value()[0];
            logicName = "/" + componentName + "." + logicName + ".logic";
            if (logicName.equals(id)) {
                return new DefaultResourceMethod(resource, method);
            }
        }
        return null;
    }
}
