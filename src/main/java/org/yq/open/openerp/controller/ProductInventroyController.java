package org.yq.open.openerp.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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
import org.yq.open.openerp.repository.UserRepository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
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

    @Autowired
    UserRepository userRepository;


    @RequestMapping("/list")
    public Map<String, Object> list(@RequestParam(name = "pageNumber", required = false) Integer pageNumber, @RequestParam(name = "pageSize", required = false) Integer pageSize,
                                    @RequestParam(name = "searchType", required = false) String searchType, @RequestParam(name = "searchValue", required = false) String searchValue,
                                    HttpSession session) {
        User u = (User) session.getAttribute("user");

        Specification<ProductInventory> spec = new Specification<ProductInventory>() {

            @Override
            public Predicate toPredicate(Root<ProductInventory> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate pu = null;
                if (null != u) {
                    if ("1".equals(u.getType())) {
                        pu = cb.equal(root.get("userId").as(String.class), u.getId());
                    }
                }
                if (StringUtils.isNotBlank(searchType) && StringUtils.isNotBlank(searchValue)) {


                    if (StringUtils.equalsIgnoreCase(searchType, "all")) {
                        Predicate p1 = cb.like(root.get("productNo").as(String.class), "%" + searchValue + "%");
                        Predicate p2 = cb.like(root.get("manufacturer").as(String.class), "%" + searchValue + "%");
                        Predicate p3 = cb.like(root.get("name").as(String.class), "%" + searchValue + "%");

                        if (null == pu) {
                            query.where(cb.or(cb.or(p1, p2), p3));
                        } else {
                            query.where(cb.and(pu, cb.or(cb.or(p1, p2), p3)));
                        }

                    } else {
                        if (null == pu) {
                            query.where(cb.like(root.get(searchType).as(String.class), "%" + searchValue + "%"));
                        } else {
                            query.where(cb.and(pu, cb.like(root.get(searchType).as(String.class), "%" + searchValue + "%")));
                        }

                    }
                } else {
                    if (null != pu) {
                        query.where(pu);
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


        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "createDate", "id");

        Map<String, User> userMap = new HashMap<>();
        Page<ProductInventory> ls = productInventoryRepository.findAll(spec, pageable);

        if (null != ls) {
            List<ProductInventory> cls = ls.getContent();
            if (null != cls) {
                for (ProductInventory cl : cls) {
                    User tempUser = getUser(cl.getUserId(), userMap);
                    if (null != tempUser) {
                        cl.setUserName(tempUser.getAccount());
                    }
                }
            }
        }
        return toSuccess(ls);
    }


    @RequestMapping(path = "/exportPro", method = RequestMethod.POST)
    public void exportPro(HttpSession session, HttpServletResponse resp) throws IOException {
        User u = (User) session.getAttribute("user");
        ProductInventory p = new ProductInventory();
        if (null != u && "1".equals(u.getType())) {
            p.setUserId(u.getId());
        }
        Example<ProductInventory> ex = Example.of(p);
        List<ProductInventory> list = productInventoryRepository.findAll(ex, Sort.by(Sort.Direction.DESC, "createDate"));

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("库存信息");
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        Row row = sheet.createRow(0);
        int i = 0;
        createCell(style, row, i++, "物资编码");
        createCell(style, row, i++, "物资名称");
        createCell(style, row, i++, "单位");
        createCell(style, row, i++, "单位成本");
        createCell(style, row, i++, "存放位置");
        createCell(style, row, i++, "库存值");
        createCell(style, row, i++, "型号");
        createCell(style, row, i++, "规格");
        createCell(style, row, i++, "说明");
        createCell(style, row, i++, "生产厂商");
        createCell(style, row, i++, "厂家联系电话");
        createCell(style, row, i++, "用途");
        createCell(style, row, i++, "备用");
        if (null != u && "1".equals(u.getType())) {

        } else {
            createCell(style, row, i++, "所属账号");
        }
        if (null != list) {
            int r = 1;
            Map<String, User> userMap = new HashMap<>();
            for (ProductInventory pro : list) {
                Row rowValue = sheet.createRow(r++);
                int cv = 0;
                createCell(style, rowValue, cv++, Objects.toString(pro.getProductNo(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getName(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getUnit(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getUnitCost(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getAddress(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getInventoryNum(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getProductModel(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getSpec(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getDesc(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getManufacturer(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getmTeleNumber(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getPurpose(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getStandby(), ""));
                if (null != u && "1".equals(u.getType())) {

                } else {
                    User tempUser = getUser(pro.getUserId(), userMap);
                    String userAccount = "";
                    if (null != tempUser) {
                        userAccount = tempUser.getAccount();
                    }
                    createCell(style, rowValue, cv++, userAccount);
                }

            }
        }

        String fileName = new String("库存.xlsx".getBytes(), "iso8859-1");
        OutputStream out = resp.getOutputStream();

        resp.setHeader("content-type", "application/octet-stream");
        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        wb.write(out);

        out.flush();


    }

    @RequestMapping(path = "/exportActionRecord", method = RequestMethod.POST)
    public void exportActionRecord(HttpSession session, HttpServletResponse resp) throws IOException {
        User u = (User) session.getAttribute("user");
        ProductInventory p = new ProductInventory();
        if (null != u && "1".equals(u.getType())) {
            p.setUserId(u.getId());
        }
        Example<ProductInventory> ex = Example.of(p);
        List<ProductInventory> list = productInventoryRepository.findAll(ex, Sort.by(Sort.Direction.DESC, "createDate"));

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("库存操作记录");
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Row row = sheet.createRow(0);
        int i = 0;
        createCell(style, row, i++, "物资编码");
        createCell(style, row, i++, "物资名称");
        createCell(style, row, i++, "单位");
        createCell(style, row, i++, "单位成本");
        createCell(style, row, i++, "存放位置");
        createCell(style, row, i++, "库存值");
        createCell(style, row, i++, "型号");
        createCell(style, row, i++, "规格");
        createCell(style, row, i++, "说明");
        createCell(style, row, i++, "生产厂商");
        createCell(style, row, i++, "厂家联系电话");
        createCell(style, row, i++, "用途");
        createCell(style, row, i++, "备用");
        if (null != u && "1".equals(u.getType())) {

        } else {
            createCell(style, row, i++, "所属账号");
        }
        createCell(style, row, i++, "操作类型");
        createCell(style, row, i++, "操作人");
        createCell(style, row, i++, "数量");
        createCell(style, row, i++, "操作前库存");
        createCell(style, row, i++, "操作后库存");
        createCell(style, row, i++, "记录时间");
        createCell(style, row, i++, "备注");
        if (null != list) {
            int r = 1;
            Map<String, User> userMap = new HashMap<>();
            for (ProductInventory pro : list) {
                UseRecord d = new UseRecord();
                d.setUserId(pro.getUserId());
                d.setProductNo(pro.getProductNo());
                d.setProId(pro.getId());
                Example<UseRecord> exu = Example.of(d);
                List<UseRecord> urs = useRecordRepository.findAll(exu, Sort.by(Sort.Direction.ASC, "createDate"));
                Row rowValue = sheet.createRow(r);
                int recordLength = CollectionUtils.isEmpty(urs) ? 1 : urs.size();
                int cv = 0;
                createCell(style, rowValue, cv++, Objects.toString(pro.getProductNo(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getName(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getUnit(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getUnitCost(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getAddress(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getInventoryNum(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getProductModel(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getSpec(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getDesc(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getManufacturer(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getmTeleNumber(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getPurpose(), ""));
                createCell(style, rowValue, cv++, Objects.toString(pro.getStandby(), ""));
                if (null != u && "1".equals(u.getType())) {

                } else {
                    User tempUser = getUser(pro.getUserId(), userMap);
                    String userAccount = "";
                    if (null != tempUser) {
                        userAccount = tempUser.getAccount();
                    }
                    createCell(style, rowValue, cv++, userAccount);
                }
                int endCv = cv;
                if (CollectionUtils.isNotEmpty(urs)) {
                    int addIndex = 0;

                    for (UseRecord ur : urs) {
                        Row recordRowValue = null;
                        if (addIndex == 0) {
                            recordRowValue = rowValue;
                        } else {
                            recordRowValue = sheet.createRow(r + addIndex);
                        }
                        int cellBegin = cv;
                        createCell(style, recordRowValue, cellBegin++, switchActionType(ur.getType()));
                        createCell(style, recordRowValue, cellBegin++, Objects.toString(ur.getUserName(), ""));
                        createCell(style, recordRowValue, cellBegin++, Objects.toString(ur.getNum(), ""));
                        createCell(style, recordRowValue, cellBegin++, Objects.toString(ur.getBeforeNum(), ""));
                        createCell(style, recordRowValue, cellBegin++, Objects.toString(ur.getAfterNum(), ""));
                        createCell(style, recordRowValue, cellBegin++, parseTime(ur.getCreateDate()));
                        createCell(style, recordRowValue, cellBegin++, Objects.toString(ur.getRemark(), ""));
                        addIndex = addIndex + 1;
                    }
                }
                mergCell(r,recordLength,0,endCv,sheet);
                r = r + recordLength;

            }
        }


        String fileName = new String("操作记录.xlsx".getBytes(), "iso8859-1");
        OutputStream out = resp.getOutputStream();

        resp.setHeader("content-type", "application/octet-stream");
        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        wb.write(out);

        out.flush();

    }

    private void mergCell(int beginRow, int rowSize, int i, int endCv, Sheet sheet) {
        if(rowSize<=1)
        {
            return;
        }
        for(int index = i;index<endCv;index++) {
            CellRangeAddress callRangeAddress = new CellRangeAddress(beginRow,beginRow+rowSize-1,index,index);
            sheet.addMergedRegion(callRangeAddress);
        }
    }


    public String switchActionType(String actionType) {
        if ("1".equals(actionType)) {
            return "出库";
        } else {
            return "入库";
        }
    }

    public String parseTime(Date d) {
        if (null != d) {
            return DateFormatUtils.format(d, "yyyy-MM-dd HH:mm:ss");
        } else {
            return "";
        }
    }


    private void createCell(CellStyle style, Row row, int column, String value) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createMergCell(CellStyle style, Row row,int column, String value,Sheet sheet,int beginRow,int rowSize) {
        Cell cell = row.createCell(column);

        CellRangeAddress callRangeAddress = new CellRangeAddress(beginRow,beginRow+rowSize,column,column);
        sheet.addMergedRegion(callRangeAddress);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private User getUser(String userId, Map<String, User> userMap) {
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        User u = userMap.get(userId);
        if (null != u) {
            return u;
        }
        Optional<User> op = userRepository.findById(userId);
        if (!op.isPresent()) {
            u = new User();
            userMap.put(userId, u);
            return u;
        } else {
            u = op.get();
            userMap.put(userId, u);
            return u;
        }
    }

    @RequestMapping("/add")
    public Map<String, Object> add(ProductInventory data, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (null == u) {
            throw new IllegalStateException("SESSION不存在！");
        }
        data.setId(UUID.randomUUID().toString());
        data.setCreateDate(new Date());
        data.setUpdateDate(new Date());
        data.setUserId(u.getId());

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
            param.setUserId(db.getUserId());
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
            data.setBeforeNum(pi.getInventoryNum());
            long rl = subtract(pi.getInventoryNum(), data.getNum());
            if (rl < 0) {
                return toError("-1", "库存不够！");
            }
            pi.setInventoryNum(rl);
            data.setId(UUID.randomUUID().toString());
            data.setProductNo(pi.getProductNo());
            data.setUserId(pi.getUserId());
            data.setAfterNum(pi.getInventoryNum());
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
            data.setBeforeNum(pi.getInventoryNum());
            long rl = add(pi.getInventoryNum(), data.getNum());

            pi.setInventoryNum(rl);
            data.setId(UUID.randomUUID().toString());
            data.setProductNo(pi.getProductNo());
            data.setUserId(pi.getUserId());
            data.setAfterNum(pi.getInventoryNum());
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
    public Map<String, Object> listUseRecord(@RequestParam(name = "pageNumber", required = false) Integer pageNumber, @RequestParam(name = "pageSize", required = false) Integer pageSize, String productId, @RequestParam(name = "userId", required = false) String userId) {

        UseRecord d = new UseRecord();
        d.setProductNo(productId);
        d.setUserId(userId);
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
            Map<String, Object> result = importFileToDB(pis, u);

            return toSuccess(result);

        }

        return toError("-1", "导入失败!");
    }


    private Map<String, Object> importFileToDB(List<ProductInventory> pis, User u) {

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
                addNum = addNum + 1;
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
        Map<String, Object> result = new HashMap<>();
        result.put("addNum", addNum);
        result.put("modifyNum", modifyNum);
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
                continue;
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
        if (null == s) {
            return null;
        }
        Cell c = row.getCell(s);
        if (null == c) {
            return null;
        }
        String str = null;

        if (c.getCellType() == CellType.NUMERIC) {
            DecimalFormat df = new DecimalFormat("#");
            str = df.format(c.getNumericCellValue());
        } else {
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

    @RequestMapping(path = "/downDesc", method = {RequestMethod.GET, RequestMethod.POST})
    public void downDesc(String fileName, HttpServletResponse resp) throws IOException {
        String path = System.getProperty("user.dir") + File.separator + "prodesc";

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = path + File.separator + fileName;
        File file = FileUtils.getFile(filePath);
        OutputStream out = resp.getOutputStream();
        if (file.exists() && file.isFile()) {
            resp.setHeader("content-type", "application/octet-stream");
            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            FileUtils.copyFile(file, out);

        } else {
            resp.setHeader("content-type", "application/octet-stream");
            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-Disposition", "attachment;filename=empty.txt");
            IOUtils.write(new byte[]{}, out);
        }
        out.flush();
    }
}
