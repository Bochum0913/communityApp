package ca.bytetube.communityApp.web.local;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/** Resolve the account page URLs used by the navigation menu. */
@Controller
@RequestMapping("/local")
public class LocalPageController {
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register() { return "local/register"; }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() { return "local/login"; }

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public String account() { return "local/account"; }

    @RequestMapping(value = "/accountbind", method = RequestMethod.GET)
    public String accountBind() { return "local/accountbind"; }

    @RequestMapping(value = "/changepsw", method = RequestMethod.GET)
    public String changePassword() { return "local/changepsw"; }
}
