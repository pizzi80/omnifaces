package org.omnifaces.cdi.ratelimit;

import org.omnifaces.cdi.RateLimit;

/**
 * Thrown when rate limit has exceeded according to {@link RateLimiter}.
 *
 * @author Bauke Scholtz
 * @since 5.0
 * @see RateLimit
 * @see RateLimiter
 * @see RateLimitExceededException
 */
public class RateLimitExceededException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_RATE_LIMIT_EXCEEDED = "Rate limit exceeded for client ID '%s'";

    private final long recommendedDelayInMillis;

    public RateLimitExceededException(String clientId, long recommendedDelayInMillis) {
        super(ERROR_RATE_LIMIT_EXCEEDED.formatted(clientId));
        this.recommendedDelayInMillis = recommendedDelayInMillis;
    }

    public long getRecommendedDelayInMillis() {
        return recommendedDelayInMillis;
    }
}
