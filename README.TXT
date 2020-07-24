Test: THE MAVEN BUILD PROCESS OF THE PROJECT HAS BEEN REFACTORED FOR v4.5.x.
NOW THE PROJECT CONTAINS MORE THAN JUST A SINGLE MAVEN ARTIFACT IN THE REPOSITORY.

==================================
How to develop on NEXUSe2e Server?
==================================

NEXUSe2e is a Maven project. It is recommended to use Maven to build it.
Of course it is possible to build NEXUSe2e in a different way,
but it will take a lot of configuration effort that Maven can handle for you.

This document describes the steps that are necessary to create the
NEXUSe2e Server project under Eclipse and build it with Maven.
If you are not familar with Maven you could read about it here: http://maven.apache.org.

-------------------------------
1. Download and install software
-------------------------------

1.1 Install Maven (v3 is recommended) from http://maven.apache.org,
and add the Maven subfolder "bin" to the PATH variable of your os.
Possibly this step is superfluous, if you do not want to build NEXUSe2e also outside your IDE.

1.2 Install Eclipse IDE for Java EE Developers from http://www.eclipse.org
We take the JEE distribution, because we need the "Web Tools Platform" (WTP).
You could also install WTP seperately, if you want a smaller Eclipse distribution.

1.2 Install an Eclipse plugin that supports Subversion
e.g. "Subversive" from http://www.eclipse.org/subversive/.

1.3 Install the m2eclipse Maven plugin for Eclipse from http://m2eclipse.sonatype.org/,
and it's optional component that enables "Maven Integration for WTP".

1.4 Install Tomcat 6 from http://tomcat.apache.org

---------------------------------------
2. Create a new Java project in Eclipse
---------------------------------------

2.1 Create a new SVN repository location with the URL
https://nexuse2e-server.svn.sourceforge.net/svnroot/nexuse2e-server
and your sourceforge.net login and password.

2.2 Select the trunk, or a certain branch (version >= 4.5.x) from the new repository location,
and check out all sub-dirs as separate projects by choosing "Check Out",
or "Find/Check Out As..." from the context menu, and tell Eclipse that these are Java projects.

After that you should see several new projects in the Package Explorer view
of the Java perspective. Each project is a separate Maven artifact which together build the NEXUSe2e webapp.
The newly created projects may show some errors and problems. Do not worry about that right now.

2.3 Right-click on each project, and select "m2 Maven" > "Enable Dependency Management".
That will make m2eclipse take over the Maven build process, and download necessary Java libraries.

Now you should be able to build each project by selecting e.g. "Run As" > "Maven package" from the context menu.

If the Maven build fails, that will usually be caused by missing dependencies.
Those dependencies cannot be resolved automatically, because the according libraries
are not available in the common public Maven repositories.
To fix that, check the console output of m2eclipse for the missing dependencies.
Then download those jar files manually from the manufacturers' homepages.

If you run your own 3rd party Maven repository, you could simply deploy the downloaded jars to it.
Ensure that you have your 3rd party repository configured in your local ~/.m2/settings.xml.

If you do not run a 3rd party Maven repository, you can simply install each jar
to your local Maven repository (~/.m2/repository). Just open a terminal console,
and fire a Maven install command for each jar. E.g.:
mvn install:install-file -DgroupId=com.jcraft -DartifactId=jsch -Dversion=0.1.40 -Dpackaging=jar -Dfile=jsch-0.1.40.jar
Make sure you use the correct groupId, artifactId, and version as specified in the pom.xml
of the according NEXUSe2e Maven sub-project.
For more details about that consult http://maven.apache.org/plugins/maven-install-plugin/.

-----------------------------
3. Run NEXUSe2e server webapp
-----------------------------

We just rush through the steps here. The whole process is explained in more detail at
https://docs.sonatype.org/display/M2ECLIPSE/WTP+mini+howto.

3.1 Open the "Servers" view from the Eclipse Web Tool Platform (WTP), and add your Tomcat installation.

3.2 Open the context menu of your "nexuse2e-webapp" project, and select "Run As" > "Run on Server".
The "Maven Integration for WTP" support plugin should handle everything for you then.
After a while a browser tab should open in your editor view showing NEXUSe2e's web gui.

========
Appendix
========

------------------------------------
A. Build the project without eclipse
------------------------------------

A.a Just run the desired Maven command in a terminal console.

---------------------------------------
B. Install Reporting package (optional)
---------------------------------------

B.a Go to http://www.eclipse.org/birt/ and download the latest version of the BIRT reporting runtime package.
Extract the "ReportEngine" directory to the web application's WEB-INF directory and rename it to "platform".
