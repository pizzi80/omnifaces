package org.omnifaces.servlet;
import static java.lang.System.currentTimeMillis;
import static org.omnifaces.util.Servlets.getRemoteAddr;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>
 * CDI managed bean for rate limiting requests per client identifier. This utility provides a thread-safe
 * sliding window rate limiting mechanism with configurable request limits and time windows.
 * <p>
 * The rate limiter supports both HTTP request-based rate limiting (using client IP addresses) and custom
 * client identifier-based rate limiting (using any string identifier such as user IDs, API keys, etc.).
 * It uses a {@link ConcurrentHashMap} to track request counts per client identifier and automatically
 * cleans up expired entries to prevent memory leaks. Rate limit violations throw {@link RateLimitExceededException}.
 *
 * <h2>Usage</h2>
 * <p>
 * This CDI bean can be injected in any CDI managed artifact.
 * Below is an example of a servlet filter that applies rate limiting to all requests:
 *
 * <pre>
 * &#64;WebFilter("/*")
 * public class RateLimitFilter extends HttpFilter {
 *
 *     &#64;Inject
 *     private RateLimiter rateLimiter;
 *
 *     &#64;Override
 *     public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
 *         try {
 *             rateLimiter.checkRateLimit(request, 100, Duration.ofMinutes(1));
 *             chain.doFilter(request, response);
 *         }
 *         catch (RateLimiter.RateLimitExceededException e) {
 *             response.sendError(429); // Too Many Requests
 *         }
 *     }
 * }
 * </pre>
 * <p>
 * For more fine-grained control, you can use custom client identifiers instead of IP addresses:
 *
 * <pre>
 * &#64;Named
 * &#64;RequestScoped
 * public class ApiController {
 *
 *     &#64;Inject
 *     private RateLimiter rateLimiter;
 *
 *     public void processApiRequest(String apiKey) {
 *         try {
 *             rateLimiter.checkRateLimit(apiKey, 1000, Duration.ofHours(1));
 *             // Process API request ...
 *         }
 *         catch (RateLimiter.RateLimitExceededException e) {
 *             // Handle rate limit exceeded, e.g. block with sleep, or fire an async schedule with a retry attempt or even throw exception.
 *         }
 *     }
 * }
 * </pre>
 *
 * @author Bauke Scholtz
 * @since 5.0
 */
@ApplicationScoped
public class RateLimiter {

    // Variables ------------------------------------------------------------------------------------------------------

    private final Map<String, RequestCounter> requestCountsByClientId = new ConcurrentHashMap<>();

    // Actions --------------------------------------------------------------------------------------------------------

    /**
     * Checks if the current request exceeds the configured rate limit for the client IP address associated with the
     * given request.
     * <p>
     * This method implements a sliding window rate limiting algorithm. It tracks the number of requests
     * per IP address within the specified time window and throws an exception if the limit is exceeded.
     * Expired tracking entries are automatically cleaned up to prevent memory leaks.
     *
     * @param request The HTTP servlet request to check.
     * @param maxRequestsPerTimeWindow The maximum number of requests allowed within the time window.
     * @param timeWindow The time window duration.
     * @throws RateLimitExceededException When the rate limit is exceeded for the client IP address associated with
     * the given request.
     */
    public void checkRateLimit(HttpServletRequest request, int maxRequestsPerTimeWindow, Duration timeWindow) throws RateLimitExceededException {
        checkRateLimit(getRemoteAddr(request), maxRequestsPerTimeWindow, timeWindow);
    }

    /**
     * Checks if the current request exceeds the configured rate limit for the given client identifier.
     * <p>
     * This method implements a sliding window rate limiting algorithm. It tracks the number of requests
     * per IP address within the specified time window and throws an exception if the limit is exceeded.
     * Expired tracking entries are automatically cleaned up to prevent memory leaks.
     *
     * @param clientId The client identifier to check, whether client IP, user ID, API key, etc.
     * @param maxRequestsPerTimeWindow The maximum number of requests allowed within the time window.
     * @param timeWindow The time window duration.
     * @throws RateLimitExceededException When the rate limit is exceeded for the given client identifier.
     */
    public void checkRateLimit(String clientId, int maxRequestsPerTimeWindow, Duration timeWindow) throws RateLimitExceededException {
        var now = currentTimeMillis();
        var timeWindowInMillis = timeWindow.toMillis();
        requestCountsByClientId.entrySet().removeIf(entry -> now - entry.getValue().starttime >= timeWindowInMillis);
        var counter = requestCountsByClientId.compute(clientId, ($, rc) -> (rc == null) ? new RequestCounter() : rc.increment());

        if (counter.count > maxRequestsPerTimeWindow && now - counter.starttime < timeWindowInMillis) {
            throw new RateLimitExceededException("Rate limit exceeded for client ID: " + clientId);
        }
    }

    // Nested classes -------------------------------------------------------------------------------------------------

    /**
     * Convenience class for a request counter.
     */
    private static class RequestCounter {
        long starttime;
        int count;

        public RequestCounter() {
            this.starttime = currentTimeMillis();
            this.count = 1;
        }

        public RequestCounter increment() {
            count++;
            return this;
        }
    }

    /**
     * Thrown when rate limit has exceeded according to {@link RateLimiter}.
     */
    public static class RateLimitExceededException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
