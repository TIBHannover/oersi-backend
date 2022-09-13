package org.oersi.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.api.VocabControllerApi;
import org.oersi.domain.VocabItem;
import org.oersi.dto.VocabBulkBodyDto;
import org.oersi.dto.VocabItemDto;
import org.oersi.service.VocabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VocabController implements VocabControllerApi {

  private final @NonNull VocabService vocabService;

  @Override
  public ResponseEntity<Void> createOrUpdateMany(VocabBulkBodyDto body) {
    List<VocabItem> items = new ArrayList<>();
    for (VocabItemDto itemDto: body.getItems()) {
      VocabItem item = new VocabItem();
      item.setVocabIdentifier(body.getVocabIdentifier());
      item.setKey(itemDto.getKey());
      item.setParentKey(itemDto.getParentKey());
      items.add(item);
    }
    vocabService.updateVocab(body.getVocabIdentifier(), items);
    return ResponseEntity.ok().build();
  }
}
