package org.sidre.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.OembedInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Primary
public class MetadataCustomProcessorImpl implements MetadataCustomProcessor {

    @Qualifier("amb")
    private final @NonNull MetadataCustomProcessor ambMetadataProcessor;

    @Value("${metadata.custom.processor}")
    private String customProcessorId;

    private MetadataCustomProcessor getProcessor() {
        if ("amb".equals(customProcessorId)) {
            return ambMetadataProcessor;
        }
        return new MetadataCustomProcessor() {
            @Override
            public void process(BackendMetadata metadata) { /* default processor: do nothing */ }

            @Override
            public OembedInfo processOembedInfo(OembedInfo oembedInfo, BackendMetadata metadata) {
                return oembedInfo;
            }
        };
    }

    @Override
    public void process(BackendMetadata metadata) {
        getProcessor().process(metadata);
    }

    @Override
    public OembedInfo processOembedInfo(OembedInfo oembedInfo, BackendMetadata metadata) {
        return getProcessor().processOembedInfo(oembedInfo, metadata);
    }
}
