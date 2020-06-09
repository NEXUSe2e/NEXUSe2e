package org.nexuse2e.dbtest;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PurgeQueryIntegrationTest {

    @Container
    public MSSQLServerContainer mssqlserver = new MSSQLServerContainer()
            .acceptLicense();
    
    @Test
    void testWorks() {
    }
}
