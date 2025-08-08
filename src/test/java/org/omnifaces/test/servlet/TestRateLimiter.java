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
package org.omnifaces.test.servlet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.omnifaces.servlet.RateLimiter;
import org.omnifaces.servlet.RateLimiter.RateLimitExceededException;

class TestRateLimiter {

    private static final String FIRST_IP = "192.0.2.1";
    private static final String SECOND_IP = "192.0.2.2";

    private RateLimiter rateLimiter;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
        request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(FIRST_IP);
    }

    @Test
    void testRequestsWithinLimitShouldPass() {
        var timeWindow = Duration.ofSeconds(10);
        var maxRequests = 5;

        for (var i = 0; i < maxRequests; i++) {
            assertDoesNotThrow(() -> rateLimiter.checkRateLimit(request, maxRequests, timeWindow));
        }
    }

    @Test
    void testRequestsExceedingLimitShouldThrowException() {
        var timeWindow = Duration.ofSeconds(10);
        var maxRequests = 3;

        for (var i = 0; i < maxRequests; i++) {
            assertDoesNotThrow(() -> rateLimiter.checkRateLimit(request, maxRequests, timeWindow));
        }

        assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkRateLimit(request, maxRequests, timeWindow));
    }

    @Test
    void testDifferentIPAddressesAreTrackedSeparately() {
        var timeWindow = Duration.ofSeconds(10);
        var maxRequests = 2;

        var request1 = mock(HttpServletRequest.class);
        when(request1.getRemoteAddr()).thenReturn(FIRST_IP);

        var request2 = mock(HttpServletRequest.class);
        when(request2.getRemoteAddr()).thenReturn(SECOND_IP);

        for (var i = 0; i < maxRequests; i++) {
            assertDoesNotThrow(() -> rateLimiter.checkRateLimit(request1, maxRequests, timeWindow));
            assertDoesNotThrow(() -> rateLimiter.checkRateLimit(request2, maxRequests, timeWindow));
        }

        assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkRateLimit(request1, maxRequests, timeWindow));
        assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkRateLimit(request2, maxRequests, timeWindow));
    }

    @Test
    void testRateLimitResetAfterTimeWindow() throws InterruptedException {
        var timeWindow = Duration.ofMillis(100); // Very short window for testing
        var maxRequests = 2;

        for (var i = 0; i < maxRequests; i++) {
            assertDoesNotThrow(() -> rateLimiter.checkRateLimit(request, maxRequests, timeWindow));
        }

        assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkRateLimit(request, maxRequests, timeWindow));

        Thread.sleep(150); // Wait for the time window to pass

        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(request, maxRequests, timeWindow));
    }

    @Test
    void testAutomaticCleanupOfExpiredEntries() throws InterruptedException {
        var timeWindow = Duration.ofMillis(50);
        var maxRequests = 1;

        var request1 = mock(HttpServletRequest.class);
        when(request1.getRemoteAddr()).thenReturn(FIRST_IP);

        var request2 = mock(HttpServletRequest.class);
        when(request2.getRemoteAddr()).thenReturn(SECOND_IP);

        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(request1, maxRequests, timeWindow));
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(request2, maxRequests, timeWindow));

        Thread.sleep(100); // Wait for entries to expire

        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(request1, maxRequests, timeWindow));
    }
}