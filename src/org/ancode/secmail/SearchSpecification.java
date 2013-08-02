
package org.ancode.secmail;

import org.ancode.secmail.mail.Flag;

public interface SearchSpecification {

    public Flag[] getRequiredFlags();

    public Flag[] getForbiddenFlags();

    public boolean isIntegrate();

    public String getQuery();

    public String[] getAccountUuids();

    public String[] getFolderNames();
}