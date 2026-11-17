package ca.bytetube.communityApp.web.frontend;

import ca.bytetube.communityApp.entity.PersonInfo;
import ca.bytetube.communityApp.service.impl.CommerceService;
import ca.bytetube.communityApp.web.local.AccountController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/frontend/api")
public class CommerceController {
    @Autowired private CommerceService commerce;

    private PersonInfo user(HttpServletRequest request){
        return (PersonInfo)request.getSession().getAttribute("user");
    }
    private boolean customer(PersonInfo p){return p!=null && p.getUserType()!=null && p.getUserType()==1;}
    private boolean merchant(PersonInfo p){return p!=null && p.getUserType()!=null && p.getUserType()==2;}
    private Map<String,Object> ok(Object data){Map<String,Object> result=AccountController.result(true,"");result.put("data",data);return result;}

    @RequestMapping(value="/orders",method=RequestMethod.GET)
    @ResponseBody public Map<String,Object> orders(HttpServletRequest request){
        PersonInfo p=user(request);if(p==null)return AccountController.result(false,"Sign in first.");
        return ok(merchant(p)?commerce.merchantOrders(p.getUserId()):commerce.customerOrders(p.getUserId()));
    }

    /** Legacy order creation is disabled; payments use /stripe-checkout. */
    @RequestMapping(value="/place-order",method=RequestMethod.POST)
    @ResponseBody public Map<String,Object> legacyPlaceOrder(HttpServletRequest request) {
        return AccountController.result(false,"Use Stripe Checkout to place an order.");
    }

    @RequestMapping(value="/cancel-order",method=RequestMethod.POST)
    @ResponseBody public Map<String,Object> cancel(HttpServletRequest request,@RequestParam long orderId){
        PersonInfo p=user(request);if(!customer(p))return AccountController.result(false,"Customer sign-in required.");
        if(!AccountController.validCsrf(request))return AccountController.result(false,"Invalid request token.");
        try{commerce.cancelOrder(p.getUserId(),orderId);return ok("Order cancelled");}
        catch(Exception e){return AccountController.result(false,e instanceof IllegalArgumentException?e.getMessage():"Cancellation failed.");}
    }

    @RequestMapping(value="/complete-order",method=RequestMethod.POST)
    @ResponseBody public Map<String,Object> complete(HttpServletRequest request,@RequestParam long orderId){
        PersonInfo p=user(request);if(!merchant(p))return AccountController.result(false,"Merchant sign-in required.");
        if(!AccountController.validCsrf(request))return AccountController.result(false,"Invalid request token.");
        try{commerce.completeOrder(p.getUserId(),orderId);return ok("Order completed and points credited");}
        catch(Exception e){return AccountController.result(false,e instanceof IllegalArgumentException?e.getMessage():"Unable to complete order.");}
    }

    @RequestMapping(value="/points",method=RequestMethod.GET)
    @ResponseBody public Map<String,Object> points(HttpServletRequest request){
        PersonInfo p=user(request);if(p==null)return AccountController.result(false,"Sign in first.");
        return ok(commerce.balance(p.getUserId()));
    }

    @RequestMapping(value="/point-history",method=RequestMethod.GET)
    @ResponseBody public Map<String,Object> history(HttpServletRequest request){
        PersonInfo p=user(request);if(p==null)return AccountController.result(false,"Sign in first.");
        return ok(commerce.pointHistory(p.getUserId()));
    }
}
