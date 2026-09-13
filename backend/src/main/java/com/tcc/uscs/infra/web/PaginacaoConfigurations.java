package com.tcc.uscs.infra.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

@Configuration
@EnableSpringDataWebSupport(
  pageSerializationMode = PageSerializationMode.VIA_DTO
)
public class PaginacaoConfigurations {}
