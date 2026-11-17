package ca.bytetube.communityApp.service;

import ca.bytetube.communityApp.dto.ProductCategoryExecution;
import ca.bytetube.communityApp.entity.ProductCategory;
import ca.bytetube.communityApp.exceptions.ProductCategoryOperationException;

import java.util.List;

public interface ProductCategoryService {
	/** Product category operation. */
	List<ProductCategory> getProductCategoryList(long shopId);

	/** Product category operation. */
	ProductCategoryExecution batchAddProductCategory(List<ProductCategory> productCategoryList)
			throws ProductCategoryOperationException;

	/** Product category operation. */
	ProductCategoryExecution deleteProductCategory(long productCategoryId, long shopId) throws ProductCategoryOperationException;

    /** Change the priority for a category in the current shop. */
    ProductCategoryExecution updateProductCategoryPriority(long productCategoryId,
            long shopId, int priority) throws ProductCategoryOperationException;
}

