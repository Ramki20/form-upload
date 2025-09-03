package gov.usda.fsa.fcao.flp.flpids.common.dao.impl.support;

import java.lang.reflect.Field;
import java.util.Collections;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.loader.criteria.CriteriaLoader;
import org.hibernate.persister.entity.OuterJoinLoadable;

public class HibernateHqlAndCriteriaToSqlTranslator {
	 private SessionFactory sessionFactory;
	 
	  public void setSessionFactory(SessionFactory sessionFactory){
	    this.sessionFactory = sessionFactory;
	  }
	 
	  public String toSql(Criteria criteria){
	    try{
	      CriteriaImpl c = (CriteriaImpl) criteria;
	      SessionImpl s = (SessionImpl)c.getSession();
	      SessionFactoryImplementor factory = (SessionFactoryImplementor)s.getSessionFactory();
	      LoadQueryInfluencers loadQueryInfluencers = new LoadQueryInfluencers(factory);
	      String[] implementors = factory.getImplementors( c.getEntityOrClassName() );
	      if(implementors.length > 0){
		      CriteriaLoader loader = new CriteriaLoader((OuterJoinLoadable)factory.getEntityPersister(implementors[0]),
		        factory, c, implementors[0], loadQueryInfluencers);
		      Field f = OuterJoinLoader.class.getDeclaredField("sql");
		      f.setAccessible(true);
		      return (String) f.get(loader);
	      }
	      return "";
	    }
	    catch(Exception e){
	      throw new RuntimeException(e); 
	    }
	  }
	 
	  public String toSql(String hqlQueryText){
	    if (hqlQueryText!=null && hqlQueryText.trim().length()>0){
	      final QueryTranslatorFactory translatorFactory = new ASTQueryTranslatorFactory();
	      final SessionFactoryImplementor factory = 
	        (SessionFactoryImplementor) sessionFactory;
	      final QueryTranslator translator = translatorFactory.
	        createQueryTranslator(
	          hqlQueryText, 
	          hqlQueryText, 
	          Collections.EMPTY_MAP, factory, null
	        );
	      translator.compile(Collections.EMPTY_MAP, false);
	      return translator.getSQLString(); 
	    }
	    return null;
	  }

}