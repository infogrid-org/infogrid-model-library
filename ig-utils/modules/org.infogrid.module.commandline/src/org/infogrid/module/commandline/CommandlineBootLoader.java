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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.module.commandline;

import java.io.IOException;
import org.infogrid.module.ModuleAdvertisement;
import org.infogrid.module.ModuleClassLoader;
import org.infogrid.module.ModuleErrorHandler;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.OverridingStandardModuleActivator;
import org.infogrid.module.SoftwareInstallationException;
import org.infogrid.module.StandardModule;
import org.infogrid.module.StandardModuleActivator;

/**
 * <p>Acts as the main() program in a Module-based application.
 *    Based on passed-in parameters, it instantiates a ModuleRegistry, figures
 *    out what Modules are available, resolves and activates the top-most Module and
 *    all its mandatory dependencies, and runs it.</p>
 *
 * <p>Recognized arguments to the main program are described in class
 *    CommandlineSoftwareInstallation. Additional arguments can be passed in, which
 *    will be passed to the root module.</p>
 */
public class CommandlineBootLoader
{
    /**
     * The main program from the Java perspective: just a shell that catches exceptions.
     *
     * @param args arguments to the BootLoader
     */
    public static void main(
            String [] args )
    {
        try {
            main0( args );

            System.exit( 0 );

        } catch( Throwable ex ) {
            fatal( null, ex );
            System.exit( 1 );
        }
    }

    /**
     * The real main program.
     *
     * @param args arguments to the BootLoader
     */
    static void main0(
            String [] args )
    {
        // parse arguments into a SoftwareInstallation
        try {
            theInstallation = CommandlineSoftwareInstallation.createFromCommandLine( args );

            if( theInstallation == null ) {
                fatal( "Cannot determine SoftwareInstallation", null );
            }
        } catch( SoftwareInstallationException ex ) {
            fatal( null, ex );
        }

        
        final long sleepNow = theInstallation.getSleepPeriodBeforeExit();
        if( sleepNow > 0L ) {
            Runtime.getRuntime().addShutdownHook( new Thread() {
                @Override
                public void run()
                {
                    System.err.println( "Now sleeping " + sleepNow + " msec. (invoked with -sleepbeforeexit option)" );
                    try {
                        Thread.sleep( sleepNow );
                    } catch( InterruptedException ex ) {
                        // ignore
                    }                    
                }
            });
        }

        String [] remainingArgs = theInstallation.getRemainingArguments();

        // create ModuleRegistry
        ModuleRegistry theModuleRegistry = null;
        try {
            theModuleRegistry = CommandlineModuleRegistry.create( theInstallation );
        } catch( IOException ex ) {
            fatal( null, ex );
        } catch( ClassNotFoundException ex ) {
            fatal( null, ex );
        }
        if( theModuleRegistry == null ) {
            fatal( "Could not create Module Registry", null );
        }
        if( theInstallation.isShowModuleRegistry() ) {
            ModuleErrorHandler.informModuleRegistry( theModuleRegistry );
            System.exit( 0 );
        }

        // find and resolve the main module
        ModuleAdvertisement rootModuleAdv = null;
        try {
            rootModuleAdv = theModuleRegistry.determineSingleResolutionCandidate( theInstallation.getRootModuleRequirement() );
        } catch( Throwable ex ) {
            fatal( null, ex );
        }

        try {
            StandardModule theRootModule         = (StandardModule) theModuleRegistry.resolve( rootModuleAdv );
            ClassLoader    rootModuleClassLoader = theRootModule.getClassLoader();

            if( ( rootModuleClassLoader instanceof ModuleClassLoader ) && theInstallation.allowDefaultClassPathForRootModule() ) {
                ((ModuleClassLoader) rootModuleClassLoader).setConsultDefaultClassLoaderFirst( true );
            }

            StandardModuleActivator act = theRootModule.getDefaultModuleActivator();
            if(    theInstallation.getOverriddenActivationClassName() != null
                || theInstallation.getOverriddenActivationMethodName() != null )
            {
                act = new OverridingStandardModuleActivator(
                        act,
                        theInstallation.getOverriddenActivationClassName(),
                        theInstallation.getOverriddenActivationMethodName(),
                        theInstallation.getOverriddenDeactivationMethodName(),
                        theRootModule.getClassLoader() );
            }

            theRootModule.activateRecursively( act );
                    // may throw an exception

            theRootModule.configureRecursively( null, null ); // FIXME

            String runClassName  = theInstallation.getOverriddenRunClassName();
            String runMethodName = theInstallation.getOverriddenRunMethodName();

            theRootModule.run( runClassName, runMethodName, remainingArgs );

            theRootModule.deactivateRecursively();

        } catch( Throwable ex ) {
            fatal( null, ex );
        }
    }

    /**
     * This is called if all hope is lost and we need to exit.
     *
     * @param msg error message
     * @param ex exception, if any, that caused the problem
     */
    protected static void fatal(
            String    msg,
            Throwable ex )
    {
        ModuleErrorHandler.fatal( msg, ex, theInstallation );

        System.exit( 1 );
    }

    /**
     * Our CommandlineSoftwareInstallation, as soon as we create it.
     */
    protected static CommandlineSoftwareInstallation theInstallation = null;
}