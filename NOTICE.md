Photon is an implementation of the Material Exchange Format (MXF) specification SMPTE st0377-1:2011 constrained by
SMPTE IMF-Essence Component SMPTE st2067-5:2013 
Copyright (C) 2015 Netflix, Inc.

RegXMLLib an open source library hosted on github is utilized to serialize the metadata in the MXF
essence into a SMPTE IMF-Composition Playlist (SMPTE st2067-3:2013).

/*
 * Copyright (c) 2014, Pierre-Anthony Lemieux (pal@sandflow.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * *  Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * *  Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

The source includes XML Schema definitions and xml documents in the resources/ directory.

The schema definitions imf-core-constraints-20130620-pal.xsd, imf-cpl.xsd and dcmlTypes.xsd are
released and maintained by the SMPTE Registration Authority, LLC and have been included as is.
The xmldsig-core-schema.xsd schema definition is released and maintained by the W3C and has been
included as is.

The xml documents Elements.xml, Groups.xml, Labels.xml and Types.xml are released and maintained by the
SMPTE Registration Authority, LLC and have been included as is.
