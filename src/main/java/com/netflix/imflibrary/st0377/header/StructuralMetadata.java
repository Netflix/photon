/*
 *
 * Copyright 2015 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.netflix.imflibrary.st0377.header;


import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.st2067_201.IABEssenceDescriptor;
import com.netflix.imflibrary.st2067_201.IABSoundfieldLabelSubDescriptor;
import com.netflix.imflibrary.st2067_203.MGASoundEssenceDescriptor;
import com.netflix.imflibrary.st2067_203.MGAAudioMetadataSubDescriptor;
import com.netflix.imflibrary.st2067_203.MGASoundfieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.st2067_203.SADMAudioMetadataSubDescriptor;
import com.netflix.imflibrary.st2067_204.ADMAudioMetadataSubDescriptor;
import com.netflix.imflibrary.st2067_204.RIFFChunkDefinitionSubDescriptor;
import com.netflix.imflibrary.st2067_204.RIFFChunkReferencesSubDescriptor;
import com.netflix.imflibrary.st2067_204.ADM_CHNASubDescriptor;
import com.netflix.imflibrary.st2067_204.ADMChannelMapping;
import com.netflix.imflibrary.st2067_204.ADMSoundfieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.st379_2.ContainerConstraintsSubDescriptor;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.MXFPropertyPopulator;
import com.netflix.imflibrary.KLVPacket;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that contains helper methods to identify and populate Structural Metadata set objects
 */
public final class StructuralMetadata
{
    private StructuralMetadata()
    {
        //to prevent instantiation
    }

    private static final byte[] KEY_BASE = {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x00, 0x01, 0x00, 0x0d, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00};
    private static final byte[] KEY_MASK = {   1,    1,    1,    1,    1,    0,    1,    0,    1,    1,    1,    1,    1,    0,    0,    0};

    private static final byte[] DESCRIPTIVE_METADATA_KEY_BASE = {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x00, 0x01, 0x00, 0x0d, 0x01, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] DESCRIPTIVE_METADATA_KEY_MASK = {   1,    1,    1,    1,    1,    0,    1,    0,    1,    1,    1,    1,    0,    0,    0,    1};


    private static final byte[] PHDR_METADATA_TRACK_SUBDESCRIPTOR = {0x06, 0x0e, 0x2b, 0x34, 0x02, 0x53, 0x01, 0x05, 0x0e, 0x09, 0x06, 0x07, 0x01, 0x01, 0x01, 0x03};


