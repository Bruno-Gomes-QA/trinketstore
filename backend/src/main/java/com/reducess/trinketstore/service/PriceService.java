package com.reducess.trinketstore.service;
import com.reducess.trinketstore.dto.CreatePriceRequest;
import com.reducess.trinketstore.dto.PriceResponse;
import com.reducess.trinketstore.dto.UpdatePriceRequest;
import com.reducess.trinketstore.entity.Price;
import com.reducess.trinketstore.entity.Product;
import com.reducess.trinketstore.repository.PriceRepository;
import com.reducess.trinketstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class PriceService {
    private final PriceRepository priceRepository;
    private final ProductRepository productRepository;
    @Transactional
    public PriceResponse createPrice(CreatePriceRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        Price price = new Price();
        price.setProductId(request.getProductId());
        price.setAmountPrice(request.getAmountPrice());
        price.setCurrencyPrice(request.getCurrencyPrice());
        price.setVigentePrice(request.getVigentePrice());
        Price savedPrice = priceRepository.save(price);
        if (Boolean.TRUE.equals(savedPrice.getVigentePrice())) {
            deactivateOtherPrices(savedPrice.getProductId(), savedPrice.getIdPrice());
        }
        return mapToPriceResponse(savedPrice, product);
    }
    @Transactional(readOnly = true)
    public PriceResponse getPriceById(Integer id) {
        Price price = priceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preço não encontrado"));
        Product product = productRepository.findById(price.getProductId()).orElse(null);
        return mapToPriceResponse(price, product);
    }
    @Transactional(readOnly = true)
    public List<PriceResponse> getAllPrices() {
        return priceRepository.findAll().stream()
                .map(price -> {
                    Product product = productRepository.findById(price.getProductId()).orElse(null);
                    return mapToPriceResponse(price, product);
                })
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<PriceResponse> getPricesByProductId(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        return priceRepository.findByProductId(productId).stream()
                .map(price -> mapToPriceResponse(price, product))
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<PriceResponse> getActivePrices() {
        return priceRepository.findByVigentePrice(true).stream()
                .map(price -> {
                    Product product = productRepository.findById(price.getProductId()).orElse(null);
                    return mapToPriceResponse(price, product);
                })
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public PriceResponse getCurrentPriceByProductId(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        Price price = priceRepository.findByProductIdAndVigentePriceTrue(productId)
                .orElseThrow(() -> new RuntimeException("Preço vigente não encontrado para este produto"));
        return mapToPriceResponse(price, product);
    }
    @Transactional
    public PriceResponse updatePrice(Integer id, UpdatePriceRequest request) {
        Price price = priceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preço não encontrado"));
        if (request.getAmountPrice() != null) {
            price.setAmountPrice(request.getAmountPrice());
        }
        if (request.getCurrencyPrice() != null && !request.getCurrencyPrice().isBlank()) {
            price.setCurrencyPrice(request.getCurrencyPrice());
        }
        if (request.getVigentePrice() != null) {
            price.setVigentePrice(request.getVigentePrice());
        }
        Price updatedPrice = priceRepository.save(price);
        if (Boolean.TRUE.equals(updatedPrice.getVigentePrice())) {
            deactivateOtherPrices(updatedPrice.getProductId(), updatedPrice.getIdPrice());
        }
        Product product = productRepository.findById(updatedPrice.getProductId()).orElse(null);
        return mapToPriceResponse(updatedPrice, product);
    }
    @Transactional
    public void deletePrice(Integer id) {
        Price price = priceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preço não encontrado"));
        priceRepository.delete(price);
    }
    @Transactional
    public PriceResponse deactivatePrice(Integer id) {
        Price price = priceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preço não encontrado"));
        price.setVigentePrice(false);
        Price updatedPrice = priceRepository.save(price);
        Product product = productRepository.findById(updatedPrice.getProductId()).orElse(null);
        return mapToPriceResponse(updatedPrice, product);
    }
    @Transactional
    public PriceResponse activatePrice(Integer id) {
        Price price = priceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preço não encontrado"));
        price.setVigentePrice(true);
        Price updatedPrice = priceRepository.save(price);
        deactivateOtherPrices(updatedPrice.getProductId(), updatedPrice.getIdPrice());
        Product product = productRepository.findById(updatedPrice.getProductId()).orElse(null);
        return mapToPriceResponse(updatedPrice, product);
    }
    private PriceResponse mapToPriceResponse(Price price, Product product) {
        return new PriceResponse(
                price.getIdPrice(),
                price.getProductId(),
                price.getAmountPrice(),
                price.getCurrencyPrice(),
                price.getVigentePrice(),
                product != null ? product.getNomeProduct() : null
        );
    }

    private void deactivateOtherPrices(Integer productId, Integer exceptionPriceId) {
        List<Price> activePrices = priceRepository.findByProductIdAndVigentePrice(productId, true);
        boolean updated = false;
        for (Price activePrice : activePrices) {
            if (exceptionPriceId != null && activePrice.getIdPrice().equals(exceptionPriceId)) {
                continue;
            }
            activePrice.setVigentePrice(false);
            updated = true;
        }
        if (updated) {
            priceRepository.saveAll(activePrices);
        }
    }
}
