package ca.bytetube.communityApp.dao;

import ca.bytetube.communityApp.entity.ProductCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductCategoryDao {
	/**
	 * Find product categories belonging to a shop.
	 */
	List<ProductCategory> queryProductCategoryList(long shopId);

	/**
	 * Insert multiple product categories.
	 */
	int batchInsertProductCategory(List<ProductCategory> productCategoryList);

	/**
	 * Delete a product category owned by the current shop.
	 */
	int deleteProductCategory(@Param("productCategoryId") long productCategoryId, @Param("shopId") long shopId);
    /** Update only a category owned by the current shop. */
    int updateProductCategoryPriority(@Param("productCategoryId") long productCategoryId,
                                      @Param("shopId") long shopId,
                                      @Param("priority") int priority);

    /** Check ownership before changing product-category associations. */
    int countProductCategoryForShop(@Param("productCategoryId") long productCategoryId,
                                    @Param("shopId") long shopId);
}

