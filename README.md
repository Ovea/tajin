Tajin Servers
=============

Embedded containers to include in your project, with a generic launcher.

usage: ContainerRunner
 -container <container>     Required. Set which container to use. You use
                            one of [jetty|tomcat] or specify your own class implementing interface
                            Container or extending ContainerSkeleton
 -context <context>         Optional. Set the context path to use. Default
                            to: /
 -help                      Display this help
 -port <port>               Optional. Set the port to listen to. Default
                            to: 8080
 -servercp <servercp>       Optional. Set the additional classpath entries
                            to add to the server, separated by ',', ':' or ';'
 -webappcp <webappcp>       Optional. Set the additional classpath entries
                            to add to the webapp, separated by ',', ':' or ';'
 -webappRoot <webappRoot>   Optional. Set the location of the webapp.
                            Default to: src/main/webapp

