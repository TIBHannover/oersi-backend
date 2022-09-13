package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
public class VocabItem extends BaseEntity {

  private String vocabIdentifier;
  private String key;
  private String parentKey;

}
