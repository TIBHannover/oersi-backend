package org.oersi;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Configuration
public class WebConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ModelMapper modelMapper() {
    final ModelMapper modelMapper = new ModelMapper();
    addConverters(modelMapper);
    return modelMapper;
  }

  private void addConverters(final ModelMapper modelMapper) {
    modelMapper.addConverter(
        ctx -> ctx.getSource() == null ? null : ctx.getSource().atOffset(ZoneOffset.UTC),
        LocalDateTime.class, OffsetDateTime.class);
    modelMapper.addConverter(
        ctx -> ctx.getSource() == null ? null : ctx.getSource().toLocalDateTime(),
        OffsetDateTime.class, LocalDateTime.class);
  }

}
