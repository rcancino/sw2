package com.luxsoft.siipap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation para indicar que la propiedad de un bean debe aparecer en la
 * GUI de mantenimiento 
 * 
 * @author Ruben Cancino
 *
 */
@Retention (RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UIProperty {
	
	boolean readOnly() default false;
	
	String label() default "";
	
	boolean isPorcentage() default false;

}
