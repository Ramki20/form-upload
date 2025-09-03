package gov.usda.fsa.fcao.flp.flpids.common.dao.impl.support;

import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.Query;
import org.hibernate.query.spi.QueryImplementor;

/**
 * Hibernate 6.x compatible HQL to SQL translator
 * Note: Criteria API translation is no longer supported due to architectural changes
 */
public class HibernateHqlAndCriteriaToSqlTranslator {
    private SessionFactory sessionFactory;
    
    public void setSessionFactory(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * Criteria to SQL translation is no longer supported in Hibernate 6.x
     * The legacy Criteria API has been completely removed.
     * Use JPA Criteria API or HQL instead.
     */
    @Deprecated
    public String toSql(Object criteria){
        throw new UnsupportedOperationException(
            "Criteria to SQL translation is not supported in Hibernate 6.x. " +
            "The legacy Criteria API has been removed. Use HQL or JPA Criteria API instead."
        );
    }
    
    /**
     * Convert HQL query to SQL
     * @param hqlQueryText HQL query string
     * @return Generated SQL string
     */
    public String toSql(String hqlQueryText){
        if (hqlQueryText != null && hqlQueryText.trim().length() > 0){
            try {
                SessionFactoryImplementor factory = (SessionFactoryImplementor) sessionFactory;
                
                // Create a query to get the SQL
                Query<?> query = factory.getCurrentSession().createQuery(hqlQueryText);
                
                if (query instanceof QueryImplementor) {
                    QueryImplementor<?> queryImpl = (QueryImplementor<?>) query;
                    // In Hibernate 6, getting SQL from queries is more complex
                    // This is a simplified approach - you might need to implement
                    // more sophisticated SQL extraction based on your specific needs
                    
                    // Note: Direct SQL extraction from queries in Hibernate 6.x
                    // is not as straightforward as in earlier versions
                    // Consider using database query logging instead
                    
                    return "SQL extraction not directly available in Hibernate 6.x - enable SQL logging";
                }
                
                return "Unable to extract SQL from query";
            } catch (Exception e) {
                throw new RuntimeException("Error extracting SQL from HQL: " + e.getMessage(), e);
            }
        }
        return null;
    }
    
    /**
     * Alternative method to get SQL by enabling Hibernate SQL logging
     * Add this to your application.properties:
     * 
     * logging.level.org.hibernate.SQL=DEBUG
     * logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
     * spring.jpa.show-sql=true
     * spring.jpa.properties.hibernate.format_sql=true
     */
    public String getSqlExtractionAdvice() {
        return "For SQL extraction in Hibernate 6.x, enable SQL logging with:\n" +
               "logging.level.org.hibernate.SQL=DEBUG\n" +
               "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE\n" +
               "spring.jpa.show-sql=true\n" +
               "spring.jpa.properties.hibernate.format_sql=true";
    }
}