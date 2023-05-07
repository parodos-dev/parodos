/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.workflow.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.redhat.parodos.workflow.parameter.WorkParameterType;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Parameter annotation
 *
 * @author Annel Ketcha (Github: anludke)
 */
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Parameter {

	String key();

	String description();

	WorkParameterType type();

	boolean optional();

	String[] selectOptions() default {};

	String valueProviderName() default "";

}
