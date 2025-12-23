package com.per.category.mapper;

import org.springframework.stereotype.Component;

import com.per.category.document.CategoryDocument;
import com.per.category.entity.Category;

@Component
public class CategoryDocumentMapper {

    public CategoryDocument toDocument(Category category) {
        return CategoryDocument.builder()
                .id(category.getId().toString())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .isActive(category.getIsActive())
                .build();
    }
}
