package org.omnifaces.cdi.ratelimit;

import static jakarta.interceptor.Interceptor.Priority.LIBRARY_BEFORE;
import static org.omnifaces.util.Beans.getReference;

import java.time.Duration;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.RateLimit;
import org.omnifaces.cdi.ratelimit.RateLimiter.RateLimitExceededException;
import org.omnifaces.util.Beans;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Servlets;

/**
 * <p>
 * CDI interceptor that enforces rate limiting on methods annotated with {@link RateLimit}.
 * <p>
 * This interceptor intercepts method invocations and checks if the rate limit has been exceeded
 * for the specified client identifier. If the rate limit is exceeded, a {@link RateLimitExceededException}
 * is thrown. Otherwise, the method execution continues normally.
 * <p>
 * The interceptor automatically resolves the client identifier from the annotation configuration,
 * falling back to the client IP address from the current HTTP request if no explicit client ID is provided.
 *
 * @author Bauke Scholtz
 * @since 5.0
 * @see RateLimit
 * @see RateLimiter
 * @see RateLimitExceededException
 */
@Dependent
@RateLimit
@Interceptor
@Priority(LIBRARY_BEFORE)
public class RateLimitInterceptor {

    private static final String ERROR_CLIENT_ID_REQUIRED = "Please specify clientId on @RateLimit;"
            + " the fallback of HttpServletRequest isn't available in the current context.";

    @Inject
    private RateLimiter rateLimiter;

    @AroundInvoke
    public Object limitRate(InvocationContext context) throws Exception {
        var rateLimit = getRateLimitAnnotation(context);
        var clientId = getRateLimitClientId(rateLimit);
        var maxRequests = rateLimit.maxRequestsPerTimeWindow();
        var timeWindow = Duration.ofSeconds(rateLimit.timeWindowInSeconds());
        var maxRetries = rateLimit.maxRetries();
        rateLimiter.checkRateLimit(clientId, maxRequests, timeWindow, maxRetries);
        return context.proceed();
    }

    private static RateLimit getRateLimitAnnotation(InvocationContext context) {
        var rateLimit = context.getMethod().getAnnotation(RateLimit.class);

        if (rateLimit == null) {
            rateLimit = context.getTarget().getClass().getAnnotation(RateLimit.class);
        }

        return rateLimit;
    }

    private static String getRateLimitClientId(RateLimit rateLimit) {
        var clientId = rateLimit.clientId();

        if (clientId != null && !clientId.isBlank()) {
            return clientId;
        }

        if (Faces.hasContext()) {
            return Faces.getRemoteAddr();
        }

        if (Beans.isActive(RequestScoped.class)) {
            return Servlets.getRemoteAddr(getReference(HttpServletRequest.class));
        }

        throw new IllegalArgumentException(ERROR_CLIENT_ID_REQUIRED);
    }
}
