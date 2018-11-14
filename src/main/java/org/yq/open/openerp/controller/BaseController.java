package org.yq.open.openerp.controller;

import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public Map<String,Object> toSuccess(Object data)
    {
        Map<String,Object> result = new HashMap<>();
        result.put("code","0");
        result.put("data",data);
        return  result;
    }

    public  Map<String,Object> toError(String code,String msg)
    {
        Map<String,Object> result = new HashMap<>();
        result.put("code",code);
        result.put("msg",msg);
        return  result;
    }

}
