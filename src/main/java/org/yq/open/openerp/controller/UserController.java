package org.yq.open.openerp.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yq.open.openerp.entity.ProductInventory;
import org.yq.open.openerp.entity.UseRecord;
import org.yq.open.openerp.entity.User;
import org.yq.open.openerp.repository.UserRepository;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    @Autowired
    UserRepository userRepository;

    @RequestMapping("/showUser")
    public Map<String, Object> showUser(HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (null == u) {
            throw new IllegalStateException("SESSION不存在！");
        }
        Map<String, Object> result = toSuccess(u);
        result.put("tempUserId", session.getId());
        return result;
    }

    @RequestMapping("/addUser")
    public Map<String, Object> addUser(User user, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (null == u) {
            throw new IllegalStateException("SESSION不存在！");
        }
        user.setId(UUID.randomUUID().toString());
        user.setParentId(u.getId());
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());

        userRepository.saveAndFlush(u);

        return toSuccess(null);
    }


    @RequestMapping("/listUser")
    public Map<String, Object> listUser(@RequestParam(name = "pageNumber", required = false) Integer pageNumber, @RequestParam(name = "pageSize", required = false) Integer pageSize, @RequestParam(name = "userName", required = false) String userName) {

        User d = new User();
        if(StringUtils.isNotBlank(userName))
        {
            d.setUserName(userName);
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("userName", ExampleMatcher.GenericPropertyMatchers.contains())
                .withIgnorePaths("id");
        Example<User> ex = Example.of(d,matcher);

        if (pageNumber == null) {
            pageNumber = 0;
        }

        if (pageSize == null) {
            pageSize = 10;
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "createDate");


        Page<User> ls = userRepository.findAll(ex, pageable);

        return toSuccess(ls);
    }

    @RequestMapping("/resetPassword")
    public Map<String,Object> resetPassword(String userId,String passWord)
    {
        Optional<User> op = userRepository.findById(userId);
        if(op.isPresent())
        {
            User u = op.get();
            u.setPassword(passWord);
            userRepository.saveAndFlush(u);
            return toSuccess(null);
        }
        else
        {
            return toError("-1","用户不存在！");
        }

    }
}
