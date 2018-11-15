package org.yq.open.openerp.entity;

import org.hibernate.annotations.GeneratorType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_product_inventory")
public class ProductInventory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id",length = 100)
    private  String id;


    @Column(name="user_id")
    private Long userId;

    /**
     * 物资编码
     */
    @Column(name = "product_no")
    private String productNo;

    /**
     * 物资名称
     */
    @Column(name = "name")
    private  String name;

    /**
     * 数量
     */
    @Column(name = "num")
    private Long num;


    /**
     * 单位
     */
    @Column(name = "unit")
    private String unit;

    @Column(name="unitCost")
    private String unitCost;


    @Column(name = "address")
    private String address;

    @Column(name = "inventory_num")
    private Long inventoryNum;

    @Column(name = "product_model")
    private String productModel;

    @Column(name = "spec")
    private String  spec;

    @Column(name ="desc")
    private String desc;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "m_tel_number")
    private String mTeleNumber;

    @Column(name = "purpose")
    private String  purpose;


    @Column(name="standby")
    private String standby;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "update_date")
    private Date updateDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductNo() {
        return productNo;
    }

    public void setProductNo(String productNo) {
        this.productNo = productNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNum() {
        return num;
    }

    public void setNum(Long num) {
        this.num = num;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(String unitCost) {
        this.unitCost = unitCost;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getInventoryNum() {
        return inventoryNum;
    }

    public void setInventoryNum(Long inventoryNum) {
        this.inventoryNum = inventoryNum;
    }

    public String getProductModel() {
        return productModel;
    }

    public void setProductModel(String productModel) {
        this.productModel = productModel;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getmTeleNumber() {
        return mTeleNumber;
    }

    public void setmTeleNumber(String mTeleNumber) {
        this.mTeleNumber = mTeleNumber;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStandby() {
        return standby;
    }

    public void setStandby(String standby) {
        this.standby = standby;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
