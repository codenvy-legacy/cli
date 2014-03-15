CODENVY CLI
===

- Website: http://docs.codenvy.com/cli
- Source: http://github.com/codenvy/cli
- Mailing List: [Google Groups] (https://groups.google.com/a/codenvy.com/forum/#!forum/helpdesk)

Codenvy makes SaaS developer environments.  We provide those environments in our public cloud hosted at codenvy.com, or through on premises installations with Codenty Enterprise.  This CLI tool provides command line access to Codenvy installations.  This CLI is portable across MacOS, Linux, and Windows.

Quick Start
-----------
You will need to have [Java version 6] (http://java.com/en/download/index.jsp) or later installed on your system.  Download the CLI library and unzip it in a directory.  The start script depends upon the `JAVA_HOME` environment variable being set and pointing to your Java installation.  Also update `CLI_HOME` to point to the directory where the CLI is installed.  Then set your system `PATH` variable to point to the directory where the CLI is installed.

On MacOS or Linux, place this in your `.bashrc` file:

    export JAVA_HOME=DIR_OF_JDK
    export CLI_HOME=DIR_OF_CLI
    export PATH=$CLI_HOME:$JAVA_HOME/bin:$PATH
    
On Windows, update your system environment variables:

    JAVA_HOME=DIR_OF_JDK
    CLI_HOME=DIR_OF_CLI
    PATH=%CLI_HOME%;%JAVA_HOME%\bin;%PATH%

Test your configuration by entering `codenvy` in the console window.  You should see the CLI help message appear.

Getting Started Guide
---------------------
To learn how to interact with remote Codenvy systems, create new projects, load them into the system, and analyze their behavior, view the [Getting Started Guide.] (http://docs.codenvy.com/cli)

Contributing to the CLI
-----------------------
To hack on the CLI (or Codenvy) you will need [maven] (http://maven.apache.org/) installed.  

Once your maven is properly configured, you can build the CLI by entering

    mvn clean install

If you run into problems with the build, may need to configure maven to reference the Codenvy central nexus server to download libraries.  Add the following configuration item to your `settings.xml` file either stored in `M2_HOME` or `M2_REPO`.

     <mirrors>
       <mirror>
         <id>repo.codenvy.private</id>
         <mirrorOf>external:*</mirrorOf>
         <url>https://maven.codenvycorp.com/content/groups/codenvy-private-group/</url>
       </mirror>
     </mirrors>
    
