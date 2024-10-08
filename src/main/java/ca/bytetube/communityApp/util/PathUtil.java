package ca.bytetube.communityApp.util;

public class PathUtil {
	private static String separator = System.getProperty("file.separator");

	public static String getImgBasePath() {
		String os = System.getProperty("os.name");
		String basePath = "";
		if (os.toLowerCase().startsWith("win")) {
			basePath = "C:/Users/lbh19/OneDrive/Desktop/image";
		} else {
			basePath ="C:/Workplace/image" ;
		}
		basePath = basePath.replace("/", separator);
		return basePath;
	}

	public static String getShopImagePath(long shopId) {
		String imagePath = "/upload/images/item/shop/" + shopId + "/";
		return imagePath.replace("/", separator);
	}

}
