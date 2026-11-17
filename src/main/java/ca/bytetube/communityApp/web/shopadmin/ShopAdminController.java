package ca.bytetube.communityApp.web.shopadmin;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "shopadmin", method = { RequestMethod.GET })

/**
 // Implementation note.
 */
public class ShopAdminController {

	@RequestMapping(value = "/productcategorymanagement", method = RequestMethod.GET)
	private String productCategoryManage() {
  // Implementation note.
		return "shop/productcategorymanagement";
	}

	@RequestMapping(value = "/shopmanagement")
	public String shopManagement() {
  // Implementation note.
		return "shop/shopmanagement";
	}

	@RequestMapping(value = "/shopoperation")
	public String shopOperation() {

		return "shop/shopoperation";
	}

	@RequestMapping(value = "/shoplist")
	public String shopList() {
  // Implementation note.
		return "shop/shoplist";
	}

	@RequestMapping(value = "/productoperation")
	public String productOperation() {
  // Implementation note.
		return "shop/productoperation";
	}

	@RequestMapping(value = "/productmanagement")
	public String productManagement() {
  // Implementation note.
		return "shop/productmanagement";
	}

    @RequestMapping(value = "/dashboard")
    public String dashboard() { return "shop/dashboard"; }

    @RequestMapping(value = "/ordermanagement")
    public String orderManagement() { return "shop/ordermanagement"; }
}
