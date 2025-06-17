package org.sidre;

import org.sidre.repository.*;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MockitoBean(types = {
    MetadataRepository.class,
    MetadataEnrichmentRepository.class,
    BackendConfigRepository.class,
    OrganizationInfoRepository.class,
    VocabItemRepository.class,
    ElasticsearchRequestLogRepository.class,
    ElasticsearchOperations.class,
    ElasticsearchStartupApplicationListener.class
})
public @interface ElasticsearchServicesMock {
}
