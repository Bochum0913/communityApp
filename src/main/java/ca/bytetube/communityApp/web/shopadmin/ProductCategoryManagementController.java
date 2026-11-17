package ca.bytetube.communityApp.web.shopadmin;

import ca.bytetube.communityApp.dto.ProductCategoryExecution;
import ca.bytetube.communityApp.dto.Result;
import ca.bytetube.communityApp.entity.ProductCategory;
import ca.bytetube.communityApp.entity.Shop;
import ca.bytetube.communityApp.enums.ProductCategoryStateEnum;
import ca.bytetube.communityApp.exceptions.ProductCategoryOperationException;
import ca.bytetube.communityApp.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/shopadmin")
public class ProductCategoryManagementController {
	@Autowired
	private ProductCategoryService productCategoryService;

	@RequestMapping(value = "/getproductcategorylist", method = RequestMethod.GET)
	@ResponseBody
	private Result<List<ProductCategory>> getProductCategoryList(HttpServletRequest request) {

		Shop currentShop = (Shop) request.getSession().getAttribute("currentShop");
		List<ProductCategory> list = null;
		if (currentShop != null && currentShop.getShopId() > 0) {
			list = productCategoryService.getProductCategoryList(currentShop.getShopId());
			return new Result<List<ProductCategory>>(true, list);
		} else {
			ProductCategoryStateEnum ps = ProductCategoryStateEnum.INNER_ERROR;
			return new Result<List<ProductCategory>>(false, ps.getState(), ps.getStateInfo());
		}
	}
	@RequestMapping(value = "/addproductcategorys", method = RequestMethod.POST)
	@ResponseBody
	private Map<String, Object> addProductCategorys(@RequestBody List<ProductCategory> productCategoryList,
													HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<>();
		Shop currentShop = (Shop) request.getSession().getAttribute("currentShop");
        if (currentShop == null || currentShop.getShopId() == null || currentShop.getShopId() <= 0
                || productCategoryList == null) {
            modelMap.put("success", false);
            modelMap.put("errMsg", "No shop selected or category list missing");
            return modelMap;
        }
		for (ProductCategory pc : productCategoryList) {
			pc.setShopId(currentShop.getShopId());
		}

		if (productCategoryList != null && productCategoryList.size() > 0) {
			try {
				ProductCategoryExecution pe = productCategoryService.batchAddProductCategory(productCategoryList);
				if (pe.getState() == ProductCategoryStateEnum.SUCCESS.getState()) {
					modelMap.put("success", true);
				} else {
					modelMap.put("success", false);
					modelMap.put("errMsg", pe.getStateInfo());
				}
			} catch (ProductCategoryOperationException e) {
				modelMap.put("success", false);
				modelMap.put("errMsg", e.toString());
				return modelMap;
			}
		} else {
			modelMap.put("success", false);
			modelMap.put("errMsg", "please enter at least one product category");
		}
		return modelMap;
	}

	@RequestMapping(value = "/removeproductcategory", method = RequestMethod.POST)
	@ResponseBody
	private Map<String, Object> removeProductCategory(Long productCategoryId, HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<>();
		if (productCategoryId != null && productCategoryId > 0) {
			try {
				Shop currentShop = (Shop) request.getSession().getAttribute("currentShop");
				ProductCategoryExecution pe = productCategoryService.deleteProductCategory(productCategoryId,
						currentShop.getShopId());
				if (pe.getState() == ProductCategoryStateEnum.SUCCESS.getState()) {
					modelMap.put("success", true);
				} else {
					modelMap.put("success", false);
					modelMap.put("errMsg", pe.getStateInfo());
				}
			} catch (ProductCategoryOperationException e) {
				modelMap.put("success", false);
				modelMap.put("errMsg", e.toString());
				return modelMap;
			}
		} else {
			modelMap.put("success", false);
			modelMap.put("errMsg", "please enter at least one product category");
		}
		return modelMap;
	}
    /** Persist a priority change for a category owned by the selected shop. */
    @RequestMapping(value = "/modifyproductcategory", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> modifyProductCategory(
            @org.springframework.web.bind.annotation.RequestParam("productCategoryId") Long productCategoryId,
            @org.springframework.web.bind.annotation.RequestParam("priority") Integer priority,
            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Shop shop = (Shop) request.getSession().getAttribute("currentShop");
        if (shop == null || shop.getShopId() == null || shop.getShopId() <= 0 ||
                productCategoryId == null || priority == null) {
            result.put("success", false);
            result.put("errMsg", "Select a shop and provide a category ID and priority");
            return result;
        }
        try {
            ProductCategoryExecution execution = productCategoryService.updateProductCategoryPriority(
                    productCategoryId, shop.getShopId(), priority);
            result.put("success", execution.getState() == ProductCategoryStateEnum.SUCCESS.getState());
        } catch (ProductCategoryOperationException ex) {
            result.put("success", false);
            result.put("errMsg", ex.getMessage());
        }
        return result;
    }
}



