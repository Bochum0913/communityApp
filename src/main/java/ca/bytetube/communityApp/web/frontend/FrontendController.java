package ca.bytetube.communityApp.web.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/frontend")
public class FrontendController {

    /** Render a frontend page. */
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    private String index() {
        return "frontend/index";
    }

    /** Render a frontend page. */
    @RequestMapping(value = "/shoplist", method = RequestMethod.GET)
    private String showShopList() {
        return "frontend/shoplist";
    }

    /** Render a frontend page. */
    @RequestMapping(value = "/shopdetail", method = RequestMethod.GET)
    private String showShopDetail() {
        return "frontend/shopdetail";
    }

    /** Render a frontend page. */
    @RequestMapping(value = "/productdetail", method = RequestMethod.GET)
    private String showProductDetail() {
        return "frontend/productdetail";
    }
    @RequestMapping(value = "/myrecord", method = RequestMethod.GET)
    public String myRecord() { return "frontend/myrecord"; }

    @RequestMapping(value = "/mypoint", method = RequestMethod.GET)
    public String myPoint() { return "frontend/mypoint"; }

    @RequestMapping(value = "/pointrecord", method = RequestMethod.GET)
    public String pointRecord() { return "frontend/pointrecord"; }
    @RequestMapping(value="/paymentresult",method=RequestMethod.GET)
    private String paymentResult() { return "frontend/paymentresult"; }
}

