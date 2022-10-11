package dk.digitalidentity.re.test;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import dk.digitalidentity.re.dao.PurchaseDao;
import dk.digitalidentity.re.dao.PurchaseVendorDao;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.PurchaseVendor;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:test.properties")
@ActiveProfiles({ "test" })
public class PurchaseTest {
	private static final long TIMEOUT = 2;
	private static final String url = "https://localhost:8099";
	private static final String testTitle = "Test Title";
	private static final String testDescription = "Test Description";
	private static final String testEmail = "test@email.com";
	private static ChromeDriver driver;
	
	@Autowired
	private PurchaseDao purchaseDao;
	
	@Autowired
	private PurchaseVendorDao purchaseVendorDao;

	@PostConstruct
	public void beforeClass() {
		driver = LoginService.getInstance().login();
	}

	@AfterClass
	public static void afterClass() {
		LoginService.getInstance().logout();
	}

	@Before
	public void before() {
		driver.get(url + "/purchase/list");
	}
	
	@After
	public void after() {
	}

	@Test
	public void verifyList() {
		driver.findElement(By.className("listTable"));
	}

	@Test
	public void createWithQuestionnaire() {
		driver.get(url + "/purchase/new");
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

		WebElement fieldTitle = driver.findElementById("title");
		fieldTitle.sendKeys(testTitle);
		
		WebElement fieldDesc = driver.findElementById("description");
		fieldDesc.sendKeys(testDescription);
		
		WebElement fieldEmail = driver.findElementById("email");
		fieldEmail.sendKeys(testEmail);

		WebElement domains = driver.findElementById("ms-my-select");
		List<WebElement> options = domains.findElements(By.tagName("li"));
		options.get(0).click();
		options.get(2).click();

		WebElement button = driver.findElement(By.cssSelector("button[type=\"submit\"]"));
		button.click();
		
		WebElement helpButton = driver.findElement(By.id("skipQuestionnaire")).findElement(By.cssSelector("a.btn-primary"));
		helpButton.click();
		
		WebElement answerForm = driver.findElement(By.id("answerForm"));
		WebElement btnFinish = answerForm.findElements(By.cssSelector("ul[role=menu] > li a")).get(2);
		
		for (int i = 1; i < 3; i++) {
			WebElement btnNext = answerForm.findElements(By.cssSelector("ul[role=menu] > li a")).get(1);
			btnNext.click();
		}
		
		wait.until(ExpectedConditions.visibilityOf(btnFinish));
		btnFinish.click();
		
		WebElement okButton = driver.findElement(By.id("okButton"));
		okButton.click();
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size() == 4 );

		fieldTitle = driver.findElementById("title");
		Assert.assertTrue(fieldTitle.getAttribute("value").equals(testTitle));
		
		fieldDesc = driver.findElementById("description");
		Assert.assertTrue(fieldDesc.getAttribute("value").equals(testDescription));
		
		fieldEmail = driver.findElementById("email");
		Assert.assertTrue(fieldEmail.getAttribute("value").equals(testEmail));
		
		List<Purchase> purchases = (List<Purchase>)purchaseDao.findAll();
		Optional<Purchase> optionalPurchase = purchases.stream().filter(p -> p.getTitle().equals(testTitle) && p.getDescription().equals(testDescription) && p.getEmail().equals(testEmail)).findAny();
		Assert.assertTrue(optionalPurchase.isPresent());
		
