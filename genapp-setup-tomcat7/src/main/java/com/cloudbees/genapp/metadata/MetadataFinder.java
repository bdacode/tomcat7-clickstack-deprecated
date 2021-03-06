package com.cloudbees.genapp.metadata;

 /*
 * Copyright 2010-2013, CloudBees Inc.
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

import java.io.File;
import java.util.Map;

/**
 * The MetadataFinder class is used to create Metadata instances either from a default path to the metadata.json,
 * or otherwise, if a default path isn't specified, from $genapp_dir/metadata.json.
 *
 * Typical usage:
 * Metadata metadata = (new MetadataFinder(defaultPath)).getMetadata();
 */

public class MetadataFinder {

    private Metadata metadata;

    /**
     * Creates a new MetadataFinder instance from the metadata at $genapp_dir/metadata.json
     * @throws Exception
     */
    public MetadataFinder() throws Exception {
        this(null);
    }

    /**
     * Creates a new MetadataFinder instance from a given path to metadata.json.
     * @param defaultMetadataPath The absolute path to metadata.json
     * @throws Exception
     */
    public MetadataFinder(String defaultMetadataPath) throws Exception {
        Map<String, String> env = System.getenv();
        String metadataPath;

        if (defaultMetadataPath != null)
            metadataPath = defaultMetadataPath;
        else
            metadataPath = env.get("genapp_dir") + "/metadata.json";

        // Locate genapp's metadata.json
        File metadataJson = new File(metadataPath);
        if (!metadataJson.exists())
            throw new Exception("Missing metadata file: " + metadataJson.getAbsolutePath());

        metadata = Metadata.Builder.fromFile(metadataJson);
    }

    /**
     * Gets the Metadata instance generated by this class' constructor.
     * @return The Metadata instance generated by this class' constructor.
     */
    public Metadata getMetadata() {
        return metadata;
    }
}
