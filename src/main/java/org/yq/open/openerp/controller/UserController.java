package org.yq.open.openerp.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.store.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.data.domain.*;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yq.open.openerp.entity.ProductInventory;
import org.yq.open.openerp.entity.UseRecord;
import org.yq.open.openerp.entity.User;
import org.yq.open.openerp.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserRepository userRepository;

    @RequestMapping("/showUser")
    public Map<String, Object> showUser(HttpSession session) {
        User u = (User) session.getAttribute("user");
        LOGGER.info("showUser:{}",u);
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
        User d = new User();
        d.setAccount(user.getAccount());
        Example<User> ex = Example.of(d);
        List<User> dbUsers = userRepository.findAll(ex);
        if(CollectionUtils.isNotEmpty(dbUsers))
        {
            return  toError("-1","该账号已经存在！");
        }
        user.setId(UUID.randomUUID().toString());
        user.setParentId(u.getId());
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());

        userRepository.saveAndFlush(user);

        return toSuccess(null);
    }


    @RequestMapping("/listUser")
    public Map<String, Object> listUser(@RequestParam(name = "pageNumber", required = false) Integer pageNumber, @RequestParam(name = "pageSize", required = false) Integer pageSize, @RequestParam(name = "searchValue", required = false) String searchValue) {

        User d = new User();
        if(StringUtils.isNotBlank(searchValue))
        {
            d.setUserName(searchValue);
            d.setAccount(searchValue);
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("userName", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("account", ExampleMatcher.GenericPropertyMatchers.contains())
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


    @RequestMapping("/del")
    public Map<String, Object> del(String id) {
        userRepository.deleteById(id);
        return toSuccess(null);
    }

    @RequestMapping(path = "/uploadImg", method = RequestMethod.POST)
    public Map<String, Object> uploadImg(@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

        User u = (User) session.getAttribute("user");
        if (null == u) {
            throw new IllegalStateException("SESSION不存在！");
        }
        String path = System.getProperty("user.dir")+File.separator+"headImg";
        File dir = new File(path);
        if(!dir.exists())
        {
            dir.mkdirs();
        }
        String orgName = file.getOriginalFilename();
        String extName = FilenameUtils.getExtension(orgName);
        IOUtils.copy(file.getInputStream(),new FileOutputStream(path+File.separator+u.getId()+"."+extName));

        Optional<User> op = userRepository.findById(u.getId());
        if(op.isPresent())
        {
            User udb = op.get();
            udb.setImgUrl("/api/user/showHeadImg?name="+u.getId()+"."+extName);
            userRepository.saveAndFlush(udb);
            u.setImgUrl(udb.getImgUrl()+"&random="+(new Date().getTime()));
            session.setAttribute("user", u);
            return toSuccess(null);
        }
        else
        {
            return toError("-1","用户不存在！");
        }
    }

    @RequestMapping(value = "/showHeadImg",method = {RequestMethod.GET,RequestMethod.POST})
    public String downloadImage(String name, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = System.getProperty("user.dir")+File.separator+"headImg";
        File dir = new File(path);
        if(!dir.exists())
        {
            dir.mkdirs();
        }
        File picFile = new File(path+File.separator+name);
        if (picFile.exists())
        {
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition",
                    "attachment;fileName=" + name);
            IOUtils.copy(new FileInputStream(picFile),response.getOutputStream());
        }
        return  null;
    }

}
