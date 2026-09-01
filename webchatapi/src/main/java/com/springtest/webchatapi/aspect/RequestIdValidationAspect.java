package com.springtest.webchatapi.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.springtest.webchatapi.exception.InvalidRequestException;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RequestIdValidationAspect {

    private static final String REQUEST_ID = "requestId";

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void validateRequestId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        if (!"GET".equals(request.getMethod()) && !"DELETE".equals(request.getMethod())) {
            return;
        }

        String requestId = request.getParameter(REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            throw new InvalidRequestException("Missing required request parameter: requestId");
        }

        try {
            Long.parseLong(requestId);
        } catch (NumberFormatException exception) {
            throw new InvalidRequestException("Request parameter requestId must be a long");
        }
    }
}
