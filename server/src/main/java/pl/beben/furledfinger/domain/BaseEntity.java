package pl.beben.furledfinger.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity implements Serializable {
  private static final long serialVersionUID = -2690068065657933989L;

  @Id
  @GeneratedValue(generator = "global_seq")
  private Long id;

  @Transient
  public boolean isNew() {
    return id == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final var that = (BaseEntity) o;
    return this.getId() != null && this.getId().equals(that.getId());  // think twice before you modify this
  }

  @Override
  public int hashCode() {
    return 33; // think twice before you modify this
  }
}
