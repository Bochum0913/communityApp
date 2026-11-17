
package ca.bytetube.community.dao;

import ca.bytetube.community.BaseTest;
import ca.bytetube.communityApp.dao.AreaDao;
import ca.bytetube.communityApp.entity.Area;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class AreaDaoTest extends BaseTest {

	@Autowired
	private AreaDao areaDao;

	@Test
	public void testQueryArea() {
		List<Area> areaList = areaDao.queryArea();

		assertNotNull("Area list should not be null", areaList);
		assertEquals("Test database should initially contain no areas",
				0, areaList.size());
	}
}
