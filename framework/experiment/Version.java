/**
 * 
 */
package dbexp.framework.experiment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * @author shirl
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Version {
	String major();
	String minor();
	String build();
	String comment();
}
