package com.finance.tracker.category.mapper;

import com.finance.tracker.category.domain.dtos.CategoryDto;
import com.finance.tracker.category.domain.entities.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "createdAt", expression = "java(com.finance.tracker.accounts.utils.DateTimeMapper.toLocalDateTime(category.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(com.finance.tracker.accounts.utils.DateTimeMapper.toLocalDateTime(category.getUpdatedAt()))")
    @Mapping(target = "categoryType", source = "type")
    CategoryDto toDto(Category category);
}
