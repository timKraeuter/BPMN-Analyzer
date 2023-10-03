package no.tk.rulegenerator.server.endpoint.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// RequiresConversion is a custom annotation solely used in this example
// to annotate an attribute as "convertable"
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresConversion {}
