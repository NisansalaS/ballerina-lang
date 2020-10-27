module io.ballerina.projects {
    requires io.ballerina.tools.api;
    requires toml4j;
    requires jsr305;
    requires org.apache.commons.compress;
    requires io.ballerina.parser;
    requires gson;
    requires io.ballerina.lang;
    requires org.apache.commons.io;
    exports io.ballerina.projects.directory;
    exports io.ballerina.projects.utils;
    exports io.ballerina.projects.balo;
}
