package dk.digitalidentity.re;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.autoconfigure.actuate.CloudWatchMetricAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.cache.ElastiCacheAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.jdbc.AmazonRdsDatabaseAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.messaging.MessagingAutoConfiguration;

@SpringBootApplication(scanBasePackages = "dk.digitalidentity")
@EnableAutoConfiguration(exclude = {
		CloudWatchMetricAutoConfiguration.class,
        ElastiCacheAutoConfiguration.class,
        ContextStackAutoConfiguration.class,
        AmazonRdsDatabaseAutoConfiguration.class,
        MessagingAutoConfiguration.class
})
public class RequirementEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(RequirementEngineApplication.class, args);
	}
}
