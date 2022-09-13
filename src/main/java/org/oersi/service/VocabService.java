package org.oersi.service;

import org.oersi.domain.VocabItem;

import java.util.List;

public interface VocabService {

  List<VocabItem> updateVocab(String vocabIdentifier, List<VocabItem> items);

}
