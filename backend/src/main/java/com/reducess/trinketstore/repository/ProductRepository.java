package com.reducess.trinketstore.repository;

import com.reducess.trinketstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findBySlugProduct(String slugProduct);
    boolean existsBySlugProduct(String slugProduct);
    List<Product> findByAtivo(Boolean ativo);
    List<Product> findByCategoriaProduct(String categoriaProduct);
    List<Product> findByCategoriaProductAndAtivo(String categoriaProduct, Boolean ativo);
}

