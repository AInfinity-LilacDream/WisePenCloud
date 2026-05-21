package com.oriole.wisepen.resource.validation;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.enums.ResourceOperationType;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 校验 {@code operation_type} 字符串是否为 {@link ResourceOperationType} 之一
 */
@Documented
@Constraint(validatedBy = ResourceOperationTypeCode.ResourceOperationTypeCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceOperationTypeCode {

    String message() default ResourceValidationMsg.RESOURCE_OPERATION_TYPE_INVALID;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // 根据 {@link ResourceOperationType#values()} 校验入参
    class ResourceOperationTypeCodeValidator implements ConstraintValidator<ResourceOperationTypeCode, String> {

        private static final Set<String> ALLOWED_CODES = Arrays.stream(ResourceOperationType.values())
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) {
                return true;
            }
            return ALLOWED_CODES.contains(value);
        }
    }
}
