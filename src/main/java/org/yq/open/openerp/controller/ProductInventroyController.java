package org.yq.open.openerp.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yq.open.openerp.entity.ProductInventory;
import org.yq.open.openerp.entity.UseRecord;
import org.yq.open.openerp.entity.User;
import org.yq.open.openerp.repository.ProductInventoryRepository;
import org.yq.open.openerp.repository.UseRecordRepository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;

@RestController
@RequestMapping("/api/proInventory")
public class ProductInventroyController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductInventroyController.class);

    private final static String xls = "xls";
    private final static String xlsx = "xlsx";

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Autowired
    private UseRecordRepository useRecordRepository;


    @RequestMapping("/list")
    public Map<String, Object> list(@RequestParam(name = "pageNumber", required = false) Integer pageNumber, @RequestParam(name = "pageSize", required = false) Integer pageSize,
                                    @RequestParam(name = "searchType", required = false) String searchType, @RequestParam(name = "searchValue", required = false) String searchValue) {
        ExampleMatcher matcher = ExampleMatcher.matching();
        ProductInventory param = new ProductInventory();
        Specification<ProductInventory> spec = new Specification<ProductInventory>() {

            @Override
            public Predicate toPredicate(Root<ProductInventory> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (StringUtils.isNotBlank(searchType) && StringUtils.isNotBlank(searchValue)) {
                    if (StringUtils.equalsIgnoreCase(searchType, "all")) {
                        Predicate p1 = cb.like(root.get("productNo").as(String.class), "%" + searchValue + "%");
                        Predicate p2 = cb.like(root.get("manufacturer").as(String.class), "%" + searchValue + "%");
                        Predicate p3 = cb.like(root.get("name").as(String.class), "%" + searchValue + "%");
                        query.where(cb.or(cb.or(p1, p2), p3));
                    } else {
                        query.where(cb.like(root.get(searchType).as(String.class), "%" + searchValue + "%"));
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


        Page<ProductInventory> ls = productInventoryRepository.findAll(spec, pageable);

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
            pi.setInventoryNum(rl);
            data.setId(UUID.randomUUID().toString());
            useRecordRepository.saveAndFlush(data);
            productInventoryRepository.saveAndFlush(pi);
            return toSuccess(null);
        } else {
            return toError("-1", "无该产品库存信息！");
        }

    }

    @RequestMapping("/addMoreProd")
    public Map<String, Object> addMoreProd(UseRecord data) {
        data.setCreateDate(new Date());
        data.setUpdateDate(new Date());
        Optional<ProductInventory> op = productInventoryRepository.findById(data.getProId());

        if (op.isPresent()) {
            ProductInventory pi = op.get();
            long rl = add(pi.getInventoryNum(), data.getNum());

            pi.setInventoryNum(rl);
            data.setId(UUID.randomUUID().toString());
            useRecordRepository.saveAndFlush(data);
            productInventoryRepository.saveAndFlush(pi);
            return toSuccess(null);
        } else {
            return toError("-1", "无该产品库存信息！");
        }

    }

    private long add(Long inventoryNum, Long num) {
        if (null == inventoryNum) {
            return num;
        }
        if (null == num) {
            return inventoryNum;
        }

        return inventoryNum + num;
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

    @RequestMapping(path = "/import", method = RequestMethod.POST)
    public Map<String, Object> importFile(@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        LOGGER.info(file.getName());
        User u = (User) session.getAttribute("user");
        if (null == u) {
            throw new IllegalStateException("SESSION不存在！");
        }
        Workbook workbook = getWorkBook(file);
        if (null != workbook) {
            Sheet sheet = workbook.getSheetAt(0);

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum <= 0) {
                return toSuccess(null);
            }
            LOGGER.info("firstRowNum:{},lastRowNum:{}", firstRowNum, lastRowNum);
            Map<String, Short> titles = readTitles(sheet.getRow(firstRowNum));
            List<ProductInventory> pis = readProducts(sheet, titles);

            LOGGER.info("titles:{}", titles);
            Map<String,Object> result = importFileToDB(pis, u);

            return toSuccess(result);

        }

        return toError("-1","导入失败!");
    }

    private Map<String,Object> importFileToDB(List<ProductInventory> pis, User u) {

        List<ProductInventory> newProds = new ArrayList<>();
        int addNum = 0;
        int modifyNum = 0;
        for (ProductInventory pi : pis) {
            List<ProductInventory> dbProds = productInventoryRepository.findByProductNoAndUserId(pi.getProductNo(), u.getId());

            if (CollectionUtils.isEmpty(dbProds)) {
                pi.setUserId(u.getId());
                pi.setId(UUID.randomUUID().toString());
                pi.setUpdateDate(new Date());
                pi.setCreateDate(new Date());
                newProds.add(pi);
                addNum = addNum +1;
            } else {
                ProductInventory dbProd = dbProds.get(0);
                pi.setId(dbProd.getId());
                pi.setUserId(u.getId());
                pi.setCreateDate(dbProd.getCreateDate());
                pi.setUpdateDate(new Date());
                newProds.add(pi);
                modifyNum = modifyNum + 1;
            }
        }
        Map<String,Object> result = new HashMap<>();
        result.put("addNum",addNum);
        result.put("modifyNum",modifyNum);
        productInventoryRepository.saveAll(newProds);
        return result;
    }

    private List<ProductInventory> readProducts(Sheet sheet, Map<String, Short> titles) {
        List<ProductInventory> ps = new ArrayList<>();
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        for (int i = (firstRowNum + 1); i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (null == row) {
                break;
            }
            ProductInventory p = readProduct(row, titles);

            if (null != p) {
                ps.add(p);
            }
        }

        return ps;
    }

    private ProductInventory readProduct(Row row, Map<String, Short> titles) {

        ProductInventory p = new ProductInventory();
        p.setProductNo(getStringField(row, titles, "物资编码"));
        p.setInventoryNum(getLongField(row, titles, "库存值"));
        p.setAddress(getStringField(row, titles, "存放位置"));
        p.setName(getStringField(row, titles, "物资名称"));
        p.setUnit(getStringField(row, titles, "单位"));
        p.setUnitCost(getStringField(row, titles, "单位成本"));
        p.setProductModel(getStringField(row, titles, "型号"));
        p.setSpec(getStringField(row, titles, "规格"));
        p.setDesc(getStringField(row, titles, "说明"));
        p.setManufacturer(getStringField(row, titles, "生产厂商"));
        p.setmTeleNumber(getStringField(row, titles, "厂家联系电话"));
        p.setPurpose(getStringField(row, titles, "用途"));

        if (StringUtils.isNotBlank(p.getProductNo())) {
            return p;
        }

        return null;

    }

    private Long getLongField(Row row, Map<String, Short> titles, String title) {

        String str = getStringField(row, titles, title);
        if (StringUtils.isBlank(str)) {
            return null;
        }
        long value = NumberUtils.toLong(str);
        return value;
    }

    private String getStringField(Row row, Map<String, Short> titles, String title) {
        Short s = titles.get(title);
        if(null==s)
        {
            return  null;
        }
        Cell c = row.getCell(s);
        String str = null;
        if(c.getCellType() == CellType.NUMERIC)
        {
            DecimalFormat df = new DecimalFormat("#");
            str = df.format(c.getNumericCellValue());
        }
        else
        {
            str = c.getStringCellValue();
        }

        return str;
    }

    private Map<String, Short> readTitles(Row row) {
        if (row == null) {
            return null;
        }
        short first = row.getFirstCellNum();
        short last = row.getLastCellNum();
        Map<String, Short> titles = new HashMap<>();

        for (short i = first; i <= last; i++) {
            Cell c = row.getCell(i);
            if (c == null) {
                break;
            }
            titles.put(StringUtils.trimToEmpty(c.getStringCellValue()), i);
        }
        return titles;
    }

    public static Workbook getWorkBook(MultipartFile file) {
        //获得文件名
        String fileName = file.getOriginalFilename();
        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {
            //获取excel文件的io流
            InputStream is = file.getInputStream();
            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if (fileName.endsWith(xls)) {
                //2003
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(xlsx)) {
                //2007
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {

        }
        return workbook;
    }
}
