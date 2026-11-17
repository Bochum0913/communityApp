package ca.bytetube.communityApp.web.local;

import ca.bytetube.communityApp.entity.PersonInfo;
import ca.bytetube.communityApp.service.impl.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/local/api")
public class AccountController {
    @Autowired private AccountService accounts;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String csrf(HttpSession session){
        String token=(String)session.getAttribute("csrfToken");
        if(token==null){byte[] bytes=new byte[32];RANDOM.nextBytes(bytes);
            token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute("csrfToken",token);}
        return token;
    }
    public static boolean validCsrf(HttpServletRequest request){
        String expected=(String)request.getSession().getAttribute("csrfToken");
        String actual=request.getHeader("X-CSRF-Token");
        return expected!=null && expected.equals(actual);
    }
    public static Map<String,Object> result(boolean success,String message){
        Map<String,Object> result=new HashMap<>();result.put("success",success);result.put("message",message);return result;
    }
    public static Long currentId(HttpSession session){
        PersonInfo user=(PersonInfo)session.getAttribute("user");
        return user==null?null:user.getUserId();
    }

    @RequestMapping(value="/session",method=RequestMethod.GET)
    @ResponseBody public Map<String,Object> session(HttpServletRequest request){
        Map<String,Object> result=result(true,"");
        Long id=currentId(request.getSession());
        PersonInfo user=id==null?null:accounts.currentUser(id);
        if(user==null && id!=null) request.getSession().removeAttribute("user");
        result.put("authenticated",user!=null);
        if(user!=null){result.put("username",user.getName());result.put("email",user.getEmail());
            result.put("userId",user.getUserId());result.put("userType",user.getUserType());}
        result.put("csrfToken",csrf(request.getSession()));return result;
    }

    @RequestMapping(value="/register",method=RequestMethod.POST)
    @ResponseBody public Map<String,Object> register(HttpServletRequest request,
           @RequestParam String username,@RequestParam String password,@RequestParam String email,
           @RequestParam(defaultValue="customer") String role){
        if(!validCsrf(request))return result(false,"Invalid request token. Refresh and try again.");
        try {PersonInfo user=accounts.register(username,password,email,role);
            request.getSession().invalidate();HttpSession session=request.getSession(true);
            session.setAttribute("user",user);csrf(session);return result(true,"Registration successful.");
        }catch(Exception e){return result(false,e instanceof IllegalArgumentException?e.getMessage():"Registration failed.");}
    }

    @RequestMapping(value="/login",method=RequestMethod.POST)
    @ResponseBody public Map<String,Object> login(HttpServletRequest request,
            @RequestParam String username,@RequestParam String password){
        if(!validCsrf(request))return result(false,"Invalid request token. Refresh and try again.");
        PersonInfo user=accounts.login(username,password);
        if(user==null)return result(false,"Invalid username or password.");
        request.getSession().invalidate();HttpSession session=request.getSession(true);
        session.setAttribute("user",user);csrf(session);return result(true,"Signed in.");
    }

    @RequestMapping(value="/change-password",method=RequestMethod.POST)
    @ResponseBody public Map<String,Object> changePassword(HttpServletRequest request,
            @RequestParam String oldPassword,@RequestParam String newPassword){
        if(!validCsrf(request))return result(false,"Invalid request token.");
        Long id=currentId(request.getSession());if(id==null)return result(false,"Sign in first.");
        try {accounts.changePassword(id,oldPassword,newPassword);return result(true,"Password updated.");}
        catch(Exception e){return result(false,e instanceof IllegalArgumentException?e.getMessage():"Unable to update password.");}
    }

    @RequestMapping(value="/bind-email",method=RequestMethod.POST)
    @ResponseBody public Map<String,Object> bindEmail(HttpServletRequest request,
            @RequestParam String password,@RequestParam String email){
        if(!validCsrf(request))return result(false,"Invalid request token.");
        Long id=currentId(request.getSession());if(id==null)return result(false,"Sign in first.");
        try {accounts.bindEmail(id,password,email);request.getSession().setAttribute("user",accounts.currentUser(id));
            return result(true,"Email updated.");}
        catch(Exception e){return result(false,e instanceof IllegalArgumentException?e.getMessage():"Unable to update email.");}
    }
}
