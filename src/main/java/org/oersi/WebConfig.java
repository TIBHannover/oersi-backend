package org.oersi;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.oersi.domain.About;
import org.oersi.domain.Audience;
import org.oersi.domain.Creator;
import org.oersi.domain.LearningResourceType;
import org.oersi.domain.LocalizedString;
import org.oersi.domain.MainEntityOfPage;
import org.oersi.domain.Metadata;
import org.oersi.domain.Provider;
import org.oersi.domain.SourceOrganization;
import org.oersi.dto.AudienceDto;
import org.oersi.dto.LocalizedStringDto;
import org.oersi.dto.MetadataAboutDto;
import org.oersi.dto.MetadataCreatorDto;
import org.oersi.dto.MetadataDto;
import org.oersi.dto.MetadataLearningResourceTypeDto;
import org.oersi.dto.MetadataMainEntityOfPageDto;
import org.oersi.dto.MetadataSourceOrganizationDto;
import org.oersi.dto.ProviderDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

  private static final int CONTEXT_MIN_ITEM_NR = 2;
  private static final String CONTEXT_LANGUAGE_KEY = "@language";

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ModelMapper modelMapper() {
    final ModelMapper modelMapper = new ModelMapper();
    addConverters(modelMapper);
    addEnumMapping(modelMapper);
    addIdMapping(modelMapper);

    modelMapper.typeMap(Metadata.class, MetadataDto.class).addMappings(
        new PropertyMap<>() {
          @Override
          protected void configure() {
            using(ctx -> toContextList((Metadata) ctx.getSource())).map(source, destination.getContext());
          }
        }
    );
    modelMapper.typeMap(MetadataDto.class, Metadata.class).addMappings(
        new PropertyMap<>() {
          @Override
          protected void configure() {
            using(ctx -> getContextUri((MetadataDto) ctx.getSource())).map(source, destination.getContextUri());
            using(ctx -> getContextLanguage((MetadataDto) ctx.getSource())).map(source, destination.getContextLanguage());
          }
        }
    );

    return modelMapper;
  }

  private String getContextUri(final MetadataDto dto) {
    final int contextUriIndex = 0;
    if (dto.getContext() == null || dto.getContext().size() < CONTEXT_MIN_ITEM_NR) {
      return null;
    }
    if (!(dto.getContext().get(contextUriIndex) instanceof String)) {
      throw new IllegalArgumentException("Missing context uri of type String");
    }
    return (String) dto.getContext().get(contextUriIndex);
  }

  private String getContextLanguage(final MetadataDto dto) {
    final int contextLanguageIndex = 1;
    if (dto.getContext() == null || dto.getContext().size() < CONTEXT_MIN_ITEM_NR) {
      return null;
    }
    if (!(dto.getContext().get(contextLanguageIndex) instanceof Map)) {
      throw new IllegalArgumentException("Missing context language");
    }
    Object language = ((Map<?, ?>) dto.getContext().get(contextLanguageIndex)).get(CONTEXT_LANGUAGE_KEY);
    if (!(language instanceof String)) {
      throw new IllegalArgumentException("Missing context language of type String");
    }
    return (String) language;
  }

  private List<Object> toContextList(final Metadata metadata) {
    if (metadata.getContextUri() == null || metadata.getContextLanguage() == null) {
      return null;
    }
    return List.of(metadata.getContextUri(), Map.of(CONTEXT_LANGUAGE_KEY, metadata.getContextLanguage()));
  }

  private void addConverters(final ModelMapper modelMapper) {
    modelMapper.addConverter(
        ctx -> ctx.getSource() == null ? null : ctx.getSource().atOffset(ZoneOffset.UTC),
        LocalDateTime.class, OffsetDateTime.class);
    modelMapper.addConverter(
        ctx -> ctx.getSource() == null ? null : ctx.getSource().toLocalDateTime(),
        OffsetDateTime.class, LocalDateTime.class);
    modelMapper.addConverter(ctx -> {
      LocalizedStringDto result = null;
      if (ctx.getSource() != null && ctx.getSource().getLocalizedStrings() != null) {
        result = new LocalizedStringDto();
        result.putAll(ctx.getSource().getLocalizedStrings());
      }
      return result;
    }, LocalizedString.class, LocalizedStringDto.class);
    Converter<LocalizedStringDto, LocalizedString> labelConverter = ctx -> {
      LocalizedString result = null;
      if (ctx.getSource() != null) {
        result = new LocalizedString();
        result.setLocalizedStrings(new HashMap<>(ctx.getSource()));
      }
      return result;
    };
    modelMapper.addConverter(labelConverter, LocalizedStringDto.class, LocalizedString.class);
    // converter needs to be set directly, otherwise the mapping does not work
    // (I guess, because of the HashMap inheritance)
    modelMapper.typeMap(MetadataAboutDto.class, About.class).addMappings(mapper -> mapper
        .using(labelConverter).map(MetadataAboutDto::getPrefLabel, About::setPrefLabel));
    modelMapper.typeMap(AudienceDto.class, Audience.class).addMappings(mapper -> mapper
        .using(labelConverter).map(AudienceDto::getPrefLabel, Audience::setPrefLabel));
    modelMapper.typeMap(MetadataLearningResourceTypeDto.class, LearningResourceType.class)
        .addMappings(mapper -> mapper.using(labelConverter)
            .map(MetadataLearningResourceTypeDto::getPrefLabel, LearningResourceType::setPrefLabel));
  }

  private void addEnumMapping(final ModelMapper modelMapper) {
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
  }

  private void addIdMapping(final ModelMapper modelMapper) {
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
    modelMapper.typeMap(LearningResourceType.class, MetadataLearningResourceTypeDto.class).addMappings(
        mapper -> mapper.map(LearningResourceType::getIdentifier, MetadataLearningResourceTypeDto::setId));
    modelMapper.typeMap(MetadataLearningResourceTypeDto.class, LearningResourceType.class)
        .addMappings(mapper -> {
          mapper.map(MetadataLearningResourceTypeDto::getId, LearningResourceType::setIdentifier);
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
  }

}
