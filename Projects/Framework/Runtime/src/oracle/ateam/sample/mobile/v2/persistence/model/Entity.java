 /*******************************************************************************
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
   
  $revision_history$
  19-mar-2015   Steven Davelaar
  1.1           Added call to EntityUtils.refreshCurrentEntity in refreshChildEntityList method
                to ensure UI is also refreshed correctly when child entities are shown in form layout 
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.model;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;

import oracle.ateam.sample.mobile.v2.persistence.service.IndirectList;
import oracle.ateam.sample.mobile.v2.persistence.service.ValueHolderInterface;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingOneToMany;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingOneToOne;
import oracle.ateam.sample.mobile.v2.persistence.service.ValueHolder;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;


/**
 *  Abstract class that must be extended by all data object classes that need to be persisted, either remotely or
 *  local on mobile device in SQLite database.
 */
public abstract class Entity<E> extends ChangeEventSupportable
{

  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(Entity.class);
  
  private transient boolean isNewEntity = false;
  private transient boolean canonicalGetExecuted = false;

  public void setCanonicalGetExecuted(boolean canonicalGetExecuted)
  {
    this.canonicalGetExecuted = canonicalGetExecuted;
  }

  /**
   * Method does not start with "is" to prevent property from showing up in DC palette
   * @return
   */
  public boolean canonicalGetExecuted()
  {
    return canonicalGetExecuted;
  }

  public Object getAttributeValue(String attrName)
  {
    try
    {
      Method getter = EntityUtils.getGetMethod(this.getClass(),attrName);
      Object value = getter.invoke(this, null);
      return value;
    }
    catch (IllegalAccessException e)
    {
      throw new AdfException("Error invoking getter method for attribute " + attrName + " in class " +
                                 this.getClass().getName() + " " +
                                 e.getLocalizedMessage(),AdfException.ERROR);
    }
    catch (InvocationTargetException e)
    {
     throw new AdfException("Error invoking getter method for attribute " + attrName + " in class " +
                                this.getClass().getName() + " " +
                                e.getLocalizedMessage(),AdfException.ERROR);
   }
  }

  public void setAttributeValue(String attrName, Object value)
  {
    boolean valueHolder = value instanceof ValueHolderInterface;
    Method setter = EntityUtils.getSetMethod(this.getClass(),attrName,valueHolder);
    String valueType = value!=null ? value.getClass().getName() : "Null";
    if (setter == null)
    {
      throw new AdfException("No setter method found for attribute " + attrName + " in class " +
                                 this.getClass().getName(), AdfException.ERROR);
    }
    try
    {
      setter.setAccessible(true);
      setter.invoke(this, new Object[]
          { value });
    }
    catch (IllegalAccessException e)
    {
      throw new AdfException("Error invoking setter method for attribute " + attrName + " in class " +
                              this.getClass().getName() + " with value of type "+valueType+ ": " 
                               + e.getLocalizedMessage(),AdfException.ERROR);
    }
    catch (InvocationTargetException e)
    {
      throw new AdfException("Error invoking setter method for attribute " + attrName + " in class " +
                                 this.getClass().getName() + " with value of type "+valueType + ": " 
                                 + e.getTargetException().getClass().getName() + " " +
                                 e.getTargetException().getLocalizedMessage(),AdfException.ERROR);
    }
  }

  public void setIsNewEntity(boolean isNewEntity)
  {
    this.isNewEntity = isNewEntity;
  }

//  public boolean isIsNewEntity()
//  {
//    return isNewEntity;
//  }
  
  public boolean getIsNewEntity()
  {
    return isNewEntity;
  }
  
//  public void fireAttributeChangeEvent(Entity oldEntity, String attrName)
//  {
//    if (oldEntity!=null)
//    {
//      Object oldValue = oldEntity.getAttributeValue(attrName);
//      Object newValue = this.getAttributeValue(attrName);
//      getPropertyChangeSupport().firePropertyChange(attrName, oldValue, newValue);      
//      sLog.fine("Fired propertyChange event for entity attribute "+this.getClass().getName()+" "+attrName+" old value: "+oldValue+", new value: "+newValue);
//    }
//    getProviderChangeSupport().fireProviderRefresh(attrName);    
//    sLog.fine("Fired providerRefresh event for entity attribute "+this.getClass().getName()+" "+attrName);
//  }

  public boolean equals(Object obj)
  {
    if (obj.getClass()!=this.getClass())
    {
      return false;
    }
    Entity compareEntity = (Entity) obj;
    Object[] compareKey = EntityUtils.getEntityKey(compareEntity);
    Object[] thisKey = EntityUtils.getEntityKey(this); 
    return EntityUtils.compareKeys(compareKey, thisKey);
  }
  
  /**
   * Creates an IndirectList instance that encapsulates a AttributeMappingOneToMany
   * mapping so we can lazily load the list from DB when child collection is requested for the first time
   * @param accessorAttribute
   * @return
   */
  protected <E extends Entity> List<E> createIndirectList(String accessorAttribute)
  {
      AttributeMapping mapping = EntityUtils.findMapping(getClass(), accessorAttribute);
      if (mapping!=null && mapping instanceof AttributeMappingOneToMany)
      {
        return new IndirectList<E>(this, (AttributeMappingOneToMany)mapping);          
      }
      // fallback:  return simple array list
      return new ArrayList<E>();
  }


  /**
   * Creates a ValueHolder instance that encapsulates a AttributeMappingOneToOne
   * mapping so we can lazily load the row from DB when parent row is requested for the first time
   * @param accessorAttribute
   * @return
   */
  protected ValueHolderInterface createValueHolder(String accessorAttribute)
  {
    AttributeMapping mapping = EntityUtils.findMapping(getClass(), accessorAttribute);
    if (mapping!=null && mapping instanceof AttributeMappingOneToOne)
    {
      return new ValueHolder(this, (AttributeMappingOneToOne)mapping);          
    }
    return null;
  }

  /**
   * This method is called from IndirectList.buildDelegate when child rows for an entity are retrieved
   * through a remote server call executed in the background
   * @param oldList
   * @param newList
   * @param childClass NOT USED ANYMORE
   * @param childAttribute
   */
  public void refreshChildEntityList(List oldList, List newList, Class childClass, String childAttribute)
  {
    refreshChildEntityList(oldList,newList,childAttribute);
  }

  /**
   * This method is called from IndirectList.buildDelegate when child rows for an entity are retrieved
   * through a remote server call executed in the background
   * @param oldList
   * @param newList
   * @param childAttribute
   */
  public void refreshChildEntityList(List oldList, List newList, String childAttribute)
  {
    getPropertyChangeSupport().firePropertyChange(childAttribute, oldList, newList);
    getProviderChangeSupport().fireProviderRefresh(childAttribute);
    // the above two statements do NOT refresh the UI when the UI displays a form layout instead of
    // a list view. So, we als refresh the current entity. 
    EntityUtils.refreshCurrentEntity(childAttribute,newList,getProviderChangeSupport());
    if (AdfmfJavaUtilities.isBackgroundThread())
    {
      AdfmfJavaUtilities.flushDataChangeEvent();
    }
  }

}
