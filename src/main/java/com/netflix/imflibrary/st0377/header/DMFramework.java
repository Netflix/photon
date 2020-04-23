package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.KLVPacket;

import javax.annotation.concurrent.Immutable;

public class DMFramework extends InterchangeObject {

    private final DMFrameworkBO dmFrameworkBO;

    public DMFramework(DMFrameworkBO dmFrameworkBO) {
        this.dmFrameworkBO = dmFrameworkBO;
    }
    /**
     * Object corresponding to a parsed DMFramework structural metadata set defined in RP 2057:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static class DMFrameworkBO extends InterchangeObjectBO {
        public DMFrameworkBO(KLVPacket.Header header) {
            super(header);
        }
    }

}
