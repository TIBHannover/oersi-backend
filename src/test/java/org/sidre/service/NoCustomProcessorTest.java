package org.sidre.service;

import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.OembedInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"metadata.custom.processor="})
@ElasticsearchServicesMock
class NoCustomProcessorTest {

    @Autowired
    private MetadataCustomProcessor metadataCustomProcessor;

    @Test
    void testNoCustomProcessor() {
        BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
                new HashMap<>(Map.of(
                        "id", "https://www.test.de",
                        "name", "test"
                )
                ), "id");
        OembedInfo oembedInfo = new OembedInfo();
        metadataCustomProcessor.process(data);
        oembedInfo = metadataCustomProcessor.processOembedInfo(oembedInfo, data);
        assertThat(data.getData())
                .isEqualTo(Map.of(
                                "id", "https://www.test.de",
                                "name", "test"
                        )
                );
        assertThat(oembedInfo).isEqualTo(new OembedInfo());
    }

}
