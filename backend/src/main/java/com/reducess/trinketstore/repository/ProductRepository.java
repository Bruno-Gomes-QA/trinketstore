package com.reducess.trinketstore.repository;

import com.reducess.trinketstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlugProduct(String slugProduct);
    boolean existsBySlugProduct(String slugProduct);
    List<Product> findByAtivo(Boolean ativo);
    List<Product> findByCategoriaProduct(String categoriaProduct);
    List<Product> findByCategoriaProductAndAtivo(String categoriaProduct, Boolean ativo);
}
package com.reducess.trinketstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Long idProduct;

    @Column(name = "nome_product", nullable = false, length = 300)
    private String nomeProduct;

    @Column(name = "slug_product", nullable = false, length = 200, unique = true)
    private String slugProduct;

    @Column(name = "descricao_product", nullable = false, columnDefinition = "TEXT")
    private String descricaoProduct;

    @Column(name = "imagemurl_product", nullable = false, length = 600)
    private String imagemurlProduct;

    @Column(name = "categoria_product", nullable = false, length = 100)
    private String categoriaProduct;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (ativo == null) {
            ativo = true;
        }
    }
}

