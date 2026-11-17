package ca.bytetube.communityApp.service.impl;

import ca.bytetube.communityApp.dao.ProductCategoryDao;
import ca.bytetube.communityApp.dao.ProductDao;
import ca.bytetube.communityApp.dto.ProductCategoryExecution;
import ca.bytetube.communityApp.entity.ProductCategory;
import ca.bytetube.communityApp.enums.ProductCategoryStateEnum;
import ca.bytetube.communityApp.exceptions.ProductCategoryOperationException;
import ca.bytetube.communityApp.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {
	@Autowired
	private ProductCategoryDao productCategoryDao;

	@Autowired
	private ProductDao productDao;


	@Override
	public List<ProductCategory> getProductCategoryList(long shopId) {
		return productCategoryDao.queryProductCategoryList(shopId);
	}

	@Override
	@Transactional
	public ProductCategoryExecution batchAddProductCategory(List<ProductCategory> productCategoryList)
			throws ProductCategoryOperationException {
		if (productCategoryList != null && productCategoryList.size() > 0) {
			try {
				int effectedNum = productCategoryDao.batchInsertProductCategory(productCategoryList);
				if (effectedNum <= 0) {
					throw new ProductCategoryOperationException("failed to create the product category");
				} else {
					return new ProductCategoryExecution(ProductCategoryStateEnum.SUCCESS);
				}
			} catch (Exception e) {
				throw new ProductCategoryOperationException("batchAddProductCategory error: " + e.getMessage());
			}
		} else {
			return new ProductCategoryExecution(ProductCategoryStateEnum.EMPTY_LIST);
		}
	}

	@Override
	@Transactional
	public ProductCategoryExecution deleteProductCategory(long productCategoryId, long shopId)
			throws ProductCategoryOperationException {
        if (productCategoryId <= 0 || shopId <= 0 ||
                productCategoryDao.countProductCategoryForShop(productCategoryId, shopId) != 1) {
            throw new ProductCategoryOperationException("Category does not belong to the current shop");
        }
        // Remove product associations before deleting the category.
		try {
			int effectedNum = productDao.updateProductCategoryToNull(productCategoryId);
			if (effectedNum < 0) {
				throw new ProductCategoryOperationException("product category update failed");
			}
		} catch (Exception e) {
			throw new ProductCategoryOperationException("deleteProductCategory error: " + e.getMessage());
		}
		// Delete the selected category after associations are removed.
		try {
			int effectedNum = productCategoryDao.deleteProductCategory(productCategoryId, shopId);
			if (effectedNum <= 0) {
				throw new ProductCategoryOperationException("failed to delete the product category!");
			} else {
				return new ProductCategoryExecution(ProductCategoryStateEnum.SUCCESS);
			}
		} catch (Exception e) {
			throw new ProductCategoryOperationException("deleteProductCategory error:" + e.getMessage());
		}
	}
    @Override
    @Transactional
    public ProductCategoryExecution updateProductCategoryPriority(
            long productCategoryId, long shopId, int priority)
            throws ProductCategoryOperationException {
        if (productCategoryId <= 0 || shopId <= 0 || priority < 0 || priority > 1000000) {
            throw new ProductCategoryOperationException("Invalid category, shop, or priority");
        }
        if (productCategoryDao.countProductCategoryForShop(productCategoryId, shopId) != 1) {
            throw new ProductCategoryOperationException("Category does not belong to the current shop");
        }
        productCategoryDao.updateProductCategoryPriority(productCategoryId, shopId, priority);
        return new ProductCategoryExecution(ProductCategoryStateEnum.SUCCESS);
    }
}

