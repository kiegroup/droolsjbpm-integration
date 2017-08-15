/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.controller.validate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotDuplicateValidStringsValidator implements ConstraintValidator<NotDuplicateValidStrings, List<String>> {

    private Set<String> possibleValues;

    @Override
    public void initialize(NotDuplicateValidStrings constraintAnnotation) {

        this.possibleValues = new HashSet<>(Arrays.asList(constraintAnnotation.admittedValues()));

    }

    @Override
    public boolean isValid(List<String> values, ConstraintValidatorContext context) {
        if ( values == null ) {
            return true;
        }

        if ( values.isEmpty() ) {
            return false;
        }

        HashSet<String> oldValues = new HashSet<>();

        for (String value : values) {

            // not null
            if (value == null) {
                return false;
            }

            // no duplicates
            if (oldValues.contains(value)) {
                return false;
            }

            // not valid
            if (!possibleValues.contains(value)) {
                return false;
            }

            oldValues.add(value);

        }

        return true;

    }

}
