//
// This file is part of InfoGrid(tm). You may not use this file except in
// compliance with the InfoGrid license. The InfoGrid license and important
// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
// or you do not consent to all aspects of the license and the disclaimers,
// no license is granted; do not use this file.
// 
// For more information about InfoGrid go to http://infogrid.org/
//
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.util;

import org.infogrid.util.text.StringRepresentation;

import java.net.URISyntaxException;

/**
 * A factory for Identifiers.
 */
public interface IdentifierFactory
{
    /**
     * Create an Identifier given its external form.
     * This is the opposite of Identifier.toExternalForm().
     *
     * @param externalForm the externalForm
     * @return the Identifier
     */
    public Identifier fromExternalForm(
            String externalForm )
        throws
            URISyntaxException;

    /**
     * Convert this StringRepresentation back to an Identifier.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created MeshObjectIdentifier
     */
    public Identifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            URISyntaxException;
}
