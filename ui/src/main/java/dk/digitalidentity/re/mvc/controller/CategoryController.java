package dk.digitalidentity.re.mvc.controller;

import java.util.List;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import dk.digitalidentity.re.dao.PurchaseRequirementDao;
import dk.digitalidentity.re.dao.model.Category;
import dk.digitalidentity.re.dao.model.Subcategory;
import dk.digitalidentity.re.mvc.dto.DeleteStatus;
import dk.digitalidentity.re.mvc.form.CategoryForm;
import dk.digitalidentity.re.mvc.validator.CategoryFormValidator;
import dk.digitalidentity.re.security.RequireEditorRole;
import dk.digitalidentity.re.security.RequireGlobalEditorRole;
import dk.digitalidentity.re.service.CategoryService;
import dk.digitalidentity.re.service.RequirementService;

@Controller
@RequireEditorRole
public class CategoryController {
	private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
	
	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private RequirementService requirementService;

	@Autowired
	private PurchaseRequirementDao purchaseRequirementDao;

	@Autowired
	private CategoryFormValidator categoryFormValidator;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(categoryFormValidator);
	}

	@RequestMapping(path = { "category/", "category/list" }, method = RequestMethod.GET)
	public String listCategory(Model model) {
		model.addAttribute("categories", categoryService.findAll());
		model.addAttribute("category", new CategoryForm());
		model.addAttribute("categoryEdit", new CategoryForm());
		model.addAttribute("subcategoryEdit", new CategoryForm());

		return "category/list";
	}

	@RequireGlobalEditorRole
	@RequestMapping(path = { "category/" }, method = RequestMethod.POST)
	public String newCategory(Model model, @ModelAttribute("category") @Valid CategoryForm categoryForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("category", categoryForm);
			model.addAttribute("categoryEdit", new CategoryForm());
			model.addAttribute("categories", categoryService.findAll());

			return "category/list";
		}

		Category newCategory = modelMapper.map(categoryForm, Category.class);
		categoryService.save(newCategory);

		model.addAttribute("categories", categoryService.findAll());

		return "redirect:/category/";
	}

	@RequireGlobalEditorRole
	@RequestMapping(path = "category/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<String> deleteCategory(@PathVariable("id") long id) {
		Category category = categoryService.getById(id);

		if (category == null) {
			log.warn("Requested Category with ID:"+id+ " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		categoryService.delete(category);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequireGlobalEditorRole
	@RequestMapping(path = { "category/edit/" }, method = RequestMethod.POST)
	public String editCategory(Model model, @ModelAttribute("categoryEdit") @Valid CategoryForm categoryForm, BindingResult bindingResult) {
		Category category = categoryService.getById(categoryForm.getId());
		if (category == null) {
			log.warn("Requested Category with ID:" + categoryForm.getId() + " not found.");
	                return "redirect:/category/";
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("state", "edit");
			model.addAttribute("category", new CategoryForm());
			model.addAttribute("categoryEdit", categoryForm);
			model.addAttribute("categories", modelMapper.map(categoryService.findAll(), new TypeToken<List<CategoryForm>>() {}.getType()));

			return "category/list";
		}

		category.setName(categoryForm.getName());
		categoryService.save(category);

		return "redirect:/category/";
	}

	@RequireGlobalEditorRole
	@RequestMapping(value = "/category/trydelete/{id}", method = RequestMethod.GET)
	public ResponseEntity<DeleteStatus> tryDelete(@PathVariable("id") long id) {
		Category category = categoryService.getById(id);
		if (category == null) {
			log.warn("Cannot find category with id = " + id);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		DeleteStatus status = new DeleteStatus();

		// Find all requirement that use this category
		long requirementsQuantity = requirementService.countByCategoryAndDeletedFalse(category);
		long purchaseQuantity = purchaseRequirementDao.countByCategory(category);

		if (requirementsQuantity < 1  && purchaseQuantity < 1) {
			status.setSuccess(true);

			return new ResponseEntity<>(status, HttpStatus.OK);
		}

		if (requirementsQuantity > 0) {
			status.setRequirementQuantity(requirementsQuantity);
		}

		if (purchaseQuantity > 0) {
			status.setPurchaseQuantity(purchaseQuantity);
		}

		status.setSuccess(false);
		
		return new ResponseEntity<>(status, HttpStatus.OK);
	}
	
	@RequestMapping(path = { "category/{id}/fragments/subcategories"}, method = RequestMethod.GET)
	public String getSubcategoriesFragment(Model model, @PathVariable long id) {
		Category category = categoryService.getById(id);
		if (category == null) {
			log.warn("Requested subcategories for category with ID:" + id + " not found.");
	                return "redirect:/category/";
		}
		
		model.addAttribute("subcategories", category.getSubcategories());
		model.addAttribute("categoryId", category.getId());

		return "category/fragments/subcategories :: subcategories";
	}
	
	@RequireGlobalEditorRole
	@RequestMapping(value = "/category/{id}/addsubcategory", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> addSubcategory(@PathVariable("id") long id, @RequestParam String name) {
		Category category = categoryService.getById(id);
		if (category == null) {
			log.warn("Cannot find category with id = " + id);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		Subcategory subcategory = new Subcategory();
		subcategory.setName(name);
		subcategory.setCategory(category);
		
		category.getSubcategories().add(subcategory);
		categoryService.save(category);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequireGlobalEditorRole
	@RequestMapping(value = "/category/{id}/deletesubcategory", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> deleteSubcategory(@PathVariable("id") long id, @RequestParam(name = "subid") long subcategoryId) {
		Category category = categoryService.getById(id);
		if (category == null) {
			log.warn("Cannot find category with id = " + id);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		Subcategory subCategory= category.getSubcategories().stream().filter(s -> s.getId() == subcategoryId).findAny().orElse(null);
		if (subCategory == null) {
			log.warn("Cannot find subcategory with id = " + subcategoryId);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		category.getSubcategories().remove(subCategory);
		categoryService.save(category);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
