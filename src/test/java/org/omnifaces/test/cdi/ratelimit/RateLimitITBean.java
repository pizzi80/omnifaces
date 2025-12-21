package org.omnifaces.test.cdi.ratelimit;

import static org.omnifaces.util.Messages.addGlobalError;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.omnifaces.cdi.ratelimit.RateLimitExceededException;

@Named
@RequestScoped
public class RateLimitITBean {

    private String exampleApiResponse;

    @Inject
    private RateLimitITService service;

    public void submit() {
        try {
            exampleApiResponse = service.exampleApiRequest();
        }
        catch (RateLimitExceededException e) {
            addGlobalError(e.getMessage());
        }
    }

    public String getExampleApiResponse() {
        return exampleApiResponse;
    }
}
