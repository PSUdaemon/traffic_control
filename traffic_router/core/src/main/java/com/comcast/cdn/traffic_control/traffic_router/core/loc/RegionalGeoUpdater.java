/*
 * Copyright 2015 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comcast.cdn.traffic_control.traffic_router.core.loc;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class RegionalGeoUpdater extends AbstractServiceUpdater {
    private static final Logger LOGGER = Logger.getLogger(RegionalGeoUpdater.class);

    public RegionalGeoUpdater() {
        LOGGER.debug("init...");
        sourceCompressed = false;
        tmpPrefix = "regionalgeo";
        tmpSuffix = ".json";
    }

    public boolean loadDatabase() throws IOException {
        final File existingDB = new File(databasesDirectory, databaseName);
        RegionalGeo.parseConfigFile(existingDB);
        setLoaded(true);
        return true;
    }
}

