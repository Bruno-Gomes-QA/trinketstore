package com.reducess.trinketstore.service;

import com.reducess.trinketstore.dto.CreateProductRequest;
import com.reducess.trinketstore.dto.ProductResponse;
import com.reducess.trinketstore.dto.UpdateProductRequest;
import com.reducess.trinketstore.entity.Product;
import com.reducess.trinketstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        // Verifica se já existe um produto com o mesmo slug
        if (productRepository.existsBySlugProduct(request.getSlugProduct())) {
            throw new RuntimeException("Já existe um produto com este slug");
        }

        Product product = new Product();
        product.setNomeProduct(request.getNomeProduct());
        product.setSlugProduct(request.getSlugProduct());
        product.setDescricaoProduct(request.getDescricaoProduct());
        product.setImagemurlProduct(request.getImagemurlProduct());
        product.setCategoriaProduct(request.getCategoriaProduct());
        product.setAtivo(request.getAtivo());

        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugProduct(slug)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts() {
        return productRepository.findByAtivo(true).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategoriaProduct(category).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProductsByCategory(String category) {
        return productRepository.findByCategoriaProductAndAtivo(category, true).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Se está atualizando o slug, verifica se já existe outro produto com esse slug
        if (request.getSlugProduct() != null && !request.getSlugProduct().equals(product.getSlugProduct())) {
            if (productRepository.existsBySlugProduct(request.getSlugProduct())) {
                throw new RuntimeException("Já existe um produto com este slug");
            }
            product.setSlugProduct(request.getSlugProduct());
        }

        if (request.getNomeProduct() != null && !request.getNomeProduct().isBlank()) {
            product.setNomeProduct(request.getNomeProduct());
        }

        if (request.getDescricaoProduct() != null && !request.getDescricaoProduct().isBlank()) {
            product.setDescricaoProduct(request.getDescricaoProduct());
        }

        if (request.getImagemurlProduct() != null && !request.getImagemurlProduct().isBlank()) {
            product.setImagemurlProduct(request.getImagemurlProduct());
        }

        if (request.getCategoriaProduct() != null && !request.getCategoriaProduct().isBlank()) {
            product.setCategoriaProduct(request.getCategoriaProduct());
        }

        if (request.getAtivo() != null) {
            product.setAtivo(request.getAtivo());
        }

        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        product.setAtivo(false);
        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    @Transactional
    public ProductResponse activateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        product.setAtivo(true);
        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                product.getIdProduct(),
                product.getNomeProduct(),
                product.getSlugProduct(),
                product.getDescricaoProduct(),
                product.getImagemurlProduct(),
                product.getCategoriaProduct(),
                product.getAtivo(),
                product.getCreatedAt()
        );
    }
}

