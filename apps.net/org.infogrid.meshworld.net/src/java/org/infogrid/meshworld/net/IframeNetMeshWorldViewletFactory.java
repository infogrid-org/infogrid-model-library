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

package org.infogrid.meshworld.net;

import java.util.ArrayList;
import org.infogrid.jee.viewlet.DefaultJspViewlet;
import org.infogrid.jee.viewlet.wikiobject.WikiObjectDisplayViewlet;
import org.infogrid.jee.viewlet.wikiobject.WikiObjectEditViewlet;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.model.Wiki.WikiSubjectArea;
import org.infogrid.util.ArrayHelper;
import org.infogrid.viewlet.AbstractViewletFactory;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * ViewletFactory for the NetMeshWorld application's iframe.
 */
public class IframeNetMeshWorldViewletFactory
        extends
            AbstractViewletFactory
{
    /**
     * Constructor.
     */
    public IframeNetMeshWorldViewletFactory()
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

        // Type-based rules
        if( subject.isBlessedBy( WikiSubjectArea.WIKIOBJECT )) {
            ret.add( WikiObjectDisplayViewlet.choice( ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
            ret.add( WikiObjectEditViewlet.choice(    ViewletFactoryChoice.GOOD_MATCH_QUALITY+1.0f ));
        }
        ret.add( DefaultJspViewlet.choice( "org.infogrid.jee.viewlet.propertysheet.PropertySheetViewlet",        ViewletFactoryChoice.BAD_MATCH_QUALITY ));
        ret.add( DefaultJspViewlet.choice( "org.infogrid.jee.viewlet.propertysheet.net.NetPropertySheetViewlet", ViewletFactoryChoice.BAD_MATCH_QUALITY - 1.0 )); // slightly better

        return ArrayHelper.copyIntoNewArray( ret, ViewletFactoryChoice.class );
    }
}
