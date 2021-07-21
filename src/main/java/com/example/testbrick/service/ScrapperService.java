package com.example.testbrick.service;

import com.example.testbrick.constant.StaticParams;
import com.example.testbrick.model.Product;
import lombok.AllArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class ScrapperService {

    private final ChromeDriver driver;
    private JavascriptExecutor jsExecutor;

    @Autowired
    ExportCVS exportCVS;

    @PostConstruct
    void postConstruct() {
        scrapper();
    }

    public void scrapper() {
        jsExecutor = driver;
        // WebDriverWait wait = new WebDriverWait(driver, 5);
        int count = 100;
        final List<Product> products = new ArrayList<>(count);

        try {
            List<String> tabs = prepareTwoTabs();
            String baseUrl = StaticParams._BASE_URL + StaticParams._HANDPHONE_PATH;

            for (int page = 1; products.size() < count; page++) {
                String url = baseUrl + StaticParams._PAGE + page;

                final List<WebElement> items = getElementListByScrollingDown(url,
                        StaticParams._XPATH_PRODUCT_LIST, tabs.get(0)); // switch to main tab

                for (WebElement item : items) {
                    String path = item.findElement(By.xpath(StaticParams._XPATH_PRODUCT_LINK))
                            .getAttribute(StaticParams._HREF);
                    if (isTopAdsLink(path)) {
                        path = extractTopAdsLink(path);
                    }

                    getWebpage(path, tabs.get(1)); //switch to new tab

                    // removing overlay on first access
                    if (products.isEmpty()) {
//                    waitOnElement(wait, _XPATH_FIRST_TIME_OVERLAY);
                        // removeElement(_DOM_FIRST_TIME_OVERLAY);
                        TimeUnit.SECONDS.sleep(3);
                    }
                    // trigger lazy load
                    scrollDownSmall();
                    TimeUnit.SECONDS.sleep(1);
//                waitOnElement(wait, _XPATH_MERCHANT_NAME);
//
                    products.add(extractProduct(path));

                    if (products.size() == count) {
                        break;
                    }
                    switchTab(tabs.get(0)); //switches to main tab
                }
            }

            exportCVS.setCVS(products);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

//        products.forEach(product -> {
//            System.out.println(product.getName());
//            System.out.println(product.getPrice());
//            System.out.println(product.getMerchant());
//            System.out.println(product.getRating());
//            System.out.println(product.getImageLink());
//            System.out.println(product.getDescription());
//        });
    }

    public List<WebElement> getElementListByScrollingDown(String url, String xpath, String tab) {
        switchTab(tab);
        driver.get(url);
        jsExecutor.executeScript(StaticParams._JS_SCROLL_MEDIUM);
        return driver.findElements(By.xpath(xpath));
    }

    public void switchTab(String tab) {
        driver.switchTo().window(tab);
    }

    private boolean isTopAdsLink(String path) {
        return path.contains(StaticParams._TOP_ADS_URL);
    }

    private String extractTopAdsLink(String path) throws IOException {
        return URLDecoder.decode(path.substring(path.indexOf(StaticParams._PARAM_R) + 2)
                        .split(StaticParams._AMP)[0], StandardCharsets.UTF_8.name());
    }

    public void getWebpage(String path, String tab) {
        switchTab(tab);
        driver.get(path);
    }

    private Product extractProduct(String path) {
        String name = getText(StaticParams._XPATH_PRODUCT_NAME);
        String desc = getText(StaticParams._XPATH_PRODUCT_DESCRIPTION);
        String imageLink = getText(StaticParams._XPATH_PRODUCT_IMG_LINK, StaticParams._SRC);
        String price = getText(StaticParams._XPATH_PRODUCT_PRICE)
                .split(StaticParams._RUPIAH_SIGN)[1].replace(StaticParams._DOT, StaticParams._EMPTY);
        String merchant = getText(StaticParams._XPATH_MERCHANT_NAME);
        String rating = getText(StaticParams._XPATH_PRODUCT_RATING);

        return Product.builder()
                .type("handphone")
                .name(name)
                .description(desc.replace(",", ""))
                .imageLink(imageLink)
                .merchant(merchant)
                .price(Double.parseDouble(price))
                .rating(rating == null || rating.isEmpty() ? 0.0 : Double.parseDouble(rating))
                .link(path)
                .build();
    }

    public List<String> prepareTwoTabs() {
        driver.get(StaticParams._GOOGLE_URL);
        jsExecutor.executeScript(StaticParams._JS_WINDOW_OPEN);
        return new ArrayList<> (driver.getWindowHandles());
    }

//    public void waitOnElement(WebDriverWait wait, String xpath) {
//        wait.until(ExpectedConditions.visibilityOfElementLocated(
//                By.xpath(xpath)));
//    }

//    public void removeElement(String element) {
//        jsExecutor.executeScript(String.format(StaticParams._JS_REMOVE_ELEMENT, element));
//    }

    public void scrollDownSmall() {
        jsExecutor.executeScript(StaticParams._JS_SCROLL_SMALL);
    }

    public String getText(String xpath) {
        return driver.findElements(By.xpath(xpath)).isEmpty()
                ? StaticParams._EMPTY : driver.findElement(By.xpath(xpath)).getText();
    }

    public String getText(String xpath, String attribute) {
        return driver.findElements(By.xpath(xpath)).isEmpty()
                ? StaticParams._EMPTY : driver.findElement(By.xpath(xpath)).getAttribute(attribute);
    }
}
