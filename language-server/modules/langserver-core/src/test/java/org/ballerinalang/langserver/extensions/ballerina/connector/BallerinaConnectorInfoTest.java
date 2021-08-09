/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.connector;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test for connector info.
 */
public class BallerinaConnectorInfoTest {

    private BallerinaConnectorInfo connectorInfo;

    @BeforeClass
    public void initConnectorInfo() {
        connectorInfo = new BallerinaConnectorInfo(
                "ballerinax",
                "slack",
                "Client",
                "0.1.1",
                "1",
                "Slack",
                true,
                "service-connectors"
        );
    }

    @Test(description = "Test version from connector info object")
    public void getVersionTest() {
        String version = "1";
        Assert.assertEquals(version, connectorInfo.getCacheVersion());
    }

    @Test(description = "Test overloaded constructor with logo")
    public void createConnectorInfoWithLogo() {
        String encodedLogo = "bm90aGluZwo=";
        connectorInfo = new BallerinaConnectorInfo(
                "ballerinax",
                "slack",
                "Client",
                "0.1.1",
                "1",
                "Slack",
                encodedLogo,
                true,
                "service-connectors"
        );
        Assert.assertEquals(encodedLogo, connectorInfo.getLogoBase64Encoded());
    }
}
