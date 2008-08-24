package com.wideplay.warp.widgets.acceptance.page;

import com.wideplay.warp.widgets.acceptance.util.AcceptanceTest;
import org.openqa.selenium.How;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.ArrayList;
import java.util.List;

public class RepeatPage {
    @FindBy(how = How.XPATH, using = "//div[@class='entry'][1]/ul")
    private WebElement namesEntry;

    @FindBy(how = How.XPATH, using = "//div[@class='entry'][2]/ul")
    private WebElement moviesEntry;

    private WebDriver driver;

    public RepeatPage(WebDriver driver) {
        this.driver = driver;
    }

    public List<String> getRepeatedNames() {
        List<String> items = new ArrayList<String>();
        for (WebElement li : namesEntry.getChildrenOfType("li")) {
            items.add(li.getText());
        }

        return items;
    }

    public List<String> getRepeatedMovies() {
        List<String> items = new ArrayList<String>();
        for (WebElement li : moviesEntry.getChildrenOfType("li")) {
            items.add(li.getText());
        }

        return items;
    }

    public static RepeatPage open(WebDriver driver) {
        driver.get(AcceptanceTest.BASE_URL + "/repeat");
        return PageFactory.initElements(driver, RepeatPage.class);
    }
}
