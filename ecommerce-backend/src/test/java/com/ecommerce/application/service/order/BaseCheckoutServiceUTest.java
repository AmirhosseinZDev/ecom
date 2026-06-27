package com.ecommerce.application.service.order;

import com.ecommerce.application.service.address.AddressService;
import com.ecommerce.application.service.shipping.ShippingCalculator;
import com.ecommerce.persistence.entity.CartItem;
import com.ecommerce.persistence.entity.Order;
import com.ecommerce.persistence.entity.Price;
import com.ecommerce.persistence.entity.Product;
import com.ecommerce.persistence.entity.UserAddress;
import com.ecommerce.persistence.entity.enumeration.ProductStatus;
import com.ecommerce.persistence.entity.enumeration.Province;
import com.ecommerce.persistence.entity.enumeration.VariantType;
import com.ecommerce.persistence.repository.AppUserRepository;
import com.ecommerce.persistence.repository.CartItemRepository;
import com.ecommerce.persistence.repository.OrderRepository;
import com.ecommerce.persistence.repository.ProductRepository;
import com.ecommerce.persistence.repository.UserAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
abstract class BaseCheckoutServiceUTest {

    protected static final Long USER_ID = 7L;
    protected static final Long PRODUCT_ID = 100L;
    protected static final Long ADDRESS_ID = 55L;

    @Mock
    protected CartItemRepository cartItemRepository;
    @Mock
    protected ProductRepository productRepository;
    @Mock
    protected UserAddressRepository userAddressRepository;
    @Mock
    protected OrderRepository orderRepository;
    @Mock
    protected ShippingCalculator shippingCalculator;
    @Mock
    protected AppUserRepository appUserRepository;
    @Mock
    protected PasswordEncoder passwordEncoder;
    @Mock
    protected AddressService addressService;

    protected CheckoutService checkoutService;

    @BeforeEach
    void baseSetUp() {
        checkoutService = new CheckoutService(cartItemRepository, productRepository, userAddressRepository,
                orderRepository, new OrderMapperImpl(), shippingCalculator, appUserRepository, passwordEncoder,
                addressService);
        lenient().when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    protected Product product(int inventory, int weightGram, ProductStatus status) {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setName("Laptop");
        product.setCode("1-1");
        product.setStatus(status);
        product.setInventoryCount(inventory);
        product.setWeightGram(weightGram);
        Price price = new Price();
        price.setVariantType(VariantType.COLOR);
        price.setPrice(BigDecimal.valueOf(100));
        product.setPrices(new ArrayList<>(List.of(price)));
        return product;
    }

    protected CartItem cartItem(int quantity, BigDecimal unitPrice, BigDecimal discountPrice) {
        CartItem item = new CartItem();
        item.setId(10L);
        item.setUserId(USER_ID);
        item.setProductId(PRODUCT_ID);
        item.setVariantType(VariantType.COLOR);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setDiscountPrice(discountPrice);
        return item;
    }

    protected UserAddress address() {
        UserAddress address = new UserAddress();
        address.setId(ADDRESS_ID);
        address.setUserId(USER_ID);
        address.setRecipientFirstName("Ali");
        address.setRecipientLastName("Rezaei");
        address.setRecipientMobile("09120000000");
        address.setProvince(Province.TEHRAN);
        address.setCity("Tehran");
        address.setPostalCode("1234567890");
        address.setAddressLine("Valiasr St");
        return address;
    }
}
