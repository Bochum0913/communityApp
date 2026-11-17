package ca.bytetube.communityApp.util;

public class PathUtil {
	private static String separator = System.getProperty("file.separator");

	public static String getImgBasePath() {
		return "D:\\communityApp-uploads";
	}

	public static String getShopImagePath(long shopId) {
		String imagePath = "/upload/images/item/shop/" + shopId + "/";
		return imagePath.replace("/", separator);
	}

}
