package com.ecommerce.application.service.order;

import com.ecommerce.application.api.dto.order.OrderItemResponseDto;
import com.ecommerce.application.api.dto.order.OrderResponseDto;
import com.ecommerce.persistence.entity.Order;
import com.ecommerce.persistence.entity.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface OrderMapper {

    OrderResponseDto toResponseDto(Order order);

    OrderItemResponseDto toItemDto(OrderItem item);
}
