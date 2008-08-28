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

package org.infogrid.meshbase.net.proxy;

import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.util.FactoryCreatedObject;
import org.infogrid.util.RemoteQueryTimeoutException;
import org.infogrid.util.text.StringRepresentation;

/**
 * <p>All communications between a NetMeshBase A and a NetMeshBase B is managed by
 * a pair of Proxies: one owned by and collocated with NetMeshBase A, interacting with
 * NetMeshBase B on behalf of A; the other owned by and collocated with NetMeshBase B,
 * interacting with NetMeshBase A on behalf of A.</p>
 * 
 * <p>The implementation class of ProxyMessageEndpoint determines how to perform
 * the actual communication.</p>
 */
public interface Proxy
        extends
            FactoryCreatedObject<NetMeshBaseIdentifier,Proxy,CoherenceSpecification>
{
    /**
     * Obtain the NetMeshBase to which this Proxy belongs.
     * 
     * @return the NetMeshBase
     */
    public abstract NetMeshBase getNetMeshBase();

    /**
     * Determine the NetMeshBaseIdentifier of the partner NetMeshBase. The partner
     * NetMeshBase is the NetMeshBase with which this Proxy communicates.
     * 
     * @return the NetMeshBaseIdentifier of the partner NetMeshBase
     */
    public abstract NetMeshBaseIdentifier getPartnerMeshBaseIdentifier();

    /**
     * Obtain the ProxyMessageEndpoint associated with this Proxy.
     *
     * @return the ProxyMessageEndpoint
     */
    public abstract ProxyMessageEndpoint getMessageEndpoint();

    /**
     * Obtain the CoherenceSpecification currently in effect.
     *
     * @return the current CoherenceSpecification
     */
    public abstract CoherenceSpecification getCoherenceSpecification();
    
    /**
     * Determine when this Proxy was first created. Often this will refer to a time long before this
     * particular Java object instance was created; this time refers to when the connection between
     * the two logical NetMeshBases was created, which could have been in a previous run prior to, say,
     * a server reboot.
     *
     * @return the time this Proxy was created, in System.currentTimeMillis() format
     */
    public abstract long getTimeCreated();

    /**
     * Determine when information held by this Proxy was last updated.
     *
     * @return the time this Proxy was last updated, in System.currentTimeMillis() format
     */
    public abstract long getTimeUpdated();

    /**
     * Determine when information held by this Proxy was last read.
     *
     * @return the time this Proxy was last read, in System.currentTimeMillis() format
     */
    public abstract long getTimeRead();

    /**
     * Determine when this Proxy will expire, if at all.
     *
     * @return the time this Proxy will expire, in System.currentTimeMillis() format, or -1L if never.
     */
    public abstract long getTimeExpires();

    /**
     * Ask this Proxy to obtain from its partner NetMeshBase replicas with the enclosed
     * specification. Do not acquire the lock; that would be a separate operation.
     * This call must return immediately; the caller must wait instead. (This is necessary
     * to be able to perform accessLocally() via several Proxies in parallel.)
     * 
     * @param paths the NetMeshObjectAccessSpecifications specifying which replicas should be obtained
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @return the duration, in milliseconds, that the Proxy believes this operation will take
     */
    public abstract long obtainReplicas(
            NetMeshObjectAccessSpecification [] paths,
            long                                duration );

    /**
     * Ask this Proxy to obtain the lock for one or more replicas from the
     * partner NetMeshBase. Unlike many of the other calls, this call is
     * synchronous over the network and either succeeds, fails, or times out.
     *
     * @param localReplicas the local replicas for which the lock should be obtained
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @throws RemoteQueryTimeoutException thrown if this call times out
     */
    public abstract void tryToObtainLocks(
            NetMeshObject [] localReplicas,
            long             duration )
        throws
            RemoteQueryTimeoutException;

    /**
     * Ask this Proxy to push the locks for one or more replicas to the partner
     * NetMeshBase. Unlike many of the other calls, this call is
     * synchronous over the network and either succeeds, fails, or times out.
     * 
     * @param localReplicas the local replicas for which the lock should be pushed
     * @param isNewProxy if true, the the NetMeshObject did not replicate via this Proxy prior to this call.
     *         The sequence in the array is the same sequence as in localReplicas.
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @throws RemoteQueryTimeoutException thrown if this call times out
     */
    public abstract void tryToPushLocks(
            NetMeshObject [] localReplicas,
            boolean []       isNewProxy,
            long             duration )
        throws
            RemoteQueryTimeoutException;

    /**
     * Ask this Proxy to obtain the home replica status for one or more replicas from the
     * partner NetMeshBase. Unlike many of the other calls, this call is
     * synchronous over the network and either succeeds, fails, or times out.
     *
     * @param localReplicas the local replicas for which the home replica status should be obtained
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @throws RemoteQueryTimeoutException thrown if this call times out
     */
    public abstract void tryToObtainHomeReplicas(
            NetMeshObject [] localReplicas,
            long             duration )
        throws
            RemoteQueryTimeoutException;

    /**
     * Ask this Proxy to push the home replica status for one or more replicas to the partner
     * NetMeshBase. Unlike many of the other calls, this call is
     * synchronous over the network and either succeeds, fails, or times out.
     * 
     * @param localReplicas the local replicas for which the home replica status should be pushed
     * @param isNewProxy if true, the the NetMeshObject did not replicate via this Proxy prior to this call.
     *         The sequence in the array is the same sequence as in localReplicas.
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @throws RemoteQueryTimeoutException thrown if this call times out
     */
    public void tryToPushHomeReplicas(
            NetMeshObject [] localReplicas,
            boolean []       isNewProxy,
            long             duration )
        throws
            RemoteQueryTimeoutException;

    /**
     * Send notification to the partner NetMeshBase that this MeshBase has forcibly taken the
     * lock back for the given NetMeshObjects.
     *
     * @param localReplicas the local replicas for which the lock has been forced back
     */
    public abstract void forceObtainLocks(
            NetMeshObject [] localReplicas );

    /**
     * Tell the partner NetMeshBase that one or more local replicas would like to be
     * resynchronized. This call uses NetMeshObjectIdentifier instead of NetMeshObject
     * as sometimes the NetMeshObjects have not been instantiated when this call is
     * most naturally made.
     *
     * @param identifiers the identifiers of the NetMeshObjects
     */
    public abstract void tryResynchronizeReplicas(
            NetMeshObjectIdentifier [] identifiers );

    /**
     * Ask this Proxy to cancel the leases for the given replicas from its partner NetMeshBase.
     * 
     * @param localReplicas the local replicas for which the lease should be canceled
     */
    public abstract void cancelReplicas(
            NetMeshObject [] localReplicas );

    /**
     * Invoked by the NetMeshBase that this Proxy belongs to,
     * it causes this Proxy to initiate the "ceasing communication" sequence with
     * the partner NetMeshBase, and then kill itself.
     *
     * @throws RemoteQueryTimeoutException thrown if communications timed out
     */
    public abstract void initiateCeaseCommunications()
        throws
            RemoteQueryTimeoutException;

    /**
     * Tell this Proxy that it is not needed any more. This will invoke
     * {@link #initiateCeaseCommunications} if and only if
     * isPermanent is true.
     * 
     * @param isPermanent if true, this Proxy will go away permanently; if false,
     *        it may come alive again some time later, e.g. after a reboot
     */
    public abstract void die(
            boolean isPermanent );

    /**
     * Obtain this Proxy in externalized form.
     *
     * @return the ExternalizedProxy capturing the information in this Proxy
     */
    public abstract ExternalizedProxy asExternalized();

    /**
     * Indicates that a Transaction has been committed. This is invoked by the NetMeshBase
     * without needing a subscription.
     *
     * @param theTransaction the Transaction that was committed
     */
    public void transactionCommitted(
            Transaction theTransaction );

    /**
     * Subscribe to lease-related events, without using a Reference.
     *
     * @param newListener the to-be-added listener
     * @see #addWeakProxyListener
     * @see #addSoftProxyListener
     * @see #removeProxyListener
     */
    public abstract void addDirectProxyListener(
            ProxyListener newListener );

    /**
     * Subscribe to lease-related events, using a WeakReference.
     *
     * @param newListener the to-be-added listener
     * @see #addDirectProxyListener
     * @see #addSoftProxyListener
     * @see #removeProxyListener
     */
    public abstract void addWeakProxyListener(
            ProxyListener newListener );

    /**
     * Subscribe to lease-related events, using a SoftReference.
     *
     * @param newListener the to-be-added listener
     * @see #addWeakProxyListener
     * @see #addDirectProxyListener
     * @see #removeProxyListener
     */
    public abstract void addSoftProxyListener(
            ProxyListener newListener );

    /**
     * Unsubscribe from lease-related events.
     *
     * @param oldListener the to-be-removed listener
     * @see #addDirectProxyListener
     * @see #addWeakProxyListener
     * @see #addSoftProxyListener
     */
    public abstract void removeProxyListener(
            ProxyListener oldListener );

    /**
     * Obtain a String representation of this Proxy that can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param isDefaultMeshBase true if the enclosing MeshBase is the default MeshBase
     * @return String representation
     */
    public abstract String toStringRepresentation(
            StringRepresentation rep,
            boolean              isDefaultMeshBase );

    /**
     * Obtain the start part of a String representation of this Proxy that acts
     * as a link/hyperlink and can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param contextPath the context path
     * @param isDefaultMeshBase true if the enclosing MeshBase is the default MeshBase
     * @return String representation
     */
    public abstract String toStringRepresentationLinkStart(
            StringRepresentation rep,
            String               contextPath,
            boolean              isDefaultMeshBase );

    /**
     * Obtain the end part of a String representation of this Proxy that acts
     * as a link/hyperlink and can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param contextPath the context path
     * @param isDefaultMeshBase true if the enclosing MeshBase is the default MeshBase
     * @return String representation
     */
    public abstract String toStringRepresentationLinkEnd(
            StringRepresentation rep,
            String               contextPath,
            boolean              isDefaultMeshBase );
}