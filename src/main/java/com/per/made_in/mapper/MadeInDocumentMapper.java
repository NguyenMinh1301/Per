package com.per.made_in.mapper;

import org.springframework.stereotype.Component;

import com.per.made_in.document.MadeInDocument;
import com.per.made_in.entity.MadeIn;

@Component
public class MadeInDocumentMapper {

    public MadeInDocument toDocument(MadeIn madeIn) {
        return MadeInDocument.builder()
                .id(madeIn.getId().toString())
                .name(madeIn.getName())
                .isoCode(madeIn.getIsoCode())
                .region(madeIn.getRegion())
                .description(madeIn.getDescription())
                .imageUrl(madeIn.getImageUrl())
                .isActive(madeIn.isActive())
                .build();
    }
}
