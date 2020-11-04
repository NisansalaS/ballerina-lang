/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.central.client;

import org.awaitility.Duration;
import org.ballerinalang.central.client.model.Package;
import org.ballerinalang.central.client.model.PackageSearchResult;
import org.ballerinalang.central.client.util.CommandException;
import org.ballerinalang.central.client.util.NoPackageException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.projects.utils.ProjectConstants.BALLERINA_INSTALL_DIR_PROP;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.ballerinalang.central.client.util.TestUtils.cleanDirectory;
import static org.ballerinalang.central.client.util.CentralClientConstants.CONTENT_DISPOSITION;
import static org.ballerinalang.central.client.util.CentralClientConstants.LOCATION;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases to test central api client.
 */
public class TestCentralApiClient extends CentralAPIClient {

    private HttpURLConnection connection = mock(HttpURLConnection.class);
    private ByteArrayOutputStream console;
    private ByteArrayOutputStream errConsole;
    private static final Path utilsTestResources = Paths.get("src/test/resources/test-resources/utils");
    private static final Path tmpDir = utilsTestResources.resolve("temp-test-central-api-client");

    @Override
    protected HttpURLConnection createHttpUrlConnection(String a) {
        return connection;
    }

    @BeforeClass
    public void setup() throws IOException {
        if (!tmpDir.toFile().exists()) {
            Files.createDirectory(tmpDir);
        }
    }

    private String readOutput() throws IOException {
        String output;
        output = this.console.toString();
        this.console.close();
        this.console = new ByteArrayOutputStream();
        this.outStream = new PrintStream(this.console);
        return output;
    }

    private String readErrorOutput() throws IOException {
        String output;
        output = this.errConsole.toString();
        this.errConsole.close();
        this.errConsole = new ByteArrayOutputStream();
        this.errStream = new PrintStream(this.errConsole);
        return output;
    }

    @BeforeMethod
    public void beforeMethod() {
        this.console = new ByteArrayOutputStream();
        this.errConsole = new ByteArrayOutputStream();
        this.outStream = new PrintStream(this.console);
        this.errStream = new PrintStream(this.errConsole);
    }

    private void cleanTmpDir() {
        cleanDirectory(tmpDir);
    }

    @AfterClass
    public void cleanup() throws IOException {
        cleanTmpDir();
        Files.deleteIfExists(tmpDir);
    }

    @Test(description = "Test pull package")
    public void testPullPackage() throws IOException {
        final String baloUrl = "https://fileserver.dev-central.ballerina.io/2.0/wso2/sf/1.3.5/sf-2020r2-any-1.3.5.balo";
        Path baloPath = utilsTestResources.resolve("sf-any.balo");
        File baloFile = new File(String.valueOf(baloPath));
        InputStream baloStream = new FileInputStream(baloFile);

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_MOVED_TEMP);
        when(connection.getHeaderField(LOCATION)).thenReturn(baloUrl);
        when(connection.getHeaderField(CONTENT_DISPOSITION))
                .thenReturn("attachment; filename=sf-2020r2-any-1.3.5.balo");
        when(connection.getContentLengthLong()).thenReturn(Files.size(baloPath));
        when(connection.getInputStream()).thenReturn(baloStream);

        this.pullPackage("foo", "sf", "1.3.5", tmpDir, "any", false);

