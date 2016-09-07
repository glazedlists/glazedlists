/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for configuring the mappings of a Hibernate test class. It's
 * interpreted by a {@link HibernateClassRule} that needs to be defined on the test class as well.
 *
 * @author hbrands
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.TYPE})
public @interface HibernateConfig {

    /**
     * @return the base for the mapping resources
     */
    String baseForMappings() default "ca/odell/glazedlists/hibernate/";

    /**
     * @return the names of the mapping resoures without base path
     */
    String[] mappings();
}
