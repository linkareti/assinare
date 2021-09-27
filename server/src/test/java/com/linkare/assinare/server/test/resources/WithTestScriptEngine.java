package com.linkare.assinare.server.test.resources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkus.test.common.QuarkusTestResource;

/**
 *
 * @author bnazare
 */
@QuarkusTestResource(value = ScriptEngineTestResource.class, restrictToAnnotatedClass = true)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WithTestScriptEngine {

    enum Mode {
        STANDARD, BAD_COMMAND
    }

    Mode mode() default Mode.STANDARD;

}
