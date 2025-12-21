package org.omnifaces.test.cdi.ratelimit;

import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;

import org.omnifaces.cdi.RateLimit;
import org.omnifaces.cdi.ratelimit.RateLimitExceededException;

@ApplicationScoped
public class RateLimitITService {

    public static final long TIME_WINDOW_IN_SECONDS = 3;

    @RateLimit(clientId = "exampleApiRequest", timeWindowInSeconds = TIME_WINDOW_IN_SECONDS)
    public String exampleApiRequest() throws RateLimitExceededException {
        return "exampleApiResponse at " + LocalDateTime.now();
    }

}