        Assert.assertTrue(tmpDir.resolve("1.3.5").resolve("sf-2020r2-any-1.3.5.balo").toFile().exists());
        String buildLog = readOutput();
        given().with().pollInterval(Duration.ONE_SECOND).and()
                .with().pollDelay(Duration.ONE_SECOND)
                .await().atMost(10, SECONDS)
                .until(() -> buildLog.contains("foo/sf:1.3.5 pulled from central successfully"));
        cleanTmpDir();
    }

    @Test(description = "Test pull non existing package", expectedExceptions = CommandException.class,
            expectedExceptionsMessageRegExp = "error: package not found: foo/sf.*")
    public void testPullNonExistingPackage() throws IOException {
        String resString = "{\"message\": \"package not found: foo/sf:*_any\"}";
        InputStream resStream = new ByteArrayInputStream(resString.getBytes());

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(connection.getErrorStream()).thenReturn(resStream);

        this.pullPackage("foo", "sf", "1.3.5", tmpDir, "any", false);
    }

    @Test(description = "Test get package")
    public void testGetPackage() throws IOException {
        Path packageJsonPath = utilsTestResources.resolve("package.json");
        File packageJson = new File(String.valueOf(packageJsonPath));

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(connection.getInputStream()).thenReturn(new FileInputStream(packageJson));

        Package aPackage = this.getPackage("foo", "winery", "1.3.5", "any");
        Assert.assertNotNull(aPackage);
        Assert.assertEquals(aPackage.getOrganization(), "foo");
        Assert.assertEquals(aPackage.getName(), "winery");
        Assert.assertEquals(aPackage.getVersion(), "1.3.5");
    }

    @Test(description = "Test get non existing package", expectedExceptions = NoPackageException.class,
            expectedExceptionsMessageRegExp = "package not found: bar/winery:2.0.0_any")
    public void testGetNonExistingPackage() throws IOException {
        String resString = "{\"message\": \"package not found: bar/winery:2.0.0_any\"}";

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(resString.getBytes()));

        this.getPackage("bar", "winery", "2.0.0", "any");
    }

    @Test(description = "Test get package with bad request", expectedExceptions = CommandException.class,
            expectedExceptionsMessageRegExp = "invalid request received. invaild/unsupported semver version: v2")
    public void testGetPackageWithBadRequest() throws IOException {
        String resString = "{\"message\": \"invalid request received. invaild/unsupported semver version: v2\"}";

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(resString.getBytes()));

        this.getPackage("bar", "winery", "v2", "any");
    }

    @Test(description = "Test push package")
    public void testPushPackage() throws IOException {
        Path baloPath = utilsTestResources.resolve("sf-any.balo");
        File outputBalo = new File(String.valueOf(tmpDir.resolve("output.balo")));

        setBallerinaHome();

        try (FileOutputStream outputStream = new FileOutputStream(outputBalo)) {
            when(connection.getOutputStream()).thenReturn(outputStream);
            when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

            this.pushPackage(baloPath);
            String buildLog = readOutput();
            given().with().pollInterval(Duration.ONE_SECOND).and()
                    .with().pollDelay(Duration.ONE_SECOND)
                    .await().atMost(10, SECONDS)
                    .until(() -> buildLog.contains("foo/sf:1.3.5 pushed to central successfully"));
        }
    }

    @Test(description = "Test push package with invalid access token")
    public void testPushPackageWithInvalidAccessToken() throws IOException {
        Path baloPath = utilsTestResources.resolve("sf-any.balo");
        File outputBalo = new File(String.valueOf(tmpDir.resolve("output.balo")));

        setBallerinaHome();

        try (FileOutputStream outputStream = new FileOutputStream(outputBalo)) {
            when(connection.getOutputStream()).thenReturn(outputStream);
            when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);

            this.pushPackage(baloPath);
            String errorLog = readErrorOutput();
            given().with().pollInterval(Duration.ONE_SECOND).and()
                    .with().pollDelay(Duration.ONE_SECOND)
                    .await().atMost(10, SECONDS)
                    .until(() -> errorLog.contains("unauthorized access token for organization: foo"));
        }
    }

    @Test(description = "Test push existing package", expectedExceptions = CommandException.class,
            expectedExceptionsMessageRegExp = "package already exists: foo/github:1.8.3_2020r2_any")
    public void testPushExistingPackage() throws IOException {
        String resString = "{\"message\": \"package already exists: foo/github:1.8.3_2020r2_any\"}";
        Path baloPath = utilsTestResources.resolve("sf-any.balo");
        File outputBalo = new File(String.valueOf(tmpDir.resolve("output.balo")));

        setBallerinaHome();

        try (FileOutputStream outputStream = new FileOutputStream(outputBalo)) {
            when(connection.getOutputStream()).thenReturn(outputStream);
            when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
            when(connection.getErrorStream()).thenReturn(new ByteArrayInputStream(resString.getBytes()));

            this.pushPackage(baloPath);
        }
    }

    @Test(description = "Test push package request failure", expectedExceptions = CommandException.class,
            expectedExceptionsMessageRegExp = "error: failed to push the package: "
                    + "'foo/sf:1.3.5' to the remote repository 'https://api.central.ballerina.io/registry'")
    public void testPushPackageRequestFailure() throws IOException {
        Path baloPath = utilsTestResources.resolve("sf-any.balo");
        File outputBalo = new File(String.valueOf(tmpDir.resolve("output.balo")));

        setBallerinaHome();

        try (FileOutputStream outputStream = new FileOutputStream(outputBalo)) {
            when(connection.getOutputStream()).thenReturn(outputStream);
            when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
            when(connection.getURL()).thenReturn(new URL("https://api.central.ballerina.io/registry"));

            this.pushPackage(baloPath);
        }
    }

    @Test(description = "Test search package")
    public void testSearchPackage() throws IOException {
        Path packageSearchJsonPath = utilsTestResources.resolve("packageSearch.json");
        File packageSearchJson = new File(String.valueOf(packageSearchJsonPath));

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(connection.getInputStream()).thenReturn(new FileInputStream(packageSearchJson));

        PackageSearchResult pkgSearchResult = this.searchPackage("org=foo");
        Assert.assertNotNull(pkgSearchResult);
        Assert.assertEquals(pkgSearchResult.getCount(), 1);
        Assert.assertFalse(pkgSearchResult.getPackages().isEmpty());
        Assert.assertEquals(pkgSearchResult.getPackages().get(0).getOrganization(), "foo");
    }

    @Test(description = "Test search package with bad request", expectedExceptions = CommandException.class,
            expectedExceptionsMessageRegExp = "invalid request received. invaild/unsupported org name: foo-org")
    public void testSearchPackageWithBadRequest() throws IOException {
        String resString = "{\"message\": \"invalid request received. invaild/unsupported org name: foo-org\"}";

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
        when(connection.getErrorStream()).thenReturn(new ByteArrayInputStream(resString.getBytes()));

        this.searchPackage("org=foo-org");
    }

    private void setBallerinaHome() {
        if (System.getProperty(BALLERINA_INSTALL_DIR_PROP) == null) {
            System.setProperty(BALLERINA_INSTALL_DIR_PROP, String.valueOf(Paths.get("build")));
        }
    }
}
