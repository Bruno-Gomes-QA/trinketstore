package com.reducess.trinketstore.repository;
import com.reducess.trinketstore.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface PriceRepository extends JpaRepository<Price, Integer> {
    List<Price> findByProductId(Integer productId);
    List<Price> findByVigentePrice(Boolean vigentePrice);
    List<Price> findByProductIdAndVigentePrice(Integer productId, Boolean vigentePrice);
    Optional<Price> findByProductIdAndVigentePriceTrue(Integer productId);
}
