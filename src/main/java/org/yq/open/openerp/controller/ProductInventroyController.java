package org.yq.open.openerp.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yq.open.openerp.entity.ProductInventory;
import org.yq.open.openerp.entity.UseRecord;
import org.yq.open.openerp.repository.ProductInventoryRepository;
import org.yq.open.openerp.repository.UseRecordRepository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/proInventory")
public class ProductInventroyController extends BaseController {

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Autowired
    private UseRecordRepository useRecordRepository;


    @RequestMapping("/list")
    public Map<String, Object> list(@RequestParam(name = "pageNumber", required = false) Integer pageNumber, @RequestParam(name = "pageSize", required = false) Integer pageSize,
                                    @RequestParam(name = "searchType", required = false) String searchType, @RequestParam(name = "searchValue", required = false)String searchValue) {
        ExampleMatcher matcher = ExampleMatcher.matching();
        ProductInventory param = new ProductInventory();
        Specification<ProductInventory> spec = new Specification<ProductInventory>(){

            @Override
            public Predicate toPredicate(Root<ProductInventory> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if(StringUtils.isNotBlank(searchType)&&StringUtils.isNotBlank(searchValue))
                {
                    if(StringUtils.equalsIgnoreCase(searchType,"all"))
                    {
                        Predicate p1 = cb.like(root.get("productNo").as(String.class),"%"+searchValue+"%");
                        Predicate p2 = cb.like(root.get("manufacturer").as(String.class),"%"+searchValue+"%");
                        Predicate p3 = cb.like(root.get("name").as(String.class),"%"+searchValue+"%");
                        query.where(cb.or(cb.or(p1,p2),p3));
                    }
                    else
                    {
                        query.where(cb.like(root.get(searchType).as(String.class),"%"+searchValue+"%"));
                    }
                }
                return null;
            }
        };

        if (pageNumber == null) {
            pageNumber = 0;
        }

        if (pageSize == null) {
            pageSize = 10;
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "createDate");


        Page<ProductInventory> ls = productInventoryRepository.findAll(spec,pageable);

        return toSuccess(ls);
    }

    @RequestMapping("/add")
    public Map<String, Object> add(ProductInventory data) {
        data.setId(UUID.randomUUID().toString());
        data.setCreateDate(new Date());
        data.setUpdateDate(new Date());

        productInventoryRepository.saveAndFlush(data);

        return toSuccess(null);
    }

    @RequestMapping("/detail")
    public Map<String, Object> detail(String id) {
        Optional<ProductInventory> op = productInventoryRepository.findById(id);
        if (op.isPresent()) {
            return toSuccess(op.get());
        }
        return toError("-1", "无该产品库存信息！");
    }

    @RequestMapping("/del")
    public Map<String, Object> del(String id) {
        productInventoryRepository.deleteById(id);
        return toSuccess(null);
    }

    @RequestMapping("/modify")
    public Map<String, Object> modify(ProductInventory param) {
        Optional<ProductInventory> op = productInventoryRepository.findById(param.getId());
        if (op.isPresent()) {
            ProductInventory db = op.get();
            param.setUpdateDate(new Date());
            param.setCreateDate(db.getCreateDate());
            productInventoryRepository.saveAndFlush(param);
            return toSuccess(op.get());
        }
        return toError("-1", "无该产品库存信息！");
    }

    @RequestMapping("/use")
    public Map<String, Object> use(UseRecord data) {
        data.setCreateDate(new Date());
        data.setUpdateDate(new Date());
        Optional<ProductInventory> op = productInventoryRepository.findById(data.getProId());

        if (op.isPresent()) {
            ProductInventory pi = op.get();
            long rl = subtract(pi.getInventoryNum(), data.getNum());
            if (rl < 0) {
                return toError("-1", "库存不够！");
            }
            pi.setInventoryNum(subtract(pi.getInventoryNum(), data.getNum()));
            data.setId(UUID.randomUUID().toString());
            useRecordRepository.saveAndFlush(data);
            productInventoryRepository.saveAndFlush(pi);
            return toSuccess(null);
        } else {
            return toError("-1", "无该产品库存信息！");
        }

    }

    @RequestMapping("/listUseRecord")
    public Map<String, Object> listUseRecord(@RequestParam(name = "pageNumber", required = false) Integer pageNumber, @RequestParam(name = "pageSize", required = false) Integer pageSize, String proId) {

        UseRecord d = new UseRecord();
        d.setProId(proId);
        Example<UseRecord> ex = Example.of(d);

        if (pageNumber == null) {
            pageNumber = 0;
        }

        if (pageSize == null) {
            pageSize = 5;
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "createDate");


        Page<UseRecord> ls = useRecordRepository.findAll(ex, pageable);

        return toSuccess(ls);
    }

    private long subtract(Long inventoryNum, Long num) {

        if (null == inventoryNum) {
            return 0L;
        }
        if (null == num) {
            return inventoryNum;
        }

        return inventoryNum - num;
    }
}
