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

package org.infogrid.meshworld.net;

import java.util.ArrayList;
import org.infogrid.jee.viewlet.PseudoJspViewlet;
import org.infogrid.jee.viewlet.bulk.BulkLoaderViewlet;
import org.infogrid.jee.viewlet.graphtree.GraphTreeViewlet;
import org.infogrid.jee.viewlet.meshbase.AllMeshObjectsViewlet;
import org.infogrid.jee.viewlet.meshbase.net.ProxiesViewlet;
import org.infogrid.jee.viewlet.meshbase.net.ProxyViewlet;
import org.infogrid.jee.viewlet.modelbase.AllMeshTypesViewlet;
import org.infogrid.jee.viewlet.probe.shadow.ShadowAwareAllMeshBasesViewlet;
import org.infogrid.jee.viewlet.servlet.net.NetViewletDispatcherServlet;
import org.infogrid.jee.viewlet.wikiobject.WikiObjectDisplayViewlet;
import org.infogrid.jee.viewlet.wikiobject.WikiObjectEditViewlet;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.model.Wiki.WikiSubjectArea;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.ArrayHelper;
import org.infogrid.viewlet.AbstractViewletFactory;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * ViewletFactory for the NetMeshWorld application's main screen.
 */
public class MainNetMeshWorldViewletFactory
        extends
            AbstractViewletFactory
{
    /**
     * Constructor.
     */
    public MainNetMeshWorldViewletFactory()
    {
        super( "org.infogrid.jee.viewlet.JeeViewlet" );
    }

    /**
     * Find the ViewletFactoryChoices that apply to these MeshObjectsToView, but ignore the specified
     * viewlet type. If none are found, return an emtpy array.
     *
     * @param theObjectsToView the MeshObjectsToView
     * @return the found ViewletFactoryChoices, if any
     */
    public ViewletFactoryChoice [] determineFactoryChoicesIgnoringType(
            MeshObjectsToView theObjectsToView )
    {
        ArrayList<ViewletFactoryChoice> ret = new ArrayList<ViewletFactoryChoice>();
        
        NetMeshObject subject = (NetMeshObject) theObjectsToView.getSubject();
        NetMeshBase   base    = subject.getMeshBase();

        // NetMeshBase's Home Object
        if( base.getHomeObject() == subject ) {
            ret.add( AllMeshObjectsViewlet.choice(          ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
            ret.add( AllMeshTypesViewlet.choice(            ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            ret.add( BulkLoaderViewlet.choice(              ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            ret.add( ProxiesViewlet.choice(                 ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));
            ret.add( ShadowAwareAllMeshBasesViewlet.choice( ViewletFactoryChoice.AVERAGE_MATCH_QUALITY ));

            Proxy p = theObjectsToView.getViewletParameters() != null ? (Proxy) theObjectsToView.getViewletParameters().get( NetViewletDispatcherServlet.PROXY_NAME ) : null;
            if( p != null ) {
                ret.add( ProxyViewlet.choice( ViewletFactoryChoice.PERFECT_MATCH_QUALITY+1.d )); // not quite perfect
            }
        }
        
        // Type-based rules
        if( subject.isBlessedBy( WikiSubjectArea.WIKIOBJECT )) {
            ret.add( WikiObjectDisplayViewlet.choice( ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
            ret.add( WikiObjectEditViewlet.choice(    ViewletFactoryChoice.GOOD_MATCH_QUALITY+1.0f ));
        }
        ret.add( GraphTreeViewlet.choice( new TraversalSpecification[3], ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
        ret.add( PseudoJspViewlet.choice( "org.infogrid.jee.viewlet.propertysheet.PropertySheetViewlet",        ViewletFactoryChoice.BAD_MATCH_QUALITY ));
        ret.add( PseudoJspViewlet.choice( "org.infogrid.jee.viewlet.propertysheet.net.NetPropertySheetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY - 1.0 )); // slightly better
        ret.add( PseudoJspViewlet.choice( "org.infogrid.jee.viewlet.objectset.ObjectSetViewlet",                ViewletFactoryChoice.BAD_MATCH_QUALITY ));

        return ArrayHelper.copyIntoNewArray( ret, ViewletFactoryChoice.class );
    }
}