/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.runtime.documentation.component.service.http.codec;

import java.lang.reflect.Type;

import org.talend.sdk.component.api.service.http.ContentType;
import org.talend.sdk.component.api.service.http.Decoder;

@ContentType("application/json")
public class RecordSizeDecoder implements Decoder {

    @Override
    public Object decode(final byte[] value, final Type expectedType) {
        if (value == null) {
            return 0;
        }

        return Long.valueOf(value.length);
    }
}
