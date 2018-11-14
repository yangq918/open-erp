package org.yq.open.openerp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.yq.open.openerp.entity.ProductInventory;
import org.yq.open.openerp.entity.User;

@Repository
public interface UserRepository extends JpaSpecificationExecutor<ProductInventory>, JpaRepository<User, String> {
}
