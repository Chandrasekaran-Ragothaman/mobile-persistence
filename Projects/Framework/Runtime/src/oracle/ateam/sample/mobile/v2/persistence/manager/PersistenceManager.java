 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
  
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.manager;

import java.util.List;

import oracle.ateam.sample.mobile.v2.persistence.model.Entity;

/**
 * Interface that provides basic CRUD operations for a given entity instance.
 * You can plug in a local and remote PersistenceManager implementation in your
 * concrete subclasse of EntityCRUDService to handle local data storage as well as
 * synchronization with a remote data source.
 *
 * @see oracle.ateam.sample.mobile.v2.persistence.service.EntityCRUDService
 *
 *
 */
public interface PersistenceManager
{

  void mergeEntity(Entity entity, boolean doCommit);

  void insertEntity(Entity entity, boolean doCommit);

  void updateEntity(Entity entity, boolean doCommit);
  
  void removeEntity(Entity entity, boolean doCommit);

  <E extends Entity> List<E> findAll(Class entityClass);

  <E extends Entity> List<E> find(Class entityClass, String searchValue);

  <E extends Entity> List<E> find(Class entityClass, String searchValue, List<String> attrNamesToSearch);
  
  Entity findByKey(Class entityClass, Object[] key);

  Object getMaxValue(Class entityClass, String attrName);

  void commmit();

  void rollback();
}
