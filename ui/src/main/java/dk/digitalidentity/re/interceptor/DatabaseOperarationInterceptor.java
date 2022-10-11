package dk.digitalidentity.re.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;

import dk.digitalidentity.re.log.EventLoggable;
import dk.digitalidentity.re.log.EventLogger;
import dk.digitalidentity.re.log.LogEvent;

@Aspect
public class DatabaseOperarationInterceptor {

	@Autowired
	private EventLogger eventLogger;

	@Around("execution(* dk.digitalidentity.re.dao.*Dao.save(..))")
	public Object aroundSave(ProceedingJoinPoint joinPoint) throws Throwable {
		if (joinPoint.getArgs().length > 0) {
			Object target = joinPoint.getArgs()[0];

			if (target != null && target instanceof EventLoggable) {
				String idBefore = ((EventLoggable) target).getEntityId();
	
				Object after = joinPoint.proceed();
	
				if (idBefore.equals(((EventLoggable) after).getEntityId())) {
					eventLogger.log(LogEvent.UPDATE, target.getClass().getSimpleName(), ((EventLoggable) after).getEntityId());
				}
				else {
					eventLogger.log(LogEvent.CREATE, target.getClass().getSimpleName(), ((EventLoggable) after).getEntityId());
				}
				
				return after;
			}
		}

		// default behaviour is to do nothing
		return joinPoint.proceed();
	}

	@Before("execution(* dk.digitalidentity.re.dao.*Dao.delete(..)) && args(target)")
	public void beforeDelete(Object target) throws Throwable {
		if (target instanceof EventLoggable) {
			EventLoggable loggable = (EventLoggable) target;
			eventLogger.log(LogEvent.DELETE, target.getClass().getSimpleName(), loggable.getEntityId());
		}
	}
}