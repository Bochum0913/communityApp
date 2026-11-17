package ca.bytetube.communityApp.web.shopadmin;

import ca.bytetube.communityApp.entity.PersonInfo;
import ca.bytetube.communityApp.entity.Shop;
import ca.bytetube.communityApp.service.impl.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ShopAccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired private AccountService accounts;
    @Override
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) throws Exception {
        PersonInfo user=(PersonInfo)request.getSession().getAttribute("user");
        if(user==null || accounts.currentUser(user.getUserId())==null || user.getUserType()==null || user.getUserType()!=2){
            response.sendError(403,"Merchant login required");return false;
        }
        String uri=request.getRequestURI().substring(request.getContextPath().length());
        if(uri.equals("/shopadmin/dashboard") || uri.equals("/shopadmin/ordermanagement") ||
           uri.equals("/shopadmin/shoplist") || uri.equals("/shopadmin/getshoplist") ||
           uri.equals("/shopadmin/registershop") ||
           uri.equals("/shopadmin/getshopinitinfo")) return true;
        String productId=request.getParameter("productId");
        if(productId!=null && !productId.isEmpty()){
            try{if(!accounts.ownsProduct(user.getUserId(),Long.parseLong(productId))){
                response.sendError(403,"Product access denied");return false;}}
            catch(NumberFormatException e){response.sendError(400);return false;}
        }
        String shopStr=request.getParameter("shopStr");
        if(shopStr!=null && !shopStr.isEmpty()){
            try{JsonNode node=new ObjectMapper().readTree(shopStr);
                if(node.hasNonNull("shopId") && !accounts.ownsShop(user.getUserId(),node.get("shopId").asLong())){
                    response.sendError(403,"Shop access denied");return false;
                }}catch(Exception e){response.sendError(400,"Invalid shop data");return false;}
        }
        String productStr=request.getParameter("productStr");
        if(productStr!=null && !productStr.isEmpty()){
            try{JsonNode node=new ObjectMapper().readTree(productStr);
                if(node.hasNonNull("productId") && !accounts.ownsProduct(user.getUserId(),node.get("productId").asLong())){
                    response.sendError(403,"Product access denied");return false;
                }}catch(Exception e){response.sendError(400,"Invalid product data");return false;}
        }
        String requested=request.getParameter("shopId");
        if(requested!=null && !requested.isEmpty()) {
            long id;
            try{id=Long.parseLong(requested);}catch(NumberFormatException e){response.sendError(400);return false;}
            if(!accounts.ownsShop(user.getUserId(),id)){response.sendError(403,"Shop access denied");return false;}
            return true;
        }
        Shop shop=(Shop)request.getSession().getAttribute("currentShop");
        if(shop==null || shop.getShopId()==null || !accounts.ownsShop(user.getUserId(),shop.getShopId())){
            response.sendError(403,"Choose a shop you own first");return false;
        }
        return true;
    }
}
