package com.reducess.trinketstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long idProduct;
    private String nomeProduct;
    private String slugProduct;
    private String descricaoProduct;
    private String imagemurlProduct;
    private String categoriaProduct;
    private Boolean ativo;
    private OffsetDateTime createdAt;
}
package com.reducess.trinketstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(max = 300, message = "Nome do produto não pode ter mais de 300 caracteres")
    private String nomeProduct;

    @NotBlank(message = "Slug do produto é obrigatório")
    @Size(max = 200, message = "Slug do produto não pode ter mais de 200 caracteres")
    private String slugProduct;

    @NotBlank(message = "Descrição do produto é obrigatória")
    private String descricaoProduct;

    @NotBlank(message = "URL da imagem é obrigatória")
    @Size(max = 600, message = "URL da imagem não pode ter mais de 600 caracteres")
    private String imagemurlProduct;

    @NotBlank(message = "Categoria do produto é obrigatória")
    @Size(max = 100, message = "Categoria não pode ter mais de 100 caracteres")
    private String categoriaProduct;

    @NotNull(message = "Status ativo é obrigatório")
    private Boolean ativo;
}

