package com.ecommerce.application.service.product;

import com.ecommerce.application.api.dto.product.ProductRequestDto;
import com.ecommerce.application.api.dto.product.ProductResponseDto;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import com.ecommerce.persistence.repository.BrandRepository;
import com.ecommerce.persistence.repository.CategoryRepository;
import com.ecommerce.persistence.repository.MediaRepository;
import com.ecommerce.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author reza gholamzad
 * @since 6/16/26
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final MediaRepository mediaRepository;
    private final ProductMapper productMapper;

    public ProductResponseDto create(ProductRequestDto requestDto) {
        validateCode(requestDto.getCode(), null);
        validateUrl(requestDto.getUrl(), null);
        validateReferences(requestDto);
        Product product = new Product();
        productMapper.apply(requestDto, product);
        return productMapper.toResponseDto(productRepository.save(product));
    }

    public ProductResponseDto getById(Long id) {
        return productMapper.toResponseDto(findProductOrThrow(id));
    }

    public Page<ProductResponseDto> list(Long categoryId, Long brandId, ProductStatus status, Pageable pageable) {
        return productRepository.findAll(ProductSpecifications.build(categoryId, brandId, status), pageable)
                .map(productMapper::toResponseDto);
    }

    public ProductResponseDto update(Long id, ProductRequestDto requestDto) {
        Product product = findProductOrThrow(id);
        validateCode(requestDto.getCode(), id);
        validateUrl(requestDto.getUrl(), id);
        validateReferences(requestDto);
        productMapper.apply(requestDto, product);
        return productMapper.toResponseDto(productRepository.save(product));
    }

    public void delete(Long id) {
        productRepository.delete(findProductOrThrow(id));
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EcommerceException(ECOMErrorType.PRODUCT_NOT_FOUND));
    }

    private void validateCode(String code, Long excludeId) {
        boolean exists = excludeId == null
                ? productRepository.findByCode(code).isPresent()
                : productRepository.existsByCodeAndIdNot(code, excludeId);
        if (exists) {
            throw new EcommerceException(ECOMErrorType.PRODUCT_CODE_ALREADY_EXISTS);
        }
    }

    private void validateUrl(String url, Long excludeId) {
        boolean exists = excludeId == null
                ? productRepository.findByUrl(url).isPresent()
                : productRepository.existsByUrlAndIdNot(url, excludeId);
        if (exists) {
            throw new EcommerceException(ECOMErrorType.PRODUCT_URL_ALREADY_EXISTS);
        }
    }

    private void validateReferences(ProductRequestDto requestDto) {
        if (!categoryRepository.existsById(requestDto.getCategoryId())) {
            throw new EcommerceException(ECOMErrorType.CATEGORY_NOT_FOUND);
        }
        if (requestDto.getSubCategoryId() != null && !categoryRepository.existsById(requestDto.getSubCategoryId())) {
            throw new EcommerceException(ECOMErrorType.CATEGORY_NOT_FOUND);
        }
        if (requestDto.getBrandId() != null && !brandRepository.existsById(requestDto.getBrandId())) {
            throw new EcommerceException(ECOMErrorType.BRAND_NOT_FOUND);
        }
        Long mediaId = requestDto.getImage() != null ? requestDto.getImage().getMediaId() : null;
        if (mediaId != null && !mediaRepository.existsById(mediaId)) {
            throw new EcommerceException(ECOMErrorType.MEDIA_NOT_FOUND);
        }
    }
}
