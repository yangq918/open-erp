package org.yq.open.openerp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.yq.open.openerp.entity.ProductInventory;
@Repository
public interface ProductInventoryRepository extends JpaSpecificationExecutor<ProductInventory>, JpaRepository<ProductInventory,String> {


}
