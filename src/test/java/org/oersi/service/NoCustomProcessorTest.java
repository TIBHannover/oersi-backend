package org.oersi.service;

import org.junit.jupiter.api.Test;
import org.oersi.ElasticsearchServicesMock;
import org.oersi.domain.BackendMetadata;
import org.oersi.domain.OembedInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"metadata.custom.processor="})
@Import(ElasticsearchServicesMock.class)
class NoCustomProcessorTest {

    @Autowired
    private MetadataCustomProcessor metadataCustomProcessor;

    @Test
    void testNoCustomProcessor() {
        BackendMetadata data = MetadataHelper.toMetadata(
                new HashMap<>(Map.of(
                        "id", "https://www.test.de",
                        "name", "test"
                )
                ));
        OembedInfo oembedInfo = new OembedInfo();
        metadataCustomProcessor.process(data);
        oembedInfo = metadataCustomProcessor.processOembedInfo(oembedInfo, data);
        metadataCustomProcessor.postProcess(data);
        assertThat(data.getData())
                .isEqualTo(Map.of(
                                "id", "https://www.test.de",
                                "name", "test"
                        )
                );
        assertThat(oembedInfo).isEqualTo(new OembedInfo());
    }

}
