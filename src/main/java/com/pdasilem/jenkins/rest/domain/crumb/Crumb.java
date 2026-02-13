/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pdasilem.jenkins.rest.domain.crumb;

import com.pdasilem.jenkins.rest.domain.common.Error;
import com.pdasilem.jenkins.rest.domain.common.ErrorsHolder;

import java.util.List;

public record Crumb(String value, String sessionIdCookie, List<Error> errors)
        implements ErrorsHolder {

    public Crumb {
        errors = errors != null ? List.copyOf(errors) : List.of();
    }

    public static Crumb create(final String value,
            final List<Error> errors) {
        return new Crumb(value, null, errors);
    }

    public static Crumb create(final String value, final String sessionIdCookie) {
        return new Crumb(value, sessionIdCookie, null);
    }
}
