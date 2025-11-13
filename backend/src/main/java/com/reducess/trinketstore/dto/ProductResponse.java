package com.reducess.trinketstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Integer idProduct;
    private String nomeProduct;
    private String slugProduct;
    private String descricaoProduct;
    private String imagemurlProduct;
    private String categoriaProduct;
    private Boolean ativo;
    private OffsetDateTime createdAt;
}

