package org.oersi.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(
  uniqueConstraints = @UniqueConstraint(columnNames = {"language_code", "label_key"})
)
public class Label {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String groupId;
  @Column(name = "language_code", nullable = false, length = 3)
  private String languageCode;
  @Column(name = "label_key", nullable = false, length = 188)
  private String labelKey;
  private String labelValue;

}
