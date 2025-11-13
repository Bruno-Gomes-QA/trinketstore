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
    private Integer idProduct;
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
