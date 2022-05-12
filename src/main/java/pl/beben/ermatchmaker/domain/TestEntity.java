package pl.beben.ermatchmaker.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
public class TestEntity extends BaseEntity {
  private static final long serialVersionUID = -2158813345324753217L;
  
  String name;
}
