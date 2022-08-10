package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Entity(name = "pref_label")
public class LocalizedString extends BaseEntity {

  @ElementCollection
  @CollectionTable(name = "localized_string",
      joinColumns = {@JoinColumn(name = "pref_label_id", referencedColumnName = "id")})
  @MapKeyColumn(name = "language_code", length = 5)
  @Column(name = "label")
  private Map<String, String> localizedStrings;

}
