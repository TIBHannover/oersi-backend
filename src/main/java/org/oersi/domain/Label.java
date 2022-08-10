package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@Table(
  uniqueConstraints = @UniqueConstraint(columnNames = {"language_code", "label_key"})
)
public class Label extends BaseEntity {

  private String groupId;
  @Column(name = "language_code", nullable = false, length = 3)
  private String languageCode;
  @Column(name = "label_key", nullable = false, length = 188)
  private String labelKey;
  private String labelValue;

}