    private static final Map<MXFUID, String> ItemULToItemName;
    static
    {
        Map<MXFUID, String> map = new HashMap<>();
        //Preface
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x15, 0x02, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "instance_uid");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x07, 0x02, 0x01, 0x10, 0x02, 0x04, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "last_modified_date");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x03, 0x01, 0x02, 0x01, 0x05, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "version");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x04, 0x06, 0x01, 0x01, 0x04, 0x01, 0x08, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "primary_package");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x04, 0x02, 0x01, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "content_storage");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x01, 0x02, 0x02, 0x03, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "operational_pattern");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x01, 0x02, 0x02, 0x10, 0x02, 0x01, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "essencecontainers");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x01, 0x02, 0x02, 0x10, 0x02, 0x02, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "dm_schemes");
        }
        //TimelineTrack
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x01, 0x07, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "track_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x01, 0x04, 0x01, 0x03, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "track_number");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x04, 0x02, 0x04, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "sequence");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x05, 0x30, 0x04, 0x05, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "edit_rate");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x07 , 0x02, 0x01, 0x03, 0x01, 0x03, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "origin");
        }
        //Event
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x07 , 0x02, 0x01, 0x03, 0x03, 0x03, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "event_start_position");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x05 , 0x30, 0x04, 0x04, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "event_comment");
        }
        //DMSegment
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x04, 0x01, 0x07, 0x01, 0x05, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "track_ids");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x06, 0x01, 0x01, 0x04, 0x02, 0x0c, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "dm_framework");
        }
        //CDCIPictureEssenceDescriptor
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x09, 0x06, 0x01, 0x01, 0x04, 0x06, 0x10, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "subdescriptors");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x04, 0x06, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "sample_rate");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x04, 0x01, 0x02, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "essence_container");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x04, 0x01, 0x03, 0x01, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "frame_layout");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x04, 0x01, 0x05, 0x02, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "stored_width");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x04, 0x01, 0x05, 0x02, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "stored_height");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x04, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "aspect_ratio");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x04, 0x01, 0x03, 0x02, 0x05, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "video_line_map");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x04, 0x01, 0x06, 0x01, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "picture_essence_coding");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x04, 0x01, 0x05, 0x03, 0x0A, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "component_depth");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x04, 0x01, 0x05, 0x01, 0x05, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "horizontal_subsampling");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x04, 0x01, 0x05, 0x01, 0x10, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "vertical_subsampling");
        }
        //Sequence
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x04, 0x07, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "data_definition");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x07, 0x02, 0x02, 0x01, 0x01, 0x03, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "duration");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x04, 0x06, 0x09, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "structural_components");
        }
        //SourceClip
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x07, 0x02, 0x01, 0x03, 0x01, 0x04, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "start_position");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x03, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "source_package_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x03, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "source_track_id");
        }
        //ContentStorage
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x04, 0x05, 0x01, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "packages");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x04, 0x05, 0x02, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "essencecontainer_data");
        }
        //EssenceContainerData
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x06, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "linked_package_uid");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x04, 0x01, 0x03, 0x04, 0x05, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "index_sid");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x04, 0x01, 0x03, 0x04, 0x04, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "body_sid");
        }
        //MaterialPackage
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x15, 0x10, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "package_uid");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x07, 0x02, 0x01, 0x10, 0x01, 0x03, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "package_creation_date");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x07, 0x02, 0x01, 0x10, 0x02, 0x05, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "package_modified_date");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x04, 0x06, 0x05, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "tracks");
        }
        //SourcePackage
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x06, 0x01, 0x01, 0x04, 0x02, 0x03, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "descriptor");
        }
        //WaveAudioEssenceDescriptor
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x04, 0x02, 0x03, 0x01, 0x01, 0x01, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "audio_sampling_rate");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x04, 0x02, 0x01, 0x01, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "channelcount");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x04, 0x04, 0x02, 0x03, 0x03, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "quantization_bits");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x04, 0x02, 0x04, 0x02, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "sound_essence_coding");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x04, 0x02, 0x03, 0x02, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "block_align");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x04, 0x02, 0x03, 0x03, 0x05, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "average_bytes_per_second");
        }
        {
            byte[] byteArray = {0x06, 0x0e ,0x2b ,0x34 ,0x01 ,0x01 ,0x01 ,0x07  ,0x04 ,0x02 ,0x01 ,0x01 ,0x05 ,0x00 ,0x00 ,0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "channel_assignment");
        }
        //GenericSoundEssenceDescriptor
        {
            byte[] byteArray = {0x06, 0x0e ,0x2b ,0x34 ,0x01 ,0x01 ,0x01 ,0x0e  ,0x04 ,0x02 ,0x01 ,0x01 ,0x06 ,0x00 ,0x00 ,0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "reference_image_edit_rate");
        }
        {
            byte[] byteArray = {0x06, 0x0e ,0x2b ,0x34 ,0x01 ,0x01 ,0x01 ,0x0e  ,0x04 ,0x02 ,0x01 ,0x01 ,0x07 ,0x00 ,0x00 ,0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "reference_audio_alignment_level");
        }
        //AudioChannelLabelSubDescriptor
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x01, 0x03, 0x07, 0x01, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_label_dictionary_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x01, 0x03, 0x07, 0x01, 0x05, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_link_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x01, 0x03, 0x07, 0x01, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_tag_symbol");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x01, 0x03, 0x07, 0x01, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_tag_name");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x01, 0x03, 0x04, 0x0a, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_channel_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0d, 0x03, 0x01, 0x01, 0x02, 0x03, 0x15, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "rfc_5646_spoken_language");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x01, 0x05, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_title");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x01, 0x05, 0x11, 0x00, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_title_version");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x03, 0x02, 0x01, 0x02, 0x20, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_audio_content_kind");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x03, 0x02, 0x01, 0x02, 0x21, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_audio_element_kind");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x03, 0x02, 0x01, 0x02, 0x22, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_content");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x03, 0x02, 0x01, 0x02, 0x23, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mca_use_class");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x01, 0x03, 0x07, 0x01, 0x06, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "soundfield_group_link_id");
        }

        /*{
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x01, 0x03, 0x07, 0x01, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "group_of_soundfield_groups_linkID");
        }*/
        //JPEG2000SubDescriptor
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "rSiz");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "xSiz");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "ySiz");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "xoSiz");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x05, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "yoSiz");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x06, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "xtSiz");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x07, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "ytSiz");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x08, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "xtoSiz");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x09, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "ytoSiz");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x0A, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "cSiz");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x0B, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "picture_component_sizing");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x0C, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "coding_style_default");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0a, 0x04, 0x01, 0x06, 0x03, 0x0D, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "quantisation_default");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x09, 0x04, 0x01, 0x02, 0x01, 0x01, 0x06, 0x01, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "color_primaries");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x04, 0x01, 0x02, 0x01, 0x01, 0x03, 0x01, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "coding_equations");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x02, 0x04, 0x01, 0x02, 0x01, 0x01, 0x01, 0x02, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "transfer_characteristic");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x04, 0x01, 0x05, 0x03, 0x0b, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "component_max_ref");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x04, 0x01, 0x05, 0x03, 0x0c, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "component_min_ref");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x04, 0x01, 0x05, 0x03, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "black_ref_level");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x01, 0x04, 0x01, 0x05, 0x03, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "white_ref_level");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0c, 0x01, 0x01, 0x15, 0x12, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "resource_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0c, 0x04, 0x09, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "ucs_encoding");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x08, 0x01, 0x02, 0x01, 0x05, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "namespace_uri");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x07, 0x04, 0x09, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mime_media_type");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x01, 0x02, 0x02, 0x10, 0x02, 0x04, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "conforms_to_specifications");
        }
        // ACESPictureSubDescriptor
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x0a, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "aces_authoring_information");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x0a, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "aces_mastering_display_primaries");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x0a, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "aces_mastering_white");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x0a, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "aces_max_luminance");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x0a, 0x05, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "aces_min_luminance");
        }
        //TargetFrameSubDescriptor
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x09, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "ancillary_resource_uid");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x09, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "media_type");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x09, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "target_frame_index");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x09, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "target_frame_transfer_characteristic");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x09, 0x05, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "color_primaries");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x09, 0x06, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "max_ref");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x09, 0x07, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "min_ref");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x09, 0x08, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "essence_stream_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x09, 0x09, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "aces_picture_subdescriptor_uid");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x01, 0x06, 0x09, 0x0a, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "viewing_environment");
        }
        //Text-based DM Framework
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0d, 0x06, 0x01, 0x01, 0x04, 0x05, 0x41, 0x01, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "text_based_object");
        }
        //Text-based Object
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0d, 0x04, 0x06, 0x08, 0x06, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "metadata_payload_scheme_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0d, 0x04, 0x09, 0x02, 0x02, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mime_type");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0d, 0x03, 0x01, 0x01, 0x02, 0x02, 0x14, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "language_code");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0d, 0x03, 0x02, 0x01, 0x06, 0x03, 0x02, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "description");
        }
        //Generic Stream Text-based Set
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0d, 0x01, 0x03, 0x04, 0x08, 0x00, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "generic_stream_id");
        }
        // MGA ST 2127-1
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x04, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mga_sound_essence_block_align");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x04, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mga_sound_average_bytes_per_second");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x04, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mga_sound_essence_sequence_offset");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x05, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mga_link_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x05, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mga_audio_metadata_index");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x05, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mga_audio_metadata_identifier");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x05, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mga_audio_metadata_payload_ul_array");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x06, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "mga_metadata_section_link_id");
        }
        // MGA S-ADM ST 2127-10
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x06, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "adm_audio_programme_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x06, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "adm_audio_content_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x07, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "sadm_metadata_section_link_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x07, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "sadm_profile_level_batch");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x0a, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "riff_chunk_stream_id_link1");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x0a, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "adm_profile_level_batch");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x08, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "riff_chunk_stream_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x08, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "riff_chunk_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x08, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "riff_chunk_uuid");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x08, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "riff_chunk_sha1");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x08, 0x06, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "riff_chunk_stream_ids_array");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x09, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "num_local_channels");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x09, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "num_adm_audio_track_uids");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x09, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "adm_channel_mappings_array");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x09, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "local_channel_id");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x09, 0x05, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "adm_audio_track_uid");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x09, 0x06, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "adm_audio_track_channel_format_uid");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x09, 0x07, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "adm_audio_pack_format_uid");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x0b, 0x01, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "riff_chunk_stream_id_link2");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x0b, 0x02, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "adm_audio_programme_id_st2131");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x0b, 0x03, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "adm_audio_content_id_st2131");
        }
        {
            byte[] byteArray = {0x06, 0x0e, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x0e, 0x04, 0x02, 0x03, 0x0b, 0x04, 0x00, 0x00, 0x00};
            MXFUID mxfUL = new MXFUID(byteArray);
            map.put(mxfUL, "adm_audio_object_id_st2131");
        }
        ItemULToItemName = Collections.unmodifiableMap(map);
    }

    /**
     * A method that determines of the key passed in corresponds to a structural metadata set.
     *
     * @param key the key
     * @return the boolean
     */
    public static boolean isStructuralMetadata(byte[] key)
    {
        if (isPHDRMetadataTrackSubDescriptor(key))
        {
            return true;
        }

        for (int i=0; i< KLVPacket.KEY_FIELD_SIZE; i++)
        {
            if( (StructuralMetadata.KEY_MASK[i] != 0) && (StructuralMetadata.KEY_BASE[i] != key[i]) )
            {
                return false;
            }
        }

        return ((key[5] == 0x53) || (key[5] == 0x13));

    }

    /**
     * A method that determines of the key passed in corresponds to a descriptive metadata set.
     *
     * @param key the key
     * @return the boolean
     */
    public static boolean isDescriptiveMetadata(byte[] key)
    {
        for (int i=0; i< KLVPacket.KEY_FIELD_SIZE; i++)
        {
            if( (StructuralMetadata.DESCRIPTIVE_METADATA_KEY_MASK[i] != 0) && (StructuralMetadata.DESCRIPTIVE_METADATA_KEY_BASE[i] != key[i]) )
            {
                return false;
            }
        }

        return ((key[5] == 0x53) || (key[5] == 0x13));

    }

    /**
     * A method that determines if the key passed in corresponds to a PHDRMetadataTrackSubDescriptor.
     *
     * @param key the key
     * @return the boolean
     */
    public static boolean isPHDRMetadataTrackSubDescriptor(byte[] key)
    {
        return Arrays.equals(key, StructuralMetadata.PHDR_METADATA_TRACK_SUBDESCRIPTOR);
    }

    public static boolean isAudioWaveClipWrapped(int contentKind){
        if(contentKind == 0x02){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Gets structural metadata set class object.
     * Note: For all structural metadata set items that we do not read we will return an Object.class
     * @param key the key
     * @return the structural metadata set name
     */
    public static Class getStructuralMetadataSetClass(byte[] key)
    {

        if (isPHDRMetadataTrackSubDescriptor(key))
        {
            return PHDRMetaDataTrackSubDescriptor.PHDRMetaDataTrackSubDescriptorBO.class;
        }
        else if (isStructuralMetadata(key) && (key[13] == 0x01))
        {
            switch (key[14])
            {
                case 0x2f :
                    return Preface.PrefaceBO.class;
                case 0x30 :
                    return Object.class; //Identification
                case 0x18 :
                    return ContentStorage.ContentStorageBO.class;
                case 0x23 :
                    return EssenceContainerData.EssenceContainerDataBO.class;
                case 0x36 :
                    return MaterialPackage.MaterialPackageBO.class;
                case 0x37 :
                    return SourcePackage.SourcePackageBO.class;
                case 0x3b :
                    return TimelineTrack.TimelineTrackBO.class;
                case 0x39 :
                    return Object.class; //Event Track
                case 0x3A :
                    return StaticTrack.StaticTrackBO.class;
                case 0x0F:
                    return Sequence.SequenceBO.class;
                case 0x11 :
                    return SourceClip.SourceClipBO.class;
                case 0x14 :
                    return TimecodeComponent.TimecodeComponentBO.class;
                case 0x41 :
                    return DescriptiveMarkerSegment.DescriptiveMarkerSegmentBO.class; //DM Segment
                case 0x45 :
                    return Object.class; //DM Source Clip
                case 0x09 :
                    return Object.class; //Filler
                case 0x60 :
                    return Object.class; //Package Marker Object
                case 0x25 :
                    return FileDescriptor.FileDescriptorBO.class;
                case 0x27 :
                    return GenericPictureEssenceDescriptor.GenericPictureEssenceDescriptorBO.class;
                case 0x28 :
                    return CDCIPictureEssenceDescriptor.CDCIPictureEssenceDescriptorBO.class;
                case 0x29 :
                    return RGBAPictureEssenceDescriptor.RGBAPictureEssenceDescriptorBO.class;
                case 0x42 :
                    return GenericSoundEssenceDescriptor.GenericSoundEssenceDescriptorBO.class;
                case 0x43 :
                    return Object.class; //Generic Data Essence Descriptor
                case 0x44 :
                    return Object.class; //Multiple Descriptor
                case 0x32 :
                    return Object.class; //Network Locator
                case 0x33 :
                    return Object.class; //Text Locator
                case 0x61 :
                    return Object.class; //Application Plug-In Object
                case 0x62 :
                    return Object.class; //Application Referenced Object
                case 0x48 :
                    return WaveAudioEssenceDescriptor.WaveAudioEssenceDescriptorBO.class;
                case 0x64 :
                    return TimedTextDescriptor.TimedTextDescriptorBO.class;
                case 0x65 :
                    return TimeTextResourceSubDescriptor.TimeTextResourceSubdescriptorBO.class;
                case 0x67 :
                    return ContainerConstraintsSubDescriptor.ContainerConstraintsSubDescriptorBO.class;
                case 0x6b :
                    return AudioChannelLabelSubDescriptor.AudioChannelLabelSubDescriptorBO.class;
                case 0x6c :
                    return SoundFieldGroupLabelSubDescriptor.SoundFieldGroupLabelSubDescriptorBO.class;
                case 0x6d :
                    return GroupOfSoundFieldGroupLabelSubDescriptor.GroupOfSoundFieldGroupLabelSubDescriptorBO.class;
                case 0x5A :
                    return JPEG2000PictureSubDescriptor.JPEG2000PictureSubDescriptorBO.class;
                case 0x7B :
                    return IABEssenceDescriptor.IABEssenceDescriptorBO.class;
                case 0x7C :
                    return IABSoundfieldLabelSubDescriptor.IABSoundfieldLabelSubDescriptorBO.class;
                case 0x79 :
                    return ACESPictureSubDescriptor.ACESPictureSubDescriptorBO.class;
                case 0x7a :
                    return TargetFrameSubDescriptor.TargetFrameSubDescriptorBO.class;
                case (byte)0x81 :
                    switch(key[15]) {
                    case 0x06:
                        return MGASoundEssenceDescriptor.MGASoundEssenceDescriptorBO.class;
                    case 0x07:
                        return MGAAudioMetadataSubDescriptor.MGAAudioMetadataSubDescriptorBO.class;
                    case 0x08:
                        return MGASoundfieldGroupLabelSubDescriptor.MGASoundfieldGroupLabelSubDescriptorBO.class;
                    case 0x09:
                        return SADMAudioMetadataSubDescriptor.SADMAudioMetadataSubDescriptorBO.class;
                    case 0x0d:
                        return RIFFChunkDefinitionSubDescriptor.RIFFChunkDefinitionSubDescriptorBO.class;
                    case 0x0e:
                        return ADM_CHNASubDescriptor.ADM_CHNASubDescriptorBO.class;
                    case 0x0f:
                        return ADMChannelMapping.ADMChannelMappingBO.class;
                    case 0x10:
                        return RIFFChunkReferencesSubDescriptor.RIFFChunkReferencesSubDescriptorBO.class;
                    case 0x11:
                        return ADMAudioMetadataSubDescriptor.ADMAudioMetadataSubDescriptorBO.class;
                    case 0x12:
                        return ADMSoundfieldGroupLabelSubDescriptor.ADMSoundfieldGroupLabelSubDescriptorBO.class;
                    default :
                        return Object.class;
                    }
                default :
                    return Object.class;
            }
        }
        else if (isDescriptiveMetadata(key)) { // Metadata Sets for the Text-based Metadata
            if (key[13] == 0x02 && key[14] == 0x01) {
                return GenericStreamTextBasedSet.GenericStreamTextBasedSetBO.class;
            } else if (key[13] == 0x01 && key[14] == 0x01) {
                return TextBasedDMFramework.TextBasedDMFrameworkBO.class;
            } else {
                return Object.class;
            }
        }
        else
        {
            return Object.class;
        }
    }

    /**
     * A method that populates the fields of a Structural Metadata set
     *
     * @param object the InterchangeObjectBO object
     * @param byteProvider the mxf byte provider
     * @param numBytesToRead the num bytes to read
     * @param localTagToUIDMap the local tag to uID map
     * @throws IOException the iO exception
     */
    public static void populate(InterchangeObject.InterchangeObjectBO object, ByteProvider byteProvider, long numBytesToRead, Map<Integer, MXFUID> localTagToUIDMap)
            throws IOException
    {
        long numBytesRead = 0;
        while (numBytesRead < numBytesToRead)
        {
            /*From smpte st 377-1:2011 section 9.6, all structural header metadata objects shall be implemented as MXF Local Sets
            which implies that the data item local tag is always 2 bytes long*/
            //read local tag
            int localTag = MXFPropertyPopulator.getUnsignedShortAsInt(byteProvider.getBytes(2), KLVPacket.BYTE_ORDER);
            numBytesRead += 2;

            //read length
            long length;
            if (object.getHeader().getRegistryDesignator() == 0x53)
            {
                length = MXFPropertyPopulator.getUnsignedShortAsInt(byteProvider.getBytes(2), KLVPacket.BYTE_ORDER);
                numBytesRead += 2;
            }
            else if (object.getHeader().getRegistryDesignator() == 0x13)
            {
                KLVPacket.LengthField lengthField = KLVPacket.getLength(byteProvider);
                length = lengthField.value;
                numBytesRead += lengthField.sizeOfLengthField;
            }
            else
            {
                throw new MXFException(String.format("Byte 5 (zero-indexed) for MXF Structural Metadata key = %s is invalid",
                        Arrays.toString(object.getHeader().getKey())));
            }

            //read or skip value
            MXFUID mxfUL = localTagToUIDMap.get(localTag);
            if ((mxfUL != null) && (StructuralMetadata.ItemULToItemName.get(mxfUL) != null))
            {
                String itemName = StructuralMetadata.ItemULToItemName.get(mxfUL);
                int expectedLength = MXFPropertyPopulator.getFieldSizeInBytes(object, itemName);
                if((expectedLength > 0) && (length != expectedLength))
                {
                    throw new MXFException(String.format("Actual length from bitstream = %d is different from expected length = %d",
                            length, expectedLength));
                }
                MXFPropertyPopulator.populateField((int) length, byteProvider, object, itemName);
            }
            else
            {
                byteProvider.skipBytes(length);
            }
            numBytesRead += length;
        }

    }

}
