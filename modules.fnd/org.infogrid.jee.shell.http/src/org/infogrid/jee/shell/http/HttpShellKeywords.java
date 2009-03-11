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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.shell.http;

/**
 * Collects the keywords understood by the HttpShell.
 */
public interface HttpShellKeywords
{
    /**
     * Separator between the components of the keywords.
     */
    public static final String SEPARATOR = ".";

    /**
     * The prefix for all keywords for the protocol.
     */
    public static final String PREFIX = "shell" + SEPARATOR;

    /**
     * Keyword indicating the identifier of a MeshObject.
     */
    public static final String IDENTIFIER_TAG = ""; // empty, that is intentional

    /**
     * Keyword indicating the type of access to a MeshObject that shall be performed.
     * For possible values, see HttpShellAccessVerb.
     */
    public static final String ACCESS_TAG = SEPARATOR + "access";

    /**
     * Keyword indicating the MeshBase in which the access shall be performed.
     */
    public static final String MESH_BASE_TAG = SEPARATOR + "meshbase";

    /**
     * Keyword indicating the <code>PropertyType</code> for an operation.
     */
    public static final String PROPERTY_TYPE_TAG  = SEPARATOR + "propertytype";

    /**
     * Keyword indicating the <code>PropertyValue</code> for an operation.
     */
    public static final String PROPERTY_VALUE_TAG = SEPARATOR + "propertyvalue";

    /**
     * Keyword indicating with which <code>EntityType</code> a MeshObject shall be blessed.
     */
    public static final String BLESS_TAG = SEPARATOR + "bless";

    /**
     * Keyword indicating from which <code>EntityType</code> a MeshObject shall be unblessed.
     */
    public static final String UNBLESS_TAG = SEPARATOR + "unbless";

    /**
     * Keyword indicating a relationship to another MeshObject.
     */
    public static final String TO_TAG = SEPARATOR + "to";

    /**
     * Keyword indicating a relationship verb to another MeshObject.
     * For possible values, see HttpShellRelationshipVerb.
     */
    public static final String PERFORM_TAG = SEPARATOR + "perform";

    /**
     * Keyword indicating the <code>RoleType</code> a relationship shall be blessed with.
     */
    public static final String BLESS_ROLE_TAG = SEPARATOR + "blessRole";

    /**
     * Keyword indicating the <code>RoleType</code> a relationship shall be unblessed from.
     */
    public static final String UNBLESS_ROLE_TAG = SEPARATOR + "unblessRole";

    /**
     * Keyword indicating whether any particular component of the operation should or should
     * not throw an exception if its execution failed.
     */
    public static final String THROW_TAG = SEPARATOR + "throw";
}