package com.ecommerce.application.controller.product;

import com.ecommerce.application.api.dto.product.ProductRequestDto;
import com.ecommerce.application.api.dto.product.ProductResponseDto;
import com.ecommerce.application.api.dto.product.ProductSearchRequestDto;
import com.ecommerce.application.service.product.ProductService;
import com.ecommerce.application.api.dto.product.enumeration.ImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author reza gholamzad
 * @since 6/16/26
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDto create(
            @RequestPart("data") ProductRequestDto requestDto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "altText", required = false) String altText) {
        return productService.create(requestDto, image, altText);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponseDto getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ProductResponseDto> search(@ModelAttribute ProductSearchRequestDto searchDto, Pageable pageable) {
        return productService.search(searchDto, pageable);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDto update(@PathVariable Long id, @RequestBody ProductRequestDto requestDto) {
        return productService.update(id, requestDto);
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDto uploadImage(
            @PathVariable Long id,
            @RequestParam ImageType type,
            @RequestPart("image") MultipartFile image,
            @RequestParam(value = "altText", required = false) String altText) {
        return productService.uploadImage(id, type, image, altText);
    }

    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeImage(
            @PathVariable Long id,
            @RequestParam ImageType type,
            @RequestParam(required = false) Long imageId) {
        productService.removeImage(id, type, imageId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
