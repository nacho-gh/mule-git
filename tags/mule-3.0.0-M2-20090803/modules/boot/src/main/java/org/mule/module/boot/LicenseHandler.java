/*
 * $Id $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.boot;

import org.mule.module.boot.util.JarUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;

/**
 * This class has methods for displaying the EULA and saving the license acceptance
 * acknowledgment.
 */
public final class LicenseHandler
{
    private static final int MAX_ROWS_TO_DISPLAY = 80;

    private static final String DEFAULT_LICENSE_TYPE = "Common Public Attribution License Version 1.0 (CPAL)";
    private static final String DEFAULT_LICENSE_VERSION = "UNKNOWN";
    private static final String VERSION_TEXT_PREFIX = "Version ";
    
    private static final String LICENSE_TEXT_FILENAME = "LICENSE.txt";
    
    private static final String LICENSE_TEXT_JAR_FILE_PATH = "META-INF/mule/" + LICENSE_TEXT_FILENAME;
    private static final String LICENSE_PROPERTIES_JAR_FILE_PATH = "META-INF/mule/license.props";
    
    private LicenseHandler()
    {
        // used as utility class at this point only
    }
    
    public static boolean isLicenseAccepted() throws Exception
    {
        // here we're temporarily adding the mule-local-install.jar to the classpath for a license check
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try
        {
            File localJar = MuleBootstrapUtils.getMuleLocalJarFile();
            URLClassLoader cl = new URLClassLoader(new URL[] {localJar.toURI().toURL()}, oldCl);
            Thread.currentThread().setContextClassLoader(cl);

            return MuleBootstrapUtils.getResource(LICENSE_PROPERTIES_JAR_FILE_PATH, LicenseHandler.class) != null;
        }
        finally
        {
            // dereference the old classloader and revert to the original caller one, we're done
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }
    
    public static File getLicenseFile()
    {
        return new File(MuleBootstrapUtils.getMuleHomeFile(), LICENSE_TEXT_FILENAME);
    }
    
    /**
     * Display the EULA and get the user's acceptance. Note that only a missing
     * license file or a non-yes answer will cause this to return false. If the user
     * accepts, but we can't write the license ack file for some reason, we'll still
     * return true.
     * 
     * @return boolean whether the license was accepted or not
     */
    public static boolean getAcceptance()
    {
        boolean hasAccepted;
        
        try
        {
            if (!getLicenseFile().exists() || !MuleBootstrapUtils.getMuleLibDir().exists())
            {
                System.out.println("\nYour Mule installation seems to be incomplete. Please try downloading it again from http://mule.mulesource.org/display/MULE/Download and start again.");
                hasAccepted = false;
            }
            else
            {
                System.out.println("\n\nPlease read over the following license agreement carefully:\n\n");
                
                LicenseInfo licenseInfo = readLicenseFileAndDisplayToStdout();
                
                hasAccepted = askUserForAcceptance();
                
                if (hasAccepted)
                {
                    saveLicenseInfo(licenseInfo);
                }
            }
        }
        catch (Exception e)
        {
            hasAccepted = false;
            System.out.println("\nSorry, we encountered an error in processing your license agreement - please try again.");
            e.printStackTrace();
        }

        return hasAccepted;
    }

    public static void saveLicenseInfo(LicenseInfo licenseInfo) throws Exception
    {
        if (licenseInfo != null && !isLicenseAccepted())
        {
            if (!MuleBootstrapUtils.getMuleLibDir().canWrite())
            {
                throw new Exception("No write permissions for " + MuleBootstrapUtils.MULE_LOCAL_JAR_FILENAME + 
                    " to MULE_HOME. If you are using MULE_BASE and multiple deployments, please ask your administrator to run Mule for the first time.");
            }
            File tempJarFile = createTempLicenseJarFile(licenseInfo);
            MuleBootstrapUtils.getMuleLocalJarFile().delete();
            MuleBootstrapUtils.renameFile(tempJarFile, MuleBootstrapUtils.getMuleLocalJarFile());
        }
    }

    private static LicenseInfo readLicenseFileAndDisplayToStdout() throws IOException
    {
        String licenseType = null;
        String licenseVersion = null;
        
        BufferedReader stdin;
        BufferedReader fileReader = null;

        try
        {
            stdin = new BufferedReader(new InputStreamReader(System.in));
            fileReader = new BufferedReader(new FileReader(getLicenseFile()));
            int row = 1;
            
            while (fileReader.ready())
            {
                String line = fileReader.readLine();
    
                if (row == 1 && line.length() > 0) 
                {
                    licenseType = line;
                }
                if (row == 2 && line.startsWith(VERSION_TEXT_PREFIX))
                {
                    licenseVersion = line.substring(VERSION_TEXT_PREFIX.length());
                }
    
                if ((row % MAX_ROWS_TO_DISPLAY) == 0)
                {
                    System.out.print("\nHit return to continue ... ");
                    stdin.readLine();
                }
    
                System.out.println(line);
                row++;
            }
        }
        finally
        {
            if (fileReader != null)
            {
                fileReader.close();
            }
        }
        
        return new LicenseInfo(licenseType, licenseVersion);
    }
    
    private static boolean askUserForAcceptance() throws IOException
    {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.print("\n\nDo you accept the terms and conditions of this license agreement [y/n]?");
        final String input = stdin.readLine();        

        boolean hasAcccepted = false;
        if (input != null && input.toLowerCase().startsWith("y"))
        {
            hasAcccepted = true;
        }
        
        if (!hasAcccepted)
        {
            System.out.println("\nSorry, until you accept the terms and conditions of this EULA, you won't be able to start Mule");
        }
        
        return hasAcccepted;
    }

    private static File createTempLicenseJarFile(LicenseInfo licenseInfo) throws Exception
    {
        LinkedHashMap jarEntries = new LinkedHashMap();
        jarEntries.put(LICENSE_PROPERTIES_JAR_FILE_PATH, licenseInfo.toString());
        jarEntries.put(LICENSE_TEXT_JAR_FILE_PATH, getLicenseFile());

        File tempJar = File.createTempFile(MuleBootstrapUtils.MULE_LOCAL_JAR_FILENAME, null);

        try
        {
            JarUtils.createJarFileEntries(tempJar, jarEntries);
        }
        catch (IOException ioe)
        {
            if (tempJar != null)
            {
                throw new Exception("Unable to create temporary jar file to " + tempJar.getAbsolutePath());
            }
            else
            {
                throw new Exception("Unable to create temporary jar file for " + MuleBootstrapUtils.MULE_LOCAL_JAR_FILENAME);
            }
        }

        return tempJar;
    }
    
    public static class LicenseInfo
    {
        private String licenseType = DEFAULT_LICENSE_TYPE;
        private String licenseVersion = DEFAULT_LICENSE_VERSION;
        private String licenseDate = (new java.util.Date()).toString();
        
        public LicenseInfo()
        {
            // for default license
        }
        
        public LicenseInfo(String licenseType, String licenseVersion)
        {
            if (licenseType != null && !licenseType.trim().equals(""))
            {
                this.licenseType = licenseType;
            }
            if (licenseVersion != null && !licenseVersion.trim().equals(""))
            {
                this.licenseVersion = licenseVersion;
            }
        }
        
        public String toString()
        {
            return "LicenseType=" + licenseType + "\n" +
                "LicenseVersion=" + licenseVersion + "\n" +
                "LicenseDate=" + licenseDate + "\n";
        }
    }
}
