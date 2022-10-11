package dk.digitalidentity.re.test;

import java.util.ArrayList;
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

import dk.digitalidentity.re.dao.CategoryDao;
import dk.digitalidentity.re.dao.DomainDao;
import dk.digitalidentity.re.dao.RequirementDao;
import dk.digitalidentity.re.dao.TagDao;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.enums.Importance;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:test.properties")
@ActiveProfiles({ "test" })
public class LocalRequirementTest {
	private static final long TIMEOUT = 2;
	private static final String testName = "TestName";
	private static final String url = "https://localhost:8099";
	private static ChromeDriver driver;

	@Autowired
	private RequirementDao requirementDao;

	@Autowired
	private CategoryDao categoryDao;
	
	@Autowired
	private TagDao tagDao;

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
		driver.get(url + "/requirement/list");
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
		WebElement createButton = driver.findElements(By.cssSelector(".content-wrapper > h3 > a")).get(0);
		createButton.click();

		WebElement fieldName = driver.findElementById("name");
		fieldName.sendKeys(testName);

		WebElement domains = driver.findElementById("ms-my-select");
		List<WebElement> options = domains.findElements(By.tagName("li"));
		options.get(0).click();
		options.get(2).click();

		WebElement tags = driver.findElementById("ms-my-select2");
		List<WebElement> tagOptions = tags.findElements(By.tagName("li"));
		tagOptions.get(1).click();

		WebElement button = driver.findElement(By.cssSelector("button[type=\"submit\"]"));
		button.click();

		// Find on a list
		WebElement listTable = driver.findElement(By.className("listTable"));
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().anyMatch(e -> e.findElement(By.tagName("td")).getText().equals(testName)));
	}

	private void addTestData() {
		Requirement requirement = new Requirement();
		requirement.setName(testName);
		requirement.setCvr("36074051");
		requirement.setImportance(Importance.HIGH);
		requirement.setCategory(categoryDao.findAll().get(0));
		requirement.setDescription("This is a description");
		requirement.setTags(new ArrayList<>());
		requirement.setDomains(new ArrayList<>());
		requirement.getTags().add(tagDao.findAll().get(0));
		requirement.getDomains().add(domainDao.getById(1));
		requirementDao.save(requirement);
	}

	private void removeTestData() {
		List<Requirement> requirements = (List<Requirement>) requirementDao.findAll();
		Optional<Requirement> toBeDelete = requirements.stream().filter(d -> d.getName().startsWith(testName)).findAny();

		if (toBeDelete.isPresent()) {
			requirementDao.delete(toBeDelete.get());
		}
	}

	@Test
	public void tryDelete() {
		addTestData();
		driver.navigate().refresh();

		WebElement listTable = driver.findElement(By.className("listTable"));
		int before = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size();

		// Find element to remove
		Optional<WebElement> toBeRemoved = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr"))
				.stream().filter(e -> e.findElement(By.tagName("td")).getText().equals(testName)).findAny();
		Assert.assertTrue(toBeRemoved.isPresent());

		// Remove
		toBeRemoved.get().findElement(By.cssSelector("td:nth-child(4) > a > em.fa-remove")).click();

		// Click NO
		WebDriverUtil.cancelSweetAlert(driver);

		// Check size didn't change
		int after = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size();
		// Must be same size
		Assert.assertTrue(after == before);

		// Loop and find that this element is still on the list
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().anyMatch(e -> e.findElement(By.tagName("td")).getText().equals(testName)));
	}

	@Test
	public void delete() {
		addTestData();
		driver.navigate().refresh();

		WebElement listTable = driver.findElement(By.className("listTable"));

		// Find element to remove
		Optional<WebElement> toBeRemoved = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr"))
				.stream().filter(e -> e.findElement(By.tagName("td")).getText().equals(testName)).findAny();
		Assert.assertTrue(toBeRemoved.isPresent());

		// Remove
		toBeRemoved.get().findElement(By.cssSelector("td:nth-child(4) > a > em.fa-remove")).click();

		// Click YES
		WebDriverUtil.confirmSweetAlert(driver, listTable);

		listTable = driver.findElement(By.className("listTable"));

		// Loop and find that this element is not on the list
		Assert.assertFalse(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream()
				.anyMatch(e -> e.findElement(By.tagName("td")).getText().equals(testName)));
	}

	@Test
	public void tryEdit() {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		addTestData();
		driver.navigate().refresh();

		WebElement listTable = driver.findElement(By.className("listTable"));

		// Find element to edit
		Optional<WebElement> toBeEdited = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr"))
				.stream().filter(e -> e.findElement(By.tagName("td")).getText().equals(testName)).findAny();
		Assert.assertTrue(toBeEdited.isPresent());

		// Edit
		toBeEdited.get().findElement(By.cssSelector("td:nth-child(4) > a > em.fa-pencil")).click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("requirement-form")));
		WebElement editForm = driver.findElement(By.id("requirement-form"));

		WebElement fieldName = editForm.findElement(By.id("name"));
		fieldName.sendKeys("NotThis");

		WebElement button = editForm.findElement(By.tagName("a"));
		button.click();

		// Loop and find that this element is still on the list with the same
		// text
		listTable = driver.findElement(By.className("listTable"));
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().anyMatch(e -> e.findElement(By.tagName("td")).getText().equals(testName)));
	}

	@Test
	public void edit() {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		addTestData();
		driver.navigate().refresh();

		WebElement listTable = driver.findElement(By.className("listTable"));

		// Find element to edit
		Optional<WebElement> toBeEdited = listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr"))
				.stream().filter(e -> e.findElement(By.tagName("td")).getText().equals(testName)).findAny();
		Assert.assertTrue(toBeEdited.isPresent());

		// Edit
		toBeEdited.get().findElement(By.cssSelector("td:nth-child(4) > a > em.fa-pencil")).click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("requirement-form")));
		WebElement editForm = driver.findElement(By.id("requirement-form"));

		WebElement fieldName = editForm.findElement(By.id("name"));
		fieldName.sendKeys("2");
		// TODO maybe more validation than just name. But name can be easily
		// checked on the list. Where domains and tags have to be checked on
		// view page.

		WebElement button = editForm.findElement(By.cssSelector("button[type=\"submit\"]"));
		button.click();

		listTable = driver.findElement(By.className("listTable"));
		// Loop and find that this element is still on the list with the edited
		// text
		Assert.assertTrue(listTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).stream().anyMatch(e -> e.findElement(By.tagName("td")).getText().equals(testName + "2")));
	}
}