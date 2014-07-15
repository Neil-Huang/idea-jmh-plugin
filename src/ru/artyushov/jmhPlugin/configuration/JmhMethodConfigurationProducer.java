package ru.artyushov.jmhPlugin.configuration;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import java.util.Iterator;

/**
 * User: nikart
 * Date: 14/07/14
 * Time: 23:06
 */
public class JmhMethodConfigurationProducer extends JmhConfigurationProducer {

    @Override
    protected boolean setupConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        PsiMethod method = getAnnotatedMethod(context);
        if (method == null) {
            return false;
        }
        sourceElement.set(method);
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);

        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return false;
        }
        configuration.setProgramParameters(containingClass.getQualifiedName() + "." + method.getName());
        configuration.setName(containingClass.getName() + "." + method.getName());
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context) {
        PsiMethod method = getAnnotatedMethod(context);
        if (method == null) {
            return false;
        }
        if (configuration.getName() == null || !configuration.getName().equals(getNameForConfiguration(method))) {
            return false;
        }
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);

        return true;
    }



    private PsiMethod getAnnotatedMethod(ConfigurationContext context) {
        Location<?> location = context.getLocation();
        if (location == null) {
            return null;
        }
        Iterator<Location<PsiMethod>> iterator = location.getAncestors(PsiMethod.class, false);
        Location<PsiMethod> methodLocation = null;
        if (iterator.hasNext()) {
            methodLocation = iterator.next();
        }
        if (methodLocation == null) {
            return null;
        }
        PsiMethod method = methodLocation.getPsiElement();
        if (ConfigurationUtils.hasBenchmarkAnnotation(method)) {
            return method;
        }
        return null;
    }

    private String getNameForConfiguration(PsiMethod method) {
        PsiClass clazz = method.getContainingClass();
        if (clazz == null) {
            return null;
        }
        return clazz.getQualifiedName() + "." + method.getName();
    }
}