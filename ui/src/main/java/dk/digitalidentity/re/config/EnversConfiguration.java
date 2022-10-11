package dk.digitalidentity.re.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = { "dk.digitalidentity.re.dao" }, repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
public class EnversConfiguration {

}