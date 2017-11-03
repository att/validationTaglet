package com.att.det.taglet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.ExpressionFactory;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import javax.validation.spi.ValidationProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import com.att.det.taglet.CompositeClassLoader;
import com.sun.el.ExpressionFactoryImpl;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

public class ValidationConstraintsTaglet implements Taglet {
    private static Logger  logger  = LogManager.getLogger(ValidationConstraintsTaglet.class);
    
    private boolean combineConstraints  = false;
    
    public boolean isCombineConstraints() { return combineConstraints; }

    public void setCombineConstraints(boolean combineConstraints) { this.combineConstraints = combineConstraints; }
    public ValidationConstraintsTaglet combineConstraints(boolean combineConstraints) { this.combineConstraints = combineConstraints; return this; }

    @Override
    public String getName() { return "att.det.validationConstraints"; }

    @Override
    public boolean inConstructor() { return false; }

    @Override
    public boolean inField() { return false; }

    @Override
    public boolean inMethod() { return false; }

    @Override
    public boolean inOverview() { return true; }

    @Override
    public boolean inPackage() { return false; }

    @Override
    public boolean inType() { return true; }

    @Override
    public boolean isInlineTag() { return false; }

    @Override
    public String toString(Tag tag) {
        String  className   = tag.holder().toString();
        
        StringBuilder   sb  = new StringBuilder();
        sb.append("<dt><b>Validation Constraints</b></dt>");
        sb.append("<dd>");
        sb.append("<table border=1><thead><tr><th>Property</th><th>Message</th></tr></thead>");
        sb.append("<tbody>");
        
        try {
            logger.info("className[" + className + "]");
            Class<?>    clazz   = Class.forName(className);
            logger.info("clazz[" + clazz. getName() + "]");
            
            ValidatorFactory    validatorFactory    =
                    Validation.
                    byProvider(HibernateValidator.class).
                    providerResolver(new HibernateValidatorProviderResolver()).
                    configure().
                    messageInterpolator(new ResourceBundleMessageInterpolator(
                            new PlatformResourceBundleLocator(ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES),
                            true, buildExpressionFactory())).
                    buildValidatorFactory();
            
            Validator   validator   = validatorFactory.getValidator();
            
            processBeanDescriptor(sb, validatorFactory, validator.getConstraintsForClass(clazz));
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
        sb.append("</tbody></table>");
        sb.append("</dd>");
        return sb.toString();
    }

    private ExpressionFactory buildExpressionFactory() {
        ClassLoader oldTccl = Thread.currentThread().getContextClassLoader();
        CompositeClassLoader    ccl = new CompositeClassLoader();
        ccl.add(ExpressionFactoryImpl.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(ccl);
        try {
            return ExpressionFactory.newInstance();
        }
        finally {
            Thread.currentThread().setContextClassLoader( oldTccl );
        }
    }

    private void processBeanDescriptor(StringBuilder sb, ValidatorFactory validatorFactory, BeanDescriptor beanDescriptor) {
        if (beanDescriptor.hasConstraints()) {
            for (ConstraintDescriptor<?> constraintDescriptor : beanDescriptor.getConstraintDescriptors()) {
                emitTableRow(sb, validatorFactory, constraintDescriptor);
            }
        }
        for (PropertyDescriptor propertyDescriptor : beanDescriptor.getConstrainedProperties()) {
            if (!propertyDescriptor.hasConstraints() && propertyDescriptor.isCascaded()) {
                emitTableRow(sb, propertyDescriptor, validatorFactory);
            }
            else {
                if (isCombineConstraints()) {
                    emitTableRow(sb, propertyDescriptor, validatorFactory, propertyDescriptor.getConstraintDescriptors());
                }
                else {
                    for (ConstraintDescriptor<?> constraintDescriptor : propertyDescriptor.getConstraintDescriptors()) {
                        emitTableRow(sb, propertyDescriptor, validatorFactory, constraintDescriptor);
                    }
                    if (propertyDescriptor.isCascaded()) {
                        emitTableRow(sb, propertyDescriptor, validatorFactory);
                    }
                }
            }
        }
    }
    
    private void emitTableRow(StringBuilder sb, PropertyDescriptor propertyDescriptor, ValidatorFactory validatorFactory) {
        emitTableRow(sb, propertyDescriptor, validatorFactory, (ConstraintDescriptor<?>) null);
    }

    private void emitTableRow(StringBuilder sb, ValidatorFactory validatorFactory, ConstraintDescriptor<?> constraintDescriptor) {
        emitTableRow(sb, null, validatorFactory, constraintDescriptor);
    }

    private void emitTableRow(StringBuilder                 sb,
                              PropertyDescriptor            propertyDescriptor,
                              ValidatorFactory              validatorFactory,
                              Set<ConstraintDescriptor<?>>  constraintDescriptors) {
        
        sb.append("<tr><td>" + (propertyDescriptor != null ? propertyDescriptor.getPropertyName() : "") + "</td><td>");
        StringBuilder   msb = new StringBuilder();
        for (ConstraintDescriptor<?> constraintDescriptor : constraintDescriptors) {
            if (msb.length() > 0) {
                msb.append(", ");
            }
            msb.append(validatorFactory.getMessageInterpolator().interpolate(constraintDescriptor.getMessageTemplate(), new Context(constraintDescriptor)));
        }
        if ((propertyDescriptor != null) && propertyDescriptor.isCascaded()) {
            if (msb.length() > 0) {
                msb.append(", ");
            }
            msb.append("See class " + propertyDescriptor.getElementClass().getName());
        }
        sb.append(msb.toString());
        sb.append("</td></tr>");
    }

    private void emitTableRow(StringBuilder             sb,
                              PropertyDescriptor        propertyDescriptor,
                              ValidatorFactory          validatorFactory,
                              ConstraintDescriptor<?>   constraintDescriptor) {
        
        sb.append("<tr><td>" + (propertyDescriptor != null ? propertyDescriptor.getPropertyName() : "") + "</td><td>");
        StringBuilder   msb = new StringBuilder();
        if (constraintDescriptor != null) {
            msb.append(validatorFactory.getMessageInterpolator().interpolate(constraintDescriptor.getMessageTemplate(), new Context(constraintDescriptor)));
        }
        else if ((propertyDescriptor != null) && propertyDescriptor.isCascaded()) {
            if (msb.length() > 0) {
                msb.append(", ");
            }
            msb.append("See class " + propertyDescriptor.getElementClass().getName());
        }
        sb.append(msb.toString());
        sb.append("</td></tr>");
    }

    @Override
    public String toString(Tag[] tags) {
        StringBuilder   sb  = new StringBuilder();
        for (Tag tag : tags) {
            sb.append(toString(tag));
        }
        return sb.toString();
    }

    public static void register(Map<String, Taglet> tagletMap)  {
        ValidationConstraintsTaglet taglet  = new ValidationConstraintsTaglet();
        registerInstance(tagletMap, taglet, taglet.getName());
    }
    
    protected static void registerInstance(Map<String, Taglet> tagletMap, Taglet taglet, String name) {
        if (tagletMap.get(name) != null) {
            tagletMap.remove(name);
        }
        tagletMap.put(name, taglet);
    }
    
    public static class HibernateValidatorProviderResolver implements ValidationProviderResolver {
        @Override
        public List<ValidationProvider<?>> getValidationProviders() { return Collections.singletonList(new HibernateValidator()); }
    }
    
    private static class Context implements MessageInterpolator.Context {
        private ConstraintDescriptor<?> constraintDescriptor;
        public Context(ConstraintDescriptor<?> constraintDescriptor) {
            this.constraintDescriptor   = constraintDescriptor;
        }
        @Override public <T> T unwrap(Class<T> type) { return null; }
        @Override public Object getValidatedValue() { return null; }
        @Override public ConstraintDescriptor<?> getConstraintDescriptor() { return constraintDescriptor; }
    }
}
