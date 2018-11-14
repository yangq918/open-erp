package org.yq.open.openerp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yq.open.openerp.entity.ProductInventory;
import org.yq.open.openerp.entity.User;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    @RequestMapping("/showUser")
    public Map<String, Object> showUser(HttpSession session) {
        User u = (User)session.getAttribute("user");
        if(null==u)
        {
            throw new IllegalStateException("SESSION不存在！");
        }
        return  toSuccess(u);
    }
}
