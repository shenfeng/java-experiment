package selenium2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class OnybooksTest {

	private WebDriver driver;

	@Before
	public void setUp() {
		driver = new FirefoxDriver();
//		Selenium.class
		// driver = new HtmlUnitDriver();
	}

	@Test
	public void testLandingPage() throws InterruptedException {
		driver.get("http://192.168.1.11:8080/");
		WebElement ele = driver.findElement(By.linkText("书库"));
		ele.click();
		WebElement search = driver.findElement(By.id("book-search"));

		search.click();
		// search.sendKeys("test");
		search.sendKeys("a", Keys.DOWN);
		search.sendKeys(Keys.ENTER);
		// TimeUnit.SECONDS.sleep(3);

		if (search instanceof RenderedWebElement) {
			RenderedWebElement re = (RenderedWebElement) search;
			System.out.println(re.isDisplayed());

			System.out.println("hello");
		}
		// search.submit();
		// boolean b = driver instanceof JavascriptExecutor;
		// JavascriptExecutor js = (JavascriptExecutor)driver;

		// js.
		// System.out.println(driver);
	}

	@After
	public void tearDown() {
		driver.quit();
	}
}
