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

package org.infogrid.lid.openid;

/**
 *
 */
public abstract class EitherSideAssociation
{
    /**
     * Constructor.
     */
    protected EitherSideAssociation(
            String  associationHandle,
            byte [] sharedSecret,
            long    issuedTime,
            long    expiryTime )
    {
        theAssociationHandle = associationHandle;
        theSharedSecret      = sharedSecret;
        theIssuedTime        = issuedTime;
        theExpiryTime        = expiryTime;
    }
    
    /**
     * Obtain the Association's handle.
     *
     * @return the Association's handle
     */
    public String getAssociationHandle()
    {
        return theAssociationHandle;
    }

    /**
     * Obtain the shared secret.
     *
     * @return the shared secret
     */
    public byte [] getSharedSecret()
    {
        return theSharedSecret;
    }

    /**
     * Obtain the time it was created.
     *
     * @return the time
     */
    public long getTimeCreated()
    {
        return theIssuedTime;
    }

    /**
     * Obtain the time it was last read.
     *
     * @return the time
     */
    public long getTimeRead()
    {
        return -1L; // FIXME
    }
    
    /**
     * Obtain the time when this association expires.
     *
     * @return the time
     */
    public long getTimeExpires()
    {
        return theExpiryTime;
    }

    /**
     * Obtain the duration from now, in milli-seconds, until this association expires.
     *
     * @return the duration
     */
    public long getRelativeTimeExpires()
    {
        long deltaMillis = theExpiryTime - System.currentTimeMillis();
        return deltaMillis;
    }
    

    /**
     * Determine whether the Association is currently valid.
     *
     * @return true if currently valid, false if expired
     */
    public boolean isCurrentlyValid()
    {
        return getRelativeTimeExpires() > 0L;
    }
    
    /**
     * The Association handle.
     */
    protected String theAssociationHandle;

    /**
     * The time the Association was issued.
     */
    protected long theIssuedTime;

    /**
     * The time the Association will expire.
     */
    protected long theExpiryTime;
    
    /**
     * The shared secret.
     */
    protected byte [] theSharedSecret;
}
