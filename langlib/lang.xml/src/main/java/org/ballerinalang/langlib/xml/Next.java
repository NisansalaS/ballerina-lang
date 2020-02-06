/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.xml;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.BmpStringValue;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Native implementation of lang.xml.XMLIterator:next().
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml", functionName = "next",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "XMLIterator", structPackage = "ballerina/lang.xml"),
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class Next {
    //TODO: refactor hard coded values
    public static Object next(Strand strand, ObjectValue m) {
        IteratorValue xmlIterator = (IteratorValue) m.getNativeData("&iterator&");

        if (xmlIterator == null) {
            xmlIterator = ((XMLValue) m.get(new BmpStringValue("m"))).getIterator();
            m.addNativeData("&iterator&", xmlIterator);
        }

        if (xmlIterator.hasNext()) {
            Object xmlValue = xmlIterator.next();
            Map<String, BField> fields = new HashMap<>();
            BUnionType type = new BUnionType(Arrays.asList(BTypes.typeString, BTypes.typeXML));
            fields.put("value", new BField(type, "value", Flags.PUBLIC + Flags.REQUIRED));
            BRecordType recordType = new BRecordType("$$returnType$$", null, 0, fields,
                    null, true, 0);
            return BallerinaValues.createRecord(new MapValueImpl<>(recordType), xmlValue);
        }

        return null;
    }
}
