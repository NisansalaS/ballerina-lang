/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.runtime.nativeimpl;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.util.BLangConstants;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.ArrayValueImpl;
import io.ballerina.runtime.values.ErrorValue;

import java.util.List;

/**
 * Native implementation for get error's call stack.
 *
 * @since 0.963.0
 */
public class GetCallStack {

    public static BArray getCallStack() {
        BError error = ErrorCreator.createError(StringUtils.fromString(""));
        List<StackTraceElement> filteredStack =  error.getCallStack();
        Type recordType = ValueCreator.createRecordValue(BLangConstants.BALLERINA_RUNTIME_PKG_ID,
                "CallStackElement").getType();
        ArrayValue callStack = new ArrayValueImpl(new BArrayType(recordType));
        for (int i = 0; i < filteredStack.size(); i++) {
            callStack.add(i, ErrorValue.getStackFrame(filteredStack.get(i)));
        }
        return callStack;
    }
}
