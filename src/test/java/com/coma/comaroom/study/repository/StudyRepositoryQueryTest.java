package com.coma.comaroom.study.repository;

import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class StudyRepositoryQueryTest {
    @Test
    void entityMappingsAndRepositoryQueriesAreValidWithoutDatabaseAccess() {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPackagesToScan("com.coma.comaroom");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.dialect", "org.hibernate.dialect.MySQLDialect",
                "hibernate.boot.allow_jdbc_metadata_access", "false",
                "hibernate.hbm2ddl.auto", "none"));
        factory.afterPropertiesSet();
        try (var entityManager = factory.getObject().createEntityManager()) {
            var repositories = new JpaRepositoryFactory(entityManager);
            assertThat(repositories.getRepository(StudyRepository.class)).isNotNull();
            assertThat(repositories.getRepository(StudyMemberRepository.class)).isNotNull();
            assertThat(repositories.getRepository(StudyWeekRepository.class)).isNotNull();
            assertThat(repositories.getRepository(StudyMaterialRepository.class)).isNotNull();
            assertThat(repositories.getRepository(StudyManagerCandidateRepository.class)).isNotNull();
            assertThat(repositories.getRepository(StudyJoinRequestRepository.class)).isNotNull();
            assertThat(repositories.getRepository(StudyAttendanceSessionRepository.class)).isNotNull();
        } finally {
            factory.destroy();
        }
    }
}
