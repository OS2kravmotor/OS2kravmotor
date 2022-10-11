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

import dk.digitalidentity.re.dao.DomainDao;
import dk.digitalidentity.re.dao.model.Domain;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:test.properties")
@ActiveProfiles({ "test" })
public class DomainTest {
	private static final long TIMEOUT = 2;
	private static final String testName = "TestName";
	private static final String url = "https://localhost:8099";
	private static ChromeDriver driver;

	@Autowired
	private DomainDao domainDao;

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
		driver.get(url + "/domain/");
	}
	
	@After
	public void after() {
		removeTestData();
	}

	@Test
	public void verifyList() {
		driver.findElement(By.className("listTable"));
	}

	@Test
	public void create() {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		int before = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size();
		
		WebElement createButton = driver.findElement(By.id("addDomainButton"));
		createButton.click();
		
		WebElement modal = driver.findElement(By.id("addDomainModal"));
		wait.until(ExpectedConditions.visibilityOf(modal));
		Assert.assertTrue(modal.isDisplayed());
		
		WebElement fieldName = modal.findElement(By.id("name"));
		fieldName.sendKeys(testName);
		
		WebElement saveButton = modal.findElement(By.cssSelector("button[type=\"submit\"]"));
		saveButton.click();
		
		//Has been added
		listTable = driver.findElement(By.className("listTable"));
		int after = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size();
		Assert.assertTrue(after-1 == before);
		
		//Same object added
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().anyMatch(e -> e.findElement(By.tagName("td")).getText().equals(testName)));
	}

	private void addTestData() {
		Domain newDomain = new Domain();
		newDomain.setName(testName);
		domainDao.save(newDomain);
	}
	
	private void removeTestData() {
		List<Domain> domains = (List<Domain>) domainDao.findAll();
		Optional<Domain> toBeDelete = domains.stream().filter(d -> d.getName().startsWith(testName)).findAny();

		if (toBeDelete.isPresent()) {
			domainDao.delete(toBeDelete.get());
		}
	}

	@Test
	public void tryDelete() {
		addTestData();
		driver.navigate().refresh();
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		int before = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size();

		//Find element to remove
		Optional<WebElement> toBeRemoved = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().filter(e -> e.findElement(By.tagName("td")).getText().equals(testName)).findAny();
		Assert.assertTrue(toBeRemoved.isPresent());
		
		//Remove
		toBeRemoved.get().findElement(By.cssSelector("td:nth-child(2) > a > em.fa-remove")).click();
		
		//Click NO
		WebDriverUtil.cancelSweetAlert(driver);
		
		//Check size didn't change
		int after = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size();
		//Must be same size
		Assert.assertTrue(after == before);
		
		//Loop and find that this element is still on the list
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().anyMatch(e -> e.findElement(By.tagName("td")).getText().equals(testName)));
	}
	
	@Test
	public void delete() {
		addTestData();
		driver.navigate().refresh();
		
		WebElement listTable = driver.findElement(By.className("listTable"));
		int before = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size();

		//Find element to remove
		Optional<WebElement> toBeRemoved = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().filter(e -> e.findElement(By.tagName("td")).getText().equals(testName)).findAny();
		Assert.assertTrue(toBeRemoved.isPresent());
		
		//Remove
		toBeRemoved.get().findElement(By.cssSelector("td:nth-child(2) > a > em.fa-remove")).click();
		
		// Click YES
		WebDriverUtil.confirmSweetAlert(driver, listTable);
		
		listTable = driver.findElement(By.className("listTable"));
		//Check size changed
		int after = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size();
		Assert.assertTrue(after+1 == before);
		
		//Loop and find that this element is not on the list
		Assert.assertFalse(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().anyMatch(e -> e.findElement(By.tagName("td")).getText().equals(testName)));
	}
	
	@Test
	public void tryEdit() {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		addTestData();
		driver.navigate().refresh();
		
		WebElement listTable = driver.findElement(By.className("listTable"));

		//Find element to edit
		Optional<WebElement> toBeEdited = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().filter(e -> e.findElement(By.tagName("td")).getText().equals(testName)).findAny();
		Assert.assertTrue(toBeEdited.isPresent());
		
		//Edit
		toBeEdited.get().findElement(By.cssSelector("td:nth-child(2) > a > em.fa-pencil")).click();
		
		WebElement modal = driver.findElement(By.id("editDomainModal"));
		wait.until(ExpectedConditions.visibilityOf(modal));
		Assert.assertTrue(modal.isDisplayed());
		
		WebElement fieldName = modal.findElement(By.id("name"));
		fieldName.sendKeys("NotThis");
		
		WebElement button = modal.findElement(By.cssSelector("button[type=\"button\"]"));
		button.click();
		
		//Loop and find that this element is still on the list with the same text
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().anyMatch(e -> e.findElement(By.tagName("td")).getText().equals(testName)));
	}
	
	@Test
	public void edit() {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		addTestData();
		driver.navigate().refresh();
		
		WebElement listTable = driver.findElement(By.className("listTable"));

		//Find element to edit
		Optional<WebElement> toBeEdited = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().filter(e -> e.findElement(By.tagName("td")).getText().equals(testName)).findAny();
		Assert.assertTrue(toBeEdited.isPresent());
		
		//Edit
		toBeEdited.get().findElement(By.cssSelector("td:nth-child(2) > a > em.fa-pencil")).click();
		
		WebElement modal = driver.findElement(By.id("editDomainModal"));
		wait.until(ExpectedConditions.visibilityOf(modal));
		Assert.assertTrue(modal.isDisplayed());
		
		WebElement fieldName = modal.findElement(By.id("name"));
		fieldName.sendKeys("2");
		
		WebElement button = modal.findElement(By.cssSelector("button[type=\"submit\"]"));
		button.click();
		
		listTable = driver.findElement(By.className("listTable"));
		//Loop and find that this element is still on the list with the same text
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().anyMatch(e -> e.findElement(By.tagName("td")).getText().equals(testName+"2")));
	}
}
