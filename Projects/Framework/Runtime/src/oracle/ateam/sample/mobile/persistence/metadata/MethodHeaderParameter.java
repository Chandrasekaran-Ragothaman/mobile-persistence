/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 03-nov-2014   Steven Davelaar
 1.1           Changed from class to interface (new impl classes added)
 02-feb-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.metadata;

import oracle.adfmf.util.XmlAnyDefinition;

 /**
  * Interface that defines SOAP method header parameter or RESTful resource header parameter
  */
public interface MethodHeaderParameter
{
  public String getName();

  public String getValue();
}