package de.hsesslingen.timesy.backend.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Paths;

public class DisplayService {

    public void capturePng(String path, String destPath) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.webkit().launch();
            Page page = browser.newPage();
            page.navigate(path);
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(destPath)).setFullPage(true));
        }
    }
}
