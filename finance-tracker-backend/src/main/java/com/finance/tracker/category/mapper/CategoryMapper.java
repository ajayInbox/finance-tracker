package com.finance.tracker.category.mapper;

import com.finance.tracker.category.domain.dtos.CategoryDto;
import com.finance.tracker.category.domain.entities.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);
}
