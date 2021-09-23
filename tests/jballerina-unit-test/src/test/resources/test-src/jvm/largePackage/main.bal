// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/test;
import largePackage.clients as c;
import largePackage.services as s;
import largePackage.records as r;
import largePackage.objects as o;
import largePackage.errors as e;
import largePackage.enums as en;
import largePackage.unions as u;
import largePackage.constants as cnt;
import largePackage.tuples as t;

public function main() {
    c:Client|error myClient = checkpanic new();
    test:assertTrue(myClient is c:Client);
    s:MyService myService = new();
    o:Person300 p = new("waruna", 14);
    test:assertEquals(p.getAge(), 14);
    r:MyRecordV10 myRecord = {a:"Hello World"};
    test:assertEquals(myRecord.a, "Hello World");
    en:EN299|() myEnum = en:A299;
    en:MyZnum myZnum = en:Z1000;
    test:assertTrue(myEnum is en:EN299);
    e:MyError1 myError = error("My Error");
    test:assertEquals(myError.message(), "My Error");
    test:assertEquals(u:v1.a, "hello");
    test:assertEquals(cnt:MY_CONST1, 1);
    test:assertEquals(cnt:MY_CONST1000, "Ballerina1000");
    test:assertTrue(t:getLargeTupleArray() is anydata);
}
