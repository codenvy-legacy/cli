```
                                  _
                                 | |
         _           ___ ___   __| | ___ _ ____   ___   _
 {    ('   )    }   / __/ _ \ / _` |/ _ \ '_ \ \ / / | | |
{{  ('       )  }} | (_| (_) | (_| |  __/ | | \ V /| |_| |
 { (__,__,__,_) }   \___\___/ \__,_|\___|_| |_|\_/  \__, |
                                                     __/ |
                                                    |___/
```

Codenvy command line interface V2
=================================


![CLI](http://codenvy.github.io/cli/img/cli.gif)


# Quick Start for the developer
------------------------------

## Building
1. Checkout of the project: git clone https://github.com/codenvy/cli
2. Build: mvn clean install

## Running
1. Launch from the root directory of the project:

  1.: Interactive client with ./codenvy-cli
  
  2.: Non-interactive client with ./codenvy


## Developing a new command
Apache Karaf is used for providing the Codenvy CLI.

### New command
When adding a new builtin command, the following files need to be modified :
  * command/src/main/resources/OSGI-INF/blueprint/shell-log.xml
  * command/src/main/resources/META-INF/services/org/apache/karaf/shell/commands

The first one is for providing the command to Interactive/OSGi mode and the next one is for providing the command to the default CLI shell in non-interactive mode.


### Advanced help 
The guide to develop karaf command is available at http://karaf.apache.org/manual/latest/developers-guide/extending.html

