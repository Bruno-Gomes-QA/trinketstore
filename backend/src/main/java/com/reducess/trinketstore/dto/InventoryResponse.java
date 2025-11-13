package com.reducess.trinketstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Integer idInventory;
    private Integer productId;
    private Integer qtyOnHand;
}
package com.reducess.trinketstore.repository;

import com.reducess.trinketstore.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByProductId(Integer productId);
    List<Inventory> findByQtyOnHandGreaterThan(Integer quantity);
    List<Inventory> findByQtyOnHandLessThan(Integer quantity);
    boolean existsByProductId(Integer productId);
}

