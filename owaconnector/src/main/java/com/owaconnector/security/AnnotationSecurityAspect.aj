package com.owaconnector.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.intercept.aspectj.AspectJCallback;
import org.springframework.security.access.intercept.aspectj.AspectJSecurityInterceptor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

/**
 * Concrete AspectJ transaction aspect using Spring Security @Secured annotation
 * for JDK 1.5+.
 * 
 * <p>
 * When using this aspect, you <i>must</i> annotate the implementation class
 * (and/or methods within that class), <i>not</i> the interface (if any) that
 * the class implements. AspectJ follows Java's rule that annotations on
 * interfaces are <i>not</i> inherited. This will vary from Spring AOP.
 * 
 * @author Mike Wiesner
 * @since 1.0
 * @version $Id$
 */
public aspect AnnotationSecurityAspect implements InitializingBean {

	/**
	 * Matches the execution of any public method in a type with the Secured
	 * annotation, or any subtype of a type with the Secured annotation.
	 */
	private pointcut executionOfAnyPublicMethodInAtSecuredType() :
        execution(public * ((@Secured *)+).*(..)) && @this(Secured);

	/**
	 * Matches the execution of any method with the Secured annotation.
	 */
	private pointcut executionOfSecuredMethod() :
        execution(* *(..)) && @annotation(Secured);

	/**
	 * Matches the execution of any public method in a type with the
	 * PreAuthorize annotation, or any subtybe of a type with the PreAuthorize
	 * annotation.
	 */
	private pointcut executionOfAnyPublicMethodInAtPreAuthorizedType() :
        execution(public * ((@PreAuthorize *)+).*(..)) && @this(PreAuthorize);

	/**
	 * Matches the execution of any method with the PreAuthorize annotation.
	 */
	private pointcut executionOfPreAuthorizedMethod() :
        execution(* *(..)) && @annotation(PreAuthorize);

	/**
	 * Matches the execution of any public method in a type with the
	 * PostAuthorize annotation, or any subtybe of a type with the PostAuthorize
	 * annotation.
	 */
	private pointcut executionOfAnyPublicMethodInAtPostAuthorizeType() :
        execution(public * ((@PostAuthorize *)+).*(..)) && @this(PostAuthorize);

	/**
	 * Matches the execution of any method with the PostAuthorize annotation.
	 */
	private pointcut executionOfPostAuthorizeMethod() :
        execution(* *(..)) && @annotation(PostAuthorize);

	/**
	 * Matches the execution of any public method in a type with the PreFilter
	 * annotation, or any subtybe of a type with the PreFilter annotation.
	 */
	private pointcut executionOfAnyPublicMethodInAtPreFilterType() :
        execution(public * ((@PreFilter *)+).*(..)) && @this(PreFilter);

	/**
	 * Matches the execution of any method with the PreAuthorize annotation.
	 */
	private pointcut executionOfPreFilterMethod() :
        execution(* *(..)) && @annotation(PreFilter);

	/**
	 * Matches the execution of any public method in a type with the PostFilter
	 * annotation, or any subtybe of a type with the PostFilter annotation.
	 */
	private pointcut executionOfAnyPublicMethodInAtPostFilterType() :
        execution(public * ((@PostFilter *)+).*(..)) && @this(PostFilter);

	/**
	 * Matches the execution of any method with the PostFilter annotation.
	 */
	private pointcut executionOfPostFilterMethod() :
        execution(* *(..)) && @annotation(PostFilter);

	private pointcut securedMethodExecution() :
        executionOfAnyPublicMethodInAtSecuredType() ||
        executionOfSecuredMethod() ||
        executionOfAnyPublicMethodInAtPreAuthorizedType() ||
        executionOfPreAuthorizedMethod() ||
        executionOfAnyPublicMethodInAtPostAuthorizeType() ||
        executionOfPostAuthorizeMethod() ||
        executionOfAnyPublicMethodInAtPreFilterType() ||
        executionOfPreFilterMethod() ||
        executionOfAnyPublicMethodInAtPostFilterType() ||
        executionOfPostFilterMethod();

	private AspectJSecurityInterceptor securityInterceptor;

	Object around(): securedMethodExecution() {
		if (this.securityInterceptor == null) {
			return proceed();
		}

		AspectJCallback callback = new AspectJCallback() {
			public Object proceedWithObject() {
				return proceed();
			}
		};

		return this.securityInterceptor.invoke(thisJoinPoint, callback);
	}

	public void setSecurityInterceptor(
			AspectJSecurityInterceptor securityInterceptor) {
		this.securityInterceptor = securityInterceptor;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.securityInterceptor == null)
			throw new IllegalArgumentException("securityInterceptor required");
	}
}
