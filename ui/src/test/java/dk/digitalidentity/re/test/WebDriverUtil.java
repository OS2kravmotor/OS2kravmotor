package dk.digitalidentity.re.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebDriverUtil {
	private static final long TIMEOUT = 2;

	public static void confirmSweetAlert(ChromeDriver driver, WebElement oldItem) {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

		wait.until(ExpectedConditions.presenceOfElementLocated(By.className("sweet-alert")));
		wait.until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				WebElement swal = driver.findElement(By.className("sweet-alert"));
				boolean visible = swal.getAttribute("class").contains("visible");
				return visible;
			};
		});
		
		driver.findElement(By.className("sweet-alert")).findElement(By.className("sa-button-container")).findElement(By.className("confirm")).click();

		if (oldItem != null) {
			wait.until(ExpectedConditions.stalenessOf(oldItem));
		}
		else { // default to wait 1 second
			try {
				wait.wait(1000);
			}
			catch (Exception ex) {
				; // ignore
			}
		}
	}
	
	public static void cancelSweetAlert(ChromeDriver driver) {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

		wait.until(ExpectedConditions.presenceOfElementLocated(By.className("sweet-alert")));
		wait.until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				WebElement swal = driver.findElement(By.className("sweet-alert"));

				return swal.getAttribute("class").contains("visible");
			};
		});
		
		driver.findElement(By.className("sweet-alert")).findElement(By.className("sa-button-container")).findElement(By.className("cancel")).click();
		
		wait.until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				WebElement swal = driver.findElement(By.className("sweet-alert"));

				return (swal == null || !swal.getAttribute("class").contains("visible"));
			};
		});
	}
}
