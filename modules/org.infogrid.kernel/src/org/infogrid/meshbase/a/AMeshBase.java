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

package org.infogrid.meshbase.a;

import org.infogrid.context.Context;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.a.AMeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.MeshObjectSetFactory;

import org.infogrid.meshbase.AbstractMeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.security.AccessManager;

import org.infogrid.meshbase.transaction.AbstractMeshObjectNeighborChangeEvent;
import org.infogrid.meshbase.transaction.AbstractMeshObjectRoleChangeEvent;
import org.infogrid.meshbase.transaction.AbstractMeshObjectTypeChangeEvent;
import org.infogrid.meshbase.transaction.Change;
import org.infogrid.meshbase.transaction.MeshObjectBecameDeadStateEvent;
import org.infogrid.meshbase.transaction.MeshObjectCreatedEvent;
import org.infogrid.meshbase.transaction.MeshObjectDeletedEvent;
import org.infogrid.meshbase.transaction.MeshObjectPropertyChangeEvent;
import org.infogrid.meshbase.transaction.Transaction;

import org.infogrid.modelbase.ModelBase;
import org.infogrid.model.primitives.RoleType;

import org.infogrid.meshbase.MeshObjectIdentifierFactory;

import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CachingMap;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;

import java.util.ArrayList;

/**
 * The subclass of MeshBase suitable for the AMeshObject implementation.
 */
