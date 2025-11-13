package com.reducess.trinketstore.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_price")
    private Integer idPrice;
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    @Column(name = "amount_price", nullable = false)
    private Integer amountPrice;
    @Column(name = "currency_price", nullable = false, length = 10)
    private String currencyPrice = "BRL";
    @Column(name = "vigente_price", nullable = false)
    private Boolean vigentePrice = true;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
}
