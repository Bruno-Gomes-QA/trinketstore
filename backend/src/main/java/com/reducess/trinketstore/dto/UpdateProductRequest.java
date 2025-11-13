package com.reducess.trinketstore.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 300, message = "Nome do produto não pode ter mais de 300 caracteres")
    private String nomeProduct;

    @Size(max = 200, message = "Slug do produto não pode ter mais de 200 caracteres")
    private String slugProduct;

    private String descricaoProduct;

    @Size(max = 600, message = "URL da imagem não pode ter mais de 600 caracteres")
    private String imagemurlProduct;

    @Size(max = 100, message = "Categoria não pode ter mais de 100 caracteres")
    private String categoriaProduct;

    private Boolean ativo;
}

