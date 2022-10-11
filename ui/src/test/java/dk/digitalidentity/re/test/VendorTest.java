package dk.digitalidentity.re.test;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import dk.digitalidentity.re.dao.PurchaseVendorDao;
import dk.digitalidentity.re.dao.model.PurchaseRequirement;
import dk.digitalidentity.re.dao.model.PurchaseVendor;
import dk.digitalidentity.re.dao.model.enums.AnswerChoice;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:test.properties")
@ActiveProfiles({ "test" })
@Transactional
public class VendorTest {
	private static final String selectedRequirementName = "Arkivering hos Statens Arkiver";
	private static final long TIMEOUT = 1000;
	private static final String testDetails = "This is a test description";
	private String url = "https://localhost:8099";
	private ChromeDriver driver;

	@Value("${chromedriver.bin}")
	private String chromeDriver;
	
	@Value("${vendor.username}")
	private String username;
	
	@Value("${vendor.password}")
	private String password;
		
	@Autowired
	private PurchaseVendorDao purchaseVendorDao;
		
	@PostConstruct
	public void beforeClass() {
		driver = VendorLoginService.getInstance().login();
	}

	@AfterClass
	public static void afterClass() {
		VendorLoginService.getInstance().logout();
	}

	@Before
	public void before() {
		driver.get(url + "/vendor");
	}

	@Test
	public void verifyList() {
		driver.findElement(By.className("listTable"));
	}

	@Test
	public void tryAnswerRequirement() {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		
		PurchaseVendor purchaseVendor = purchaseVendorDao.getByUsernameAndPassword(username, password);
		//Choose requirement to answer
		PurchaseRequirement selectedRequirement = purchaseVendor.getPurchase().getRequirements().stream().filter(r -> r.getName().equals(selectedRequirementName)).findAny().get();
		
		//Clean answers
		purchaseVendor.getDetails().clear();
		purchaseVendorDao.save(purchaseVendor);
		
		//Find Answer button
		Optional<WebElement> toBeAnswered = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().filter(e -> e.findElements(By.tagName("td")).get(0).getText().equals(selectedRequirement.getName())).findAny();
		Assert.assertTrue(toBeAnswered.isPresent());
		//Click Answer
		int columnsCount = toBeAnswered.get().findElements(By.tagName("td")).size();
		toBeAnswered.get().findElement(By.cssSelector("td:nth-child(" + columnsCount + ") > a > em.fa-pencil-square-o")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("answerForm")));
		
		//Fill out answer form
		WebElement answerForm = driver.findElement(By.id("answerForm"));
		
		WebElement answerChoiceInputElement = answerForm.findElement(By.id("adChoice"));
		Select answerChoice = new Select(answerChoiceInputElement);
		//answerChoiceInputElement.click();
		answerChoice.selectByValue("YES");
		
		WebElement answerDetail = answerForm.findElement(By.id("adDetail"));
		answerDetail.sendKeys(testDetails);
		
		//Cancel
		WebElement cancelAnswerButton = answerForm.findElement(By.cssSelector("button.btn-danger"));
		cancelAnswerButton.click();

