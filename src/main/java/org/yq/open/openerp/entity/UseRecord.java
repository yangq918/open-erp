package org.yq.open.openerp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_use_record")
public class UseRecord {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id",length = 100)
    private String id;

    @Column(name = "proId")
    private String proId;

    @Column(name="user_id",length = 100)
    private String userId;

    /**
     * 物资编码
     */
    @Column(name = "product_no")
    private String productNo;

    @Column(name = "user_name")
    private String userName;

    @Column(name="type")
    private String type;

    @Column(name="num")
    private Long num;


    @Column(name="before_num")
    private Long beforeNum;

    @Column(name="after_num")
    private Long afterNum;

    @Column(name = "remark")
    private String remark;

    @Column(name = "create_date")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    @Column(name = "update_date")
    private Date updateDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProId() {
        return proId;
    }

    public void setProId(String proId) {
        this.proId = proId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getNum() {
        return num;
    }

    public void setNum(Long num) {
        this.num = num;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductNo() {
        return productNo;
    }

    public void setProductNo(String productNo) {
        this.productNo = productNo;
    }

    public Long getBeforeNum() {
        return beforeNum;
    }

    public void setBeforeNum(Long beforeNum) {
        this.beforeNum = beforeNum;
    }

    public Long getAfterNum() {
        return afterNum;
    }

    public void setAfterNum(Long afterNum) {
        this.afterNum = afterNum;
    }
}
