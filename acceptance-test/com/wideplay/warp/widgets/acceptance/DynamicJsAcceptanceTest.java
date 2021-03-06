package com.wideplay.warp.widgets.acceptance;

import com.wideplay.warp.widgets.acceptance.page.DynamicJsPage;
import com.wideplay.warp.widgets.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class DynamicJsAcceptanceTest {

    public void shouldRenderDynamicTextFromJsTemplate() {
        WebDriver driver = AcceptanceTest.createWebDriver();
        DynamicJsPage page = DynamicJsPage.open(driver);

        assert page.hasDynamicText() : "Did not generate dynamic text from warp-widget";
        assert page.hasNoMeta() : "@Meta widget annotation was not removed from the render";
    }
}