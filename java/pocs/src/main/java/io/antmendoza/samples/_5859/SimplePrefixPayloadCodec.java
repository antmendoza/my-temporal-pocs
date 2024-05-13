/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.antmendoza.samples._5859;

import io.temporal.api.common.v1.Payload;
import io.temporal.payload.codec.PayloadCodec;

import java.util.List;
import java.util.stream.Collectors;

public class SimplePrefixPayloadCodec implements PayloadCodec {


    @Override
    public List<Payload> encode(List<Payload> payloads) {

        System.out.println("Encoding ---> " +  payloads);

        return payloads.stream().map(this::encode).collect(Collectors.toList());
    }

    private Payload encode(Payload decodedPayload) {
        return decodedPayload;
    }


    @Override
    public List<Payload> decode(List<Payload> payloads) {


        System.out.println("Decoding ---> " +  payloads);
        return payloads.stream().map(this::decode).collect(Collectors.toList());
    }

    private Payload decode(Payload encodedPayload) {
        return encodedPayload;
    }
}
