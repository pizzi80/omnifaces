package org.omnifaces.test.cdi.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.omnifaces.test.cdi.ratelimit.RateLimitITService.TIME_WINDOW_IN_SECONDS;

import java.time.Duration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.omnifaces.test.OmniFacesIT;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RateLimitIT extends OmniFacesIT {

    @FindBy(id="form:submit")
    private WebElement submit;

    @FindBy(id="form:result")
    private WebElement result;

    @FindBy(id="form:messages")
    private WebElement messages;

    @Deployment(testable=false)
    public static WebArchive createDeployment() {
        return createWebArchive(RateLimitIT.class);
    }

    @Test
    void testRateLimit() {
        assertEquals("", result.getText());
        assertEquals("", messages.getText());

        guardAjax(submit::click);
        var firstResult = result.getText();
        assertNotEquals("", firstResult);
        assertEquals("", messages.getText());

        guardAjax(submit::click);
        assertEquals("", result.getText());
        assertEquals("Rate limit exceeded for client ID 'exampleApiRequest'", messages.getText());

        guardAjax(submit::click);
        assertEquals("", result.getText());
        assertEquals("Rate limit exceeded for client ID 'exampleApiRequest'", messages.getText());

        waitFor(Duration.ofSeconds(TIME_WINDOW_IN_SECONDS));

        guardAjax(submit::click);
        var secondResult = result.getText();
        assertNotEquals("", secondResult);
        assertNotEquals(firstResult, secondResult);
        assertEquals("", messages.getText());
    }
}
