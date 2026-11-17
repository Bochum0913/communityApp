package ca.bytetube.communityApp.web.local;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
@Controller
@RequestMapping("/local")
public class LocalSessionController {
    @RequestMapping(value="/logout",method=RequestMethod.POST)
    @ResponseBody public Map<String,Object> logout(HttpServletRequest request){
        if(!AccountController.validCsrf(request))return AccountController.result(false,"Invalid request token.");
        request.getSession().invalidate();return AccountController.result(true,"Signed out.");
    }
}