		//Verify answer not added
		Assert.assertFalse(purchaseVendor.getDetails().stream().anyMatch(a -> a.getRequirement().equals(selectedRequirement) && a.getChoice().equals(AnswerChoice.YES) && a.getDetail().equals(testDetails)));
	}
	
	@Test
	public void answerRequirement() {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		
		PurchaseVendor purchaseVendor = purchaseVendorDao.getByUsernameAndPassword(username, password);
		//Choose requirement to answer
		PurchaseRequirement selectedRequirement = purchaseVendor.getPurchase().getRequirements().stream().filter(r -> r.getName().equals(selectedRequirementName)).findAny().get();
		//First assert it's not answered
		Assert.assertFalse(purchaseVendor.getDetails().stream().anyMatch(a -> a.getRequirement().equals(selectedRequirement) && a.getChoice().equals(AnswerChoice.YES) && a.getDetail().equals(testDetails)));
		//Find Answer button
		Optional<WebElement> toBeAnswered = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().filter(e -> e.findElements(By.tagName("td")).get(0).getText().equals(selectedRequirement.getName())).findAny();
		Assert.assertTrue(toBeAnswered.isPresent());
		//Click Answer
		int columnsCount = toBeAnswered.get().findElements(By.tagName("td")).size();
		toBeAnswered.get().findElement(By.cssSelector("td:nth-child(" + columnsCount + ") > a > em.fa-pencil-square-o")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("answerForm")));
		
		//Fill out answer form
		WebElement answerForm = driver.findElement(By.id("answerForm"));
		
		Select answerChoice = new Select(answerForm.findElement(By.id("adChoice")));
		answerChoice.selectByValue("YES");
		
		WebElement answerDetail = answerForm.findElement(By.id("adDetail"));
		answerDetail.sendKeys(testDetails);
		
		//Save answer
		WebElement saveAnswerButton = answerForm.findElement(By.id("saveAnswerButton"));
		saveAnswerButton.click();
		
		wait.until(ExpectedConditions.stalenessOf(listTable));
		
		listTable = driver.findElement(By.className("listTable"));
		
		boolean isAdded = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream()
				.anyMatch(e -> e.getText().contains(selectedRequirement.getName())
							&& e.getText().contains("Yes")
							&& e.getText().contains(testDetails));
		
		Assert.assertTrue(isAdded);
		//clean up
		purchaseVendor.getDetails().removeIf(d -> d.getRequirement().equals(selectedRequirement));
		purchaseVendorDao.save(purchaseVendor);
	}
	
	@Test
	public void modifyAnswer() {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		
		PurchaseVendor purchaseVendor = purchaseVendorDao.getByUsernameAndPassword(username, password);
		PurchaseRequirement selectedRequirement = purchaseVendor .getPurchase().getRequirements().stream().filter(r -> r.getName().equals(selectedRequirementName)).findAny().get();
		
		listTable = driver.findElement(By.className("listTable"));
		
		//PREPARE: Find Answer button
		Optional<WebElement> toBeAnswered = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().filter(e -> e.findElements(By.tagName("td")).get(0).getText().equals(selectedRequirement.getName())).findAny();
		Assert.assertTrue(toBeAnswered.isPresent());
		//PREPARE: Click Answer
		int columnsCount = toBeAnswered.get().findElements(By.tagName("td")).size();
		toBeAnswered.get().findElement(By.cssSelector("td:nth-child(" + columnsCount + ") > a > em.fa-pencil-square-o")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("answerForm")));
		
		//PREPARE: Fill out answer form FIRST with Test Data
		WebElement answerForm = driver.findElement(By.id("answerForm"));
		
		Select answerChoice = new Select(answerForm.findElement(By.id("adChoice")));
		answerChoice.selectByValue("NO");
		
		WebElement answerDetail = answerForm.findElement(By.id("adDetail"));
		answerDetail.clear();
		answerDetail.sendKeys("This text should be changed");

		//PREPARE: Save answer
		WebElement saveAnswerButton = answerForm.findElement(By.id("saveAnswerButton"));
		saveAnswerButton.click();
		
		wait.until(ExpectedConditions.stalenessOf(listTable));
		
		listTable = driver.findElement(By.className("listTable"));
		
		//MODIFY: Find Answer button
		toBeAnswered = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().filter(e -> e.findElements(By.tagName("td")).get(0).getText().equals(selectedRequirement.getName())).findAny();
		Assert.assertTrue(toBeAnswered.isPresent());
		//MODIFY: Click Answer
		columnsCount = toBeAnswered.get().findElements(By.tagName("td")).size();
		toBeAnswered.get().findElement(By.cssSelector("td:nth-child(" + columnsCount + ") > a > em.fa-pencil-square-o")).click();
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("answerForm")));
		
		//MODIFY: Fill out answer form SECOND time
		answerForm = driver.findElement(By.id("answerForm"));
		
		answerChoice = new Select(answerForm.findElement(By.id("adChoice")));
		answerChoice.selectByValue("YES");
		
		answerDetail = answerForm.findElement(By.id("adDetail"));
		answerDetail.clear();
		answerDetail.sendKeys(testDetails);

		//MODIFY: Save answer
		saveAnswerButton = answerForm.findElement(By.id("saveAnswerButton"));
		saveAnswerButton.click();
		
		wait.until(ExpectedConditions.stalenessOf(listTable));
		
		listTable = driver.findElement(By.className("listTable"));
		
		boolean isAdded = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream()
				.anyMatch(e -> e.getText().contains(selectedRequirement.getName())
							&& e.getText().contains("Yes")
							&& e.getText().contains(testDetails));
		Assert.assertTrue(isAdded);
		//clean up
		purchaseVendor.getDetails().removeIf(d -> d.getRequirement().equals(selectedRequirement));
		purchaseVendorDao.save(purchaseVendor);
	}
}
