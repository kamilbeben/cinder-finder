package pl.beben.ermatchmaker.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.beben.ermatchmaker.domain.TestEntity;

@Repository
public interface TestEntityRepository extends CrudRepository<TestEntity, Long> {
}