public abstract class AMeshBase
        extends
            AbstractMeshBase
{
    private static final Log log = Log.getLogInstance( AMeshBase.class ); // our own, private logger

    /**
     * Constructor for subclasses only. This does not initialize content.
     *
     * @param identifier the MeshBaseIdentifier of this MeshBase
     * @param identifierFactory the factory for MeshObjectIdentifiers appropriate for this MeshBase
     * @param setFactory the factory for MeshObjectSets appropriate for this MeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param cache the CachingMap that holds the MeshObjects in this MeshBase
     * @param context the Context in which this MeshBase runs.
     */
    protected AMeshBase(
            MeshBaseIdentifier                          identifier,
            MeshObjectIdentifierFactory                 identifierFactory,
            MeshObjectSetFactory                        setFactory,
            ModelBase                                   modelBase,
            AccessManager                               accessMgr,
            CachingMap<MeshObjectIdentifier,MeshObject> cache,
            Context                                     context )
    {
        super( identifier, identifierFactory, setFactory, modelBase, accessMgr, cache, context );
    }

    /**
     * <p>Obtain a manager for object lifecycles.</p>
     * 
     * @return a MeshBaseLifecycleManagerthat works on this MeshBase with the specified parameters
     */
    public synchronized AMeshBaseLifecycleManager getMeshBaseLifecycleManager()
    {
        if( theMeshBaseLifecycleManager == null ) {
            theMeshBaseLifecycleManager = new AMeshBaseLifecycleManager( this );
        }
        return theMeshBaseLifecycleManager;
    }

    /**
     * Update the lastUpdated property. This is delegated to here so ShadowMeshBases
     * and do this differently than regular NetMeshBases.
     *
     * @param timeUpdated the time to set to, or -1L to indicate the current time
     * @param lastTimeUpdated the time this MeshObject was updated the last time
     * @return the time to set to
     */
    public long calculateLastUpdated(
            long timeUpdated,
            long lastTimeUpdated )
    {
        long ret;
        if( timeUpdated != -1L ) {
            ret = timeUpdated;
        } else {
            ret = System.currentTimeMillis();
        }
        return ret;
    }

    /**
     * Update the lastRead property. This does not trigger an event generation -- not necessary.
     * This may be overridden.
     *
     * @param timeRead the time to set to, or -1L to indicate the current time
     * @param lastTimeRead the time this MeshObject was read the last time
     * @return the time to set to
     */
    public long calculateLastRead(
            long timeRead,
            long lastTimeRead )
    {
        long ret;
        if( timeRead != -1L ) {
            ret = timeRead;
        } else {
            ret = System.currentTimeMillis();
        }
        return ret;
    }

    /**
     * <p>Determine the set of MeshObjects that are neighbors of all of the passed-in MeshObjects
     * while playing particular RoleTypes.</p>
     * <p>This is a convenience method that can have substantial performance benefits, depending on
     * the underlying implementation of MeshObject.</p>
     *
     * @param all the MeshObjects whose common neighbors we seek.
     * @param allTypes the RoleTypes to be played by the MeshObject at the same position in the array
     *        with the to-be-found MeshObjects
     * @return the set of MeshObjects that are neighbors of all MeshObjects
     */
    public MeshObjectSet findCommonNeighbors(
            MeshObject [] all,
            RoleType []   allTypes )
    {
        MeshObjectIdentifier [] currentSet    = null;
        int               currentLength = 0; // make compiler happy

        for( int i=0 ; i<all.length ; ++i ) {
            AMeshObject             pivot          = (AMeshObject) all[i];
            MeshObjectIdentifier [] pivotNeighbors = pivot.getInternalNeighborList();
            RoleType [][]           pivotTypes     = pivot.getInternalNeighborRoleTypes();

            if( currentSet == null ) {
                currentSet    = ArrayHelper.copyIntoNewArray( pivotNeighbors, MeshObjectIdentifier.class );
                currentLength = currentSet.length;

                if( allTypes != null && allTypes[i] != null ) {
                    for( int j=0 ; j<pivotNeighbors.length ; ++j ) {
                        if( pivotTypes[j] == null || !ArrayHelper.isIn( allTypes[i], pivotTypes[j], false )) {
                            currentSet[j] = null;
                            --currentLength;
                        }
                    }
                }

            } else {

                for( int j=0 ; j<currentSet.length ; ++j ) {
                    if( currentSet[j] == null ) {
                        continue;
                    }
                    int index = ArrayHelper.findIn( currentSet[j], pivotNeighbors, true );
                    if( index < 0 ) {
                        currentSet[j] = null;
                        --currentLength;

                    } else if( allTypes[i] != null && !ArrayHelper.isIn( allTypes[i], pivotTypes[index], false )) {
                        // check that the roles are right
                        currentSet[j] = null;
                        --currentLength;                        
                    }
                }
            }
            if( currentLength == 0 ) {
                break; // we are done, no common neighbors
            }
        }
        if( currentLength == 0 ) {
            return theMeshObjectSetFactory.obtainEmptyImmutableMeshObjectSet();
        } else {
            MeshObject [] ret;
            try {
                ret = accessLocally( ArrayHelper.collectNonNull( currentSet, MeshObjectIdentifier.class, currentLength ));

            } catch( MeshObjectAccessException ex ) {
                log.error( ex );
                ret = ex.getBestEffortResult();
            }
            return theMeshObjectSetFactory.createImmutableMeshObjectSet( ret );
        }
    }


    /**
     * This method may be overridden by subclasses to perform suitable actions when a
     * Transaction was committed.
     *
     * @param tx Transaction the Transaction that was committed
     */
    @Override
    protected void transactionCommittedHook(
            Transaction tx )
    {
        // FIXME? Should this be done asynchronously?
        Change [] theChanges = tx.getChangeSet().getChanges();

        ArrayList<MeshObjectIdentifier> writtenAlready = new ArrayList<MeshObjectIdentifier>( theChanges.length );
            // this needs to be MeshObjectIdentifier, not MeshObject, otherwise things get swapped back

        for( int i=0 ; i<theChanges.length ; ++i ) {
            
            Change         currentChange = theChanges[i];
            MeshObjectIdentifier affectedName  = currentChange.getAffectedMeshObjectIdentifier();

            if( writtenAlready.contains( affectedName )) {
                continue;
            }
            MeshObject affected = currentChange.getAffectedMeshObject();
            if( affected == null ) {
                log.error( "Cannot find affected MeshObject " + affectedName );

            } else if( affected.getIsDead() ) {
                // need to check for isDead first, otherwise we might update instead of delete, for example
                theCache.remove( affected.getIdentifier() );
                writtenAlready.add( affectedName );

            } else if( currentChange instanceof MeshObjectDeletedEvent ) {
                theCache.remove( affected.getIdentifier() );
                writtenAlready.add( affectedName );

            } else if( currentChange instanceof MeshObjectPropertyChangeEvent ) {
                theCache.put( affected.getIdentifier(), affected );
                writtenAlready.add( affectedName );

            } else if( currentChange instanceof AbstractMeshObjectNeighborChangeEvent ) { // either Added or Removed
                theCache.put( affected.getIdentifier(), affected );
                writtenAlready.add( affectedName );

            } else if( currentChange instanceof AbstractMeshObjectTypeChangeEvent ) { // either Added or Removed
                theCache.put( affected.getIdentifier(), affected );
                writtenAlready.add( affectedName );

            } else if( currentChange instanceof AbstractMeshObjectRoleChangeEvent ) { // either Added or Removed
                theCache.put( affected.getIdentifier(), affected );
                writtenAlready.add( affectedName );

            } else if( currentChange instanceof MeshObjectCreatedEvent ) {
                // noop, the LifecycleManager already did this
                // do not add to the writtenAlready, as there may have been changes to the MeshObject since.

            } else if( currentChange instanceof MeshObjectBecameDeadStateEvent ) {
                // noop, we catch this through MeshObjectLifecycleEvent.Deleted
 
            } else {
                log.error( "Unknown change: " + currentChange );
            }
        }
    }

    /**
     * Obtain the right ResourceHelper for StringRepresentation.
     * 
     * @return the ResourceHelper
     */
    protected ResourceHelper getResourceHelperForStringRepresentation()
    {
        return ResourceHelper.getInstance( AMeshBase.class );
    }

    /**
     * Enable AMeshObject to access the MeshObjectSetFactory.
     */
    public MeshObjectSetFactory getDefaultMeshObjectSetFactory()
    {
        return theMeshObjectSetFactory;
    }

    /**
     * The lifecycle manager.
     */
    protected AMeshBaseLifecycleManager theMeshBaseLifecycleManager;
}
