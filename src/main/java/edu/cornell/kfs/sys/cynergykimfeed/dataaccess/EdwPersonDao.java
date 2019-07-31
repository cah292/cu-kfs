package edu.cornell.kfs.sys.cynergykimfeed.dataaccess;

import java.util.Collection;

import edu.cornell.kfs.sys.cynergykimfeed.businessobject.EdwPerson;

public interface EdwPersonDao {
    
    Collection<EdwPerson> getEdwPersons(boolean full);

}
