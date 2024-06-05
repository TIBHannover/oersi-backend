package org.sidre.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sidre.api.VocabControllerApi;
import org.sidre.domain.VocabItem;
import org.sidre.dto.VocabBulkBodyDto;
import org.sidre.dto.VocabItemDto;
import org.sidre.service.VocabService;
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
      item.setItemKey(itemDto.getKey());
      item.setParentKey(itemDto.getParentKey());
      item.setPrefLabel(itemDto.getPrefLabels());
      items.add(item);
    }
    vocabService.updateVocab(body.getVocabIdentifier(), items);
    return ResponseEntity.ok().build();
  }
}
