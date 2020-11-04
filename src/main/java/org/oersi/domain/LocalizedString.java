package org.oersi.domain;

import java.util.Map;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import lombok.Data;

@Data
@Entity(name = "pref_label")
public class LocalizedString {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ElementCollection
  @CollectionTable(name = "localized_string",
      joinColumns = {@JoinColumn(name = "pref_label_id", referencedColumnName = "id")})
  @MapKeyColumn(name = "language_code", length = 5)
  @Column(name = "label")
  private Map<String, String> localizedStrings;

}