		purchaseDao.delete(optionalPurchase.get());
	}
	
	@Test
	public void createWithoutQuestionnaire() {
		driver.get(url + "/purchase/new");

		WebElement fieldTitle = driver.findElementById("title");
		fieldTitle.sendKeys(testTitle);
		
		WebElement fieldDesc = driver.findElementById("description");
		fieldDesc.sendKeys(testDescription);
		
		WebElement fieldEmail = driver.findElementById("email");
		fieldEmail.sendKeys(testEmail);

		WebElement domains = driver.findElementById("ms-my-select");
		List<WebElement> options = domains.findElements(By.tagName("li"));
		options.get(0).click();
		options.get(2).click();

		WebElement button = driver.findElement(By.cssSelector("button[type=\"submit\"]"));
		button.click();
		
		WebElement manualButton = driver.findElement(By.id("skipQuestionnaire")).findElement(By.cssSelector("button[type=submit]"));
		manualButton.click();
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size() == 4 );

		fieldTitle = driver.findElementById("title");
		Assert.assertTrue(fieldTitle.getAttribute("value").equals(testTitle));
		
		fieldDesc = driver.findElementById("description");
		Assert.assertTrue(fieldDesc.getAttribute("value").equals(testDescription));
		
		fieldEmail = driver.findElementById("email");
		Assert.assertTrue(fieldEmail.getAttribute("value").equals(testEmail));
		
		List<Purchase> purchases = (List<Purchase>)purchaseDao.findAll();
		Optional<Purchase> optionalPurchase = purchases.stream().filter(p -> p.getTitle().equals(testTitle) && p.getDescription().equals(testDescription) && p.getEmail().equals(testEmail)).findAny();
		Assert.assertTrue(optionalPurchase.isPresent());
		
		purchaseDao.delete(optionalPurchase.get());
	}
	
	@Test
	public void cancelPurchase() {
		driver.get(url + "/purchase/new");
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

		WebElement fieldTitle = driver.findElementById("title");
		fieldTitle.sendKeys(testTitle);
		
		WebElement fieldDesc = driver.findElementById("description");
		fieldDesc.sendKeys(testDescription);
		
		WebElement fieldEmail = driver.findElementById("email");
		fieldEmail.sendKeys(testEmail);

		WebElement domains = driver.findElementById("ms-my-select");
		List<WebElement> options = domains.findElements(By.tagName("li"));
		options.get(0).click();
		options.get(2).click();

		WebElement button = driver.findElement(By.cssSelector("button[type=\"submit\"]"));
		button.click();
		
		WebElement manualButton = driver.findElement(By.id("skipQuestionnaire")).findElement(By.cssSelector("button[type=submit]"));
		manualButton.click();
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size() == 4 );

		fieldTitle = driver.findElementById("title");
		Assert.assertTrue(fieldTitle.getAttribute("value").equals(testTitle));
		
		fieldDesc = driver.findElementById("description");
		Assert.assertTrue(fieldDesc.getAttribute("value").equals(testDescription));
		
		fieldEmail = driver.findElementById("email");
		Assert.assertTrue(fieldEmail.getAttribute("value").equals(testEmail));
		
		WebElement cancelButton = driver.findElementById("cancelProject");
		cancelButton.click();
		
		WebDriverUtil.confirmSweetAlert(driver, null);
		
		wait.until(ExpectedConditions.stalenessOf(listTable));
		
		listTable = driver.findElement(By.className("listTable"));
		
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().anyMatch(r -> r.findElements(By.tagName("td")).get(0).getText().equals(testTitle) && r.findElements(By.tagName("td")).get(1).getText().equals("Cancelled")));
		
		//clean up
		
		List<Purchase> purchases = (List<Purchase>)purchaseDao.findAll();
		Optional<Purchase> optionalPurchase = purchases.stream().filter(p -> p.getTitle().equals(testTitle) && p.getDescription().equals(testDescription) && p.getEmail().equals(testEmail)).findAny();
		Assert.assertTrue(optionalPurchase.isPresent());
		
		purchaseDao.delete(optionalPurchase.get());
	}
	
	@Test
	public void activatePurchase() {
		driver.get(url + "/purchase/new");
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		
		WebElement fieldTitle = driver.findElementById("title");
		fieldTitle.sendKeys(testTitle);
		
		WebElement fieldDesc = driver.findElementById("description");
		fieldDesc.sendKeys(testDescription);
		
		WebElement fieldEmail = driver.findElementById("email");
		fieldEmail.sendKeys(testEmail);

		WebElement domains = driver.findElementById("ms-my-select");
		List<WebElement> options = domains.findElements(By.tagName("li"));
		options.get(0).click();
		options.get(2).click();

		WebElement button = driver.findElement(By.cssSelector("button[type=\"submit\"]"));
		button.click();
		
		WebElement manualButton = driver.findElement(By.id("skipQuestionnaire")).findElement(By.cssSelector("button[type=submit]"));
		manualButton.click();
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size() == 4 );

		fieldTitle = driver.findElementById("title");
		Assert.assertTrue(fieldTitle.getAttribute("value").equals(testTitle));
		
		fieldDesc = driver.findElementById("description");
		Assert.assertTrue(fieldDesc.getAttribute("value").equals(testDescription));
		
		fieldEmail = driver.findElementById("email");
		Assert.assertTrue(fieldEmail.getAttribute("value").equals(testEmail));
		
		WebElement fieldEndTime = driver.findElementById("endTime");
		fieldEndTime.sendKeys("2028/08/10 - 23:00");
		
		WebElement activateButton = driver.findElementById("activateProject");
		activateButton.click();

		WebDriverUtil.confirmSweetAlert(driver, null);
		
		wait.until(ExpectedConditions.stalenessOf(listTable));
		
		listTable = driver.findElement(By.className("listTable"));
		
		WebElement purchaseForm = driver.findElementById("purchase-form");
		List<WebElement> fieldsets = purchaseForm.findElements(By.cssSelector("fieldset>div.form-group"));
		Optional<WebElement> optionalStatusField = fieldsets.stream().filter(div -> div.findElement(By.tagName("label")).getText().equals("Status")).findAny();
		Assert.assertTrue(optionalStatusField.isPresent());
		WebElement statusField = optionalStatusField.get();
		
		Assert.assertTrue(statusField.findElement(By.cssSelector("div > input")).getAttribute("value").equals("Active"));
		
		//clean up
		
		List<Purchase> purchases = (List<Purchase>)purchaseDao.findAll();
		Optional<Purchase> optionalPurchase = purchases.stream().filter(p -> p.getTitle().equals(testTitle) && p.getDescription().equals(testDescription) && p.getEmail().equals(testEmail)).findAny();
		Assert.assertTrue(optionalPurchase.isPresent());
		
		purchaseDao.delete(optionalPurchase.get());
	}
	
	@Test
	public void addAndDeleteCustomRequirement() {
		driver.get(url + "/purchase/new");
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		
		WebElement fieldTitle = driver.findElementById("title");
		fieldTitle.sendKeys(testTitle);
		
		WebElement fieldDesc = driver.findElementById("description");
		fieldDesc.sendKeys(testDescription);
		
		WebElement fieldEmail = driver.findElementById("email");
		fieldEmail.sendKeys(testEmail);

		WebElement domains = driver.findElementById("ms-my-select");
		List<WebElement> options = domains.findElements(By.tagName("li"));
		options.get(0).click();
		options.get(2).click();

		WebElement button = driver.findElement(By.cssSelector("button[type=\"submit\"]"));
		button.click();
		
		WebElement manualButton = driver.findElement(By.id("skipQuestionnaire")).findElement(By.cssSelector("button[type=submit]"));
		manualButton.click();
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size() == 4 );
		
		WebElement addCustomRequirementButton = driver.findElement(By.id("addCustomRequirementButton"));
		addCustomRequirementButton.click();
		
		WebElement modal = driver.findElement(By.id("addCustomRequirementModal"));
		wait.until(ExpectedConditions.visibilityOf(modal));
		Assert.assertTrue(modal.isDisplayed());

		WebElement crfName = modal.findElement(By.id("crfName"));
		crfName.sendKeys("CustomRequirementTest");
		
		WebElement crfDesc = modal.findElement(By.id("description"));
		crfDesc.sendKeys("DummyDescription");
		
		WebElement saveButton = modal.findElement(By.cssSelector("button[type=\"submit\"]"));
		saveButton.click();
		
		wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".listTable>tbody>tr"), 5));
		
		listTable = driver.findElement(By.className("listTable"));
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream()
				.anyMatch(r -> r.findElements(By.tagName("td")).get(1).getText().equals("CustomRequirementTest")
							&& r.findElements(By.tagName("td")).get(3).getText().equals("DummyDescription")));

		WebElement customRequirementRow = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream()
				.filter(r -> r.findElements(By.tagName("td")).get(1).getText().equals("CustomRequirementTest")
						&& r.findElements(By.tagName("td")).get(3).getText().equals("DummyDescription")).findAny().get();
		WebElement deleteButton = customRequirementRow.findElement(By.tagName("input"));
		
		deleteButton.click();
			
		//Clean up
		
		List<Purchase> purchases = (List<Purchase>)purchaseDao.findAll();
		Optional<Purchase> optionalPurchase = purchases.stream().filter(p -> p.getTitle().equals(testTitle) && p.getDescription().equals(testDescription) && p.getEmail().equals(testEmail)).findAny();
		Assert.assertTrue(optionalPurchase.isPresent());
		
		purchaseDao.delete(optionalPurchase.get());
	}
	
	@Test
	public void inviteVendor() {
		String companyName = "CompanyName";
		String vendorEmail = "email@email.com";
		
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		
		driver.get(url + "/purchase/new");

		WebElement fieldTitle = driver.findElementById("title");
		fieldTitle.sendKeys(testTitle);
		
		WebElement fieldDesc = driver.findElementById("description");
		fieldDesc.sendKeys(testDescription);
		
		WebElement fieldEmail = driver.findElementById("email");
		fieldEmail.sendKeys(testEmail);

		WebElement domains = driver.findElementById("ms-my-select");
		List<WebElement> options = domains.findElements(By.tagName("li"));
		options.get(0).click();
		options.get(2).click();

		WebElement button = driver.findElement(By.cssSelector("button[type=\"submit\"]"));
		button.click();
		
		WebElement manualButton = driver.findElement(By.id("skipQuestionnaire")).findElement(By.cssSelector("button[type=submit]"));
		manualButton.click();
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size() == 4 );

		fieldTitle = driver.findElementById("title");
		Assert.assertTrue(fieldTitle.getAttribute("value").equals(testTitle));
		
		fieldDesc = driver.findElementById("description");
		Assert.assertTrue(fieldDesc.getAttribute("value").equals(testDescription));
		
		fieldEmail = driver.findElementById("email");
		Assert.assertTrue(fieldEmail.getAttribute("value").equals(testEmail));
		
		WebElement fieldEndTime = driver.findElementById("endTime");
		fieldEndTime.sendKeys("2018/11/11 - 23:00");
		
		WebElement activateButton = driver.findElementById("activateProject");
		activateButton.click();

		WebDriverUtil.confirmSweetAlert(driver, null);
		
		wait.until(ExpectedConditions.stalenessOf(listTable));
		listTable = driver.findElement(By.className("listTable"));
		
		WebElement inviteVendorButton = driver.findElementById("inviteVendorModalButton");
		inviteVendorButton.click();
		
		WebElement modal = driver.findElement(By.id("inviteVendorModal"));
		wait.until(ExpectedConditions.visibilityOf(modal));
		Assert.assertTrue(modal.isDisplayed());

		WebElement name = modal.findElement(By.id("ivfName"));
		name.sendKeys(companyName);
		
		WebElement email = modal.findElement(By.id("email"));
		email.sendKeys(vendorEmail);
		
		WebElement message = modal.findElement(By.id("message"));
		message.sendKeys("DummyMessage");
		
		WebElement inviteButton = modal.findElement(By.id("inviteVendorButton"));
		inviteButton.click();
		
		wait.until(ExpectedConditions.stalenessOf(listTable));
		
		List<PurchaseVendor> vendorList = (List<PurchaseVendor>)purchaseVendorDao.findAll();
		vendorList.stream().filter(v -> v.getName().equals(companyName) && v.getEmail().equals(vendorEmail)).findAny();
		
		//clean up
		
		List<Purchase> purchases = (List<Purchase>)purchaseDao.findAll();
		Optional<Purchase> optionalPurchase = purchases.stream().filter(p -> p.getTitle().equals(testTitle) && p.getDescription().equals(testDescription) && p.getEmail().equals(testEmail)).findAny();
		Assert.assertTrue(optionalPurchase.isPresent());
		
		purchaseDao.delete(optionalPurchase.get());
	}
}