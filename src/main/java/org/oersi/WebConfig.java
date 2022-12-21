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
import org.oersi.domain.*;
import org.oersi.dto.*;
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
            using(ctx -> toContextList((Metadata) ctx.getSource())).map(source, destination.getAtContext());
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
    if (dto.getAtContext() == null || dto.getAtContext().size() < CONTEXT_MIN_ITEM_NR) {
      return null;
    }
    if (!(dto.getAtContext().get(contextUriIndex) instanceof String)) {
      throw new IllegalArgumentException("Missing context uri of type String");
    }
    return (String) dto.getAtContext().get(contextUriIndex);
  }

  private String getContextLanguage(final MetadataDto dto) {
    final int contextLanguageIndex = 1;
    if (dto.getAtContext() == null || dto.getAtContext().size() < CONTEXT_MIN_ITEM_NR) {
      return null;
    }
    if (!(dto.getAtContext().get(contextLanguageIndex) instanceof Map)) {
      throw new IllegalArgumentException("Missing context language");
    }
    Object language = ((Map<?, ?>) dto.getAtContext().get(contextLanguageIndex)).get(CONTEXT_LANGUAGE_KEY);
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
    modelMapper.typeMap(LabelDefinitionDto.class, LabelDefinition.class).addMappings(mapper -> mapper
      .using(labelConverter).map(LabelDefinitionDto::getLabel, LabelDefinition::setLabel));
    modelMapper.typeMap(ConditionsOfAccessDto.class, ConditionsOfAccess.class).addMappings(mapper -> mapper
      .using(labelConverter).map(ConditionsOfAccessDto::getPrefLabel, ConditionsOfAccess::setPrefLabel));
    modelMapper.typeMap(LabelledConceptDto.class, LabelledConcept.class).addMappings(mapper -> mapper
      .using(labelConverter).map(LabelledConceptDto::getPrefLabel, LabelledConcept::setPrefLabel));
  }

  private void addEnumMapping(final ModelMapper modelMapper) {
    // map enums <-> strings
    modelMapper.addConverter(
        ctx -> ctx.getSource() == null ? null
            : LanguageDto.fromValue(ctx.getSource()),
        String.class, LanguageDto.class);
    modelMapper.addConverter(ctx -> ctx.getSource() == null ? null : ctx.getSource().toString(),
      LanguageDto.class, String.class);
    modelMapper.addConverter(
        ctx -> ctx.getSource() == null ? null
            : MetadataCreatorDto.TypeEnum.fromValue(ctx.getSource()),
        String.class, MetadataCreatorDto.TypeEnum.class);
    modelMapper.addConverter(ctx -> ctx.getSource() == null ? null : ctx.getSource().toString(),
        MetadataCreatorDto.TypeEnum.class, String.class);
    modelMapper.addConverter(
      ctx -> ctx.getSource() == null ? null
        : MediaObjectDto.TypeEnum.fromValue(ctx.getSource()),
      String.class, MediaObjectDto.TypeEnum.class);
    modelMapper.addConverter(ctx -> ctx.getSource() == null ? null : ctx.getSource().toString(),
      MediaObjectDto.TypeEnum.class, String.class);
    modelMapper.addConverter(
      ctx -> ctx.getSource() == null ? null
        : CaptionDto.TypeEnum.fromValue(ctx.getSource()),
      String.class, CaptionDto.TypeEnum.class);
    modelMapper.addConverter(ctx -> ctx.getSource() == null ? null : ctx.getSource().toString(),
      CaptionDto.TypeEnum.class, String.class);
    modelMapper.addConverter(
      ctx -> ctx.getSource() == null ? null
        : TrailerDto.TypeEnum.fromValue(ctx.getSource()),
      String.class, TrailerDto.TypeEnum.class);
    modelMapper.addConverter(ctx -> ctx.getSource() == null ? null : ctx.getSource().toString(),
      TrailerDto.TypeEnum.class, String.class);
  }

  private void addIdMapping(final ModelMapper modelMapper) {
    // map DTO id field <-> Domain identifier field
    // ATTENTION: consider order of definitions (a mapping m that is used in another mapping n has
    // to be defined before n)
    modelMapper.typeMap(LabelledConcept.class, LabelledConceptDto.class)
      .addMappings(mapper -> mapper.map(LabelledConcept::getIdentifier, LabelledConceptDto::setId));
    modelMapper.typeMap(LabelledConceptDto.class, LabelledConcept.class).addMappings(mapper -> {
      mapper.map(LabelledConceptDto::getId, LabelledConcept::setIdentifier);
      mapper.skip(LabelledConcept::setId);
    });
    List.of(About.class, Assesses.class, Audience.class, CompetencyRequired.class, EducationalLevel.class, LearningResourceType.class, Teaches.class)
      .forEach(labelledConceptClazz -> {
        modelMapper.typeMap(labelledConceptClazz, LabelledConceptDto.class).includeBase(LabelledConcept.class, LabelledConceptDto.class);
        modelMapper.typeMap(LabelledConceptDto.class, labelledConceptClazz).includeBase(LabelledConceptDto.class, LabelledConcept.class);
      });

    modelMapper.typeMap(Affiliation.class, AffiliationDto.class)
        .addMappings(mapper -> mapper.map(Affiliation::getIdentifier, AffiliationDto::setId));
    modelMapper.typeMap(AffiliationDto.class, Affiliation.class).addMappings(mapper -> {
      mapper.map(AffiliationDto::getId, Affiliation::setIdentifier);
      mapper.skip(Affiliation::setId);
    });
    modelMapper.typeMap(Caption.class, CaptionDto.class)
      .addMappings(mapper -> mapper.map(Caption::getIdentifier, CaptionDto::setId));
    modelMapper.typeMap(CaptionDto.class, Caption.class).addMappings(mapper -> {
      mapper.map(CaptionDto::getId, Caption::setIdentifier);
      mapper.skip(Caption::setId);
    });
    modelMapper.typeMap(ConditionsOfAccess.class, ConditionsOfAccessDto.class)
      .addMappings(mapper -> mapper.map(ConditionsOfAccess::getIdentifier, ConditionsOfAccessDto::setId));
    modelMapper.typeMap(ConditionsOfAccessDto.class, ConditionsOfAccess.class).addMappings(mapper -> {
      mapper.map(ConditionsOfAccessDto::getId, ConditionsOfAccess::setIdentifier);
      mapper.skip(ConditionsOfAccess::setId);
    });
    modelMapper.typeMap(Contributor.class, MetadataContributorDto.class)
        .addMappings(mapper -> mapper.map(Contributor::getIdentifier, MetadataContributorDto::setId));
    modelMapper.typeMap(MetadataContributorDto.class, Contributor.class).addMappings(mapper -> {
      mapper.map(MetadataContributorDto::getId, Contributor::setIdentifier);
      mapper.skip(Contributor::setId);
    });
    modelMapper.typeMap(Creator.class, MetadataCreatorDto.class)
      .addMappings(mapper -> mapper.map(Creator::getIdentifier, MetadataCreatorDto::setId));
    modelMapper.typeMap(MetadataCreatorDto.class, Creator.class).addMappings(mapper -> {
      mapper.map(MetadataCreatorDto::getId, Creator::setIdentifier);
      mapper.skip(Creator::setId);
    });
    modelMapper.typeMap(License.class, LicenseDto.class).addMappings(
      mapper -> mapper.map(License::getIdentifier, LicenseDto::setId));
    modelMapper.typeMap(LicenseDto.class, License.class)
      .addMappings(mapper -> {
        mapper.map(LicenseDto::getId, License::setIdentifier);
        mapper.skip(License::setId);
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
    modelMapper.typeMap(Publisher.class, MetadataPublisherDto.class)
      .addMappings(mapper -> mapper.map(Publisher::getIdentifier,
        MetadataPublisherDto::setId));
    modelMapper.typeMap(MetadataPublisherDto.class, Publisher.class)
      .addMappings(mapper -> {
        mapper.map(MetadataPublisherDto::getId, Publisher::setIdentifier);
        mapper.skip(Publisher::setId);
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
