package eu.tib.oersi;

import eu.tib.oersi.domain.About;
import eu.tib.oersi.domain.Audience;
import eu.tib.oersi.domain.Creator;
import eu.tib.oersi.domain.LearningResourceType;
import eu.tib.oersi.domain.MainEntityOfPage;
import eu.tib.oersi.domain.Metadata;
import eu.tib.oersi.domain.Provider;
import eu.tib.oersi.domain.SourceOrganization;
import eu.tib.oersi.dto.AudienceDto;
import eu.tib.oersi.dto.LearningResourceTypeDto;
import eu.tib.oersi.dto.MetadataAboutDto;
import eu.tib.oersi.dto.MetadataCreatorDto;
import eu.tib.oersi.dto.MetadataDto;
import eu.tib.oersi.dto.MetadataMainEntityOfPageDto;
import eu.tib.oersi.dto.MetadataSourceOrganizationDto;
import eu.tib.oersi.dto.ProviderDto;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ModelMapper modelMapper() {
    final ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(
        ctx -> ctx.getSource() == null ? null : ctx.getSource().atOffset(ZoneOffset.UTC),
        LocalDateTime.class, OffsetDateTime.class);
    modelMapper.addConverter(
        ctx -> ctx.getSource() == null ? null : ctx.getSource().toLocalDateTime(),
        OffsetDateTime.class, LocalDateTime.class);

    // map enums <-> strings
    modelMapper.addConverter(
        ctx -> ctx.getSource() == null ? null
            : MetadataDto.InLanguageEnum.fromValue(ctx.getSource()),
        String.class, MetadataDto.InLanguageEnum.class);
    modelMapper.addConverter(ctx -> ctx.getSource() == null ? null : ctx.getSource().toString(),
        MetadataDto.InLanguageEnum.class, String.class);
    modelMapper.addConverter(
        ctx -> ctx.getSource() == null ? null
            : MetadataCreatorDto.TypeEnum.fromValue(ctx.getSource()),
        String.class, MetadataCreatorDto.TypeEnum.class);
    modelMapper.addConverter(ctx -> ctx.getSource() == null ? null : ctx.getSource().toString(),
        MetadataCreatorDto.TypeEnum.class, String.class);

    // map DTO id field <-> Domain identifier field
    // ATTENTION: consider order of definitions (a mapping m that is used in another mapping n has
    // to be defined before n)
    modelMapper.typeMap(About.class, MetadataAboutDto.class)
        .addMappings(mapper -> mapper.map(About::getIdentifier, MetadataAboutDto::setId));
    modelMapper.typeMap(MetadataAboutDto.class, About.class).addMappings(mapper -> {
      mapper.map(MetadataAboutDto::getId, About::setIdentifier);
      mapper.skip(About::setId);
    });
    modelMapper.typeMap(Audience.class, AudienceDto.class)
        .addMappings(mapper -> mapper.map(Audience::getIdentifier, AudienceDto::setId));
    modelMapper.typeMap(AudienceDto.class, Audience.class).addMappings(mapper -> {
      mapper.map(AudienceDto::getId, Audience::setIdentifier);
      mapper.skip(Audience::setId);
    });
    modelMapper.typeMap(Creator.class, MetadataCreatorDto.class)
        .addMappings(mapper -> mapper.map(Creator::getIdentifier, MetadataCreatorDto::setId));
    modelMapper.typeMap(MetadataCreatorDto.class, Creator.class).addMappings(mapper -> {
      mapper.map(MetadataCreatorDto::getId, Creator::setIdentifier);
      mapper.skip(Creator::setId);
    });
    modelMapper.typeMap(LearningResourceType.class, LearningResourceTypeDto.class).addMappings(
        mapper -> mapper.map(LearningResourceType::getIdentifier, LearningResourceTypeDto::setId));
    modelMapper.typeMap(LearningResourceTypeDto.class, LearningResourceType.class)
        .addMappings(mapper -> {
          mapper.map(LearningResourceTypeDto::getId, LearningResourceType::setIdentifier);
          mapper.skip(LearningResourceType::setId);
        });
    modelMapper.typeMap(Provider.class, ProviderDto.class)
        .addMappings(mapper -> mapper.map(Provider::getIdentifier, ProviderDto::setId));
    modelMapper.typeMap(ProviderDto.class, Provider.class).addMappings(mapper -> {
      mapper.map(ProviderDto::getId, Provider::setIdentifier);
      mapper.skip(Provider::setId);
    });
    modelMapper.typeMap(MainEntityOfPage.class, MetadataMainEntityOfPageDto.class).addMappings(
        mapper -> mapper.map(MainEntityOfPage::getIdentifier, MetadataMainEntityOfPageDto::setId));
    modelMapper.typeMap(MetadataMainEntityOfPageDto.class, MainEntityOfPage.class)
        .addMappings(mapper -> {
          mapper.map(MetadataMainEntityOfPageDto::getId, MainEntityOfPage::setIdentifier);
          mapper.skip(MainEntityOfPage::setId);
        });
    modelMapper.typeMap(SourceOrganization.class, MetadataSourceOrganizationDto.class)
        .addMappings(mapper -> mapper.map(SourceOrganization::getIdentifier,
            MetadataSourceOrganizationDto::setId));
    modelMapper.typeMap(MetadataSourceOrganizationDto.class, SourceOrganization.class)
        .addMappings(mapper -> {
          mapper.map(MetadataSourceOrganizationDto::getId, SourceOrganization::setIdentifier);
          mapper.skip(SourceOrganization::setId);
        });
    modelMapper.typeMap(Metadata.class, MetadataDto.class)
        .addMappings(mapper -> mapper.map(Metadata::getIdentifier, MetadataDto::setId));
    modelMapper.typeMap(MetadataDto.class, Metadata.class).addMappings(mapper -> {
      mapper.map(MetadataDto::getId, Metadata::setIdentifier);
      mapper.skip(Metadata::setId);
    });

    return modelMapper;
  }
}
