/*
 * Copyright OmniFaces
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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
