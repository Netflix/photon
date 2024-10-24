package com.netflix.imflibrary;

import com.netflix.imflibrary.st0377.header.J2KExtendedCapabilities;
import com.netflix.imflibrary.st0377.header.JPEG2000PictureComponent;
import com.netflix.imflibrary.st0377.header.JPEG2000PictureSubDescriptor;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class J2KHeaderParameters {

    public static class CSiz {
        public short ssiz;
        public short xrsiz;
        public short yrsiz;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CSiz cSiz = (CSiz) o;
            return ssiz == cSiz.ssiz && xrsiz == cSiz.xrsiz && yrsiz == cSiz.yrsiz;
        }

        @Override
        public int hashCode() {
            return Objects.hash(ssiz, xrsiz, yrsiz);
        }
    }

    public static class CAP {
        public long pcap;
        public int[] ccap;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CAP cap = (CAP) o;
            return pcap == cap.pcap && Arrays.equals(ccap, cap.ccap);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(pcap);
            result = 31 * result + Arrays.hashCode(ccap);
            return result;
        }
    }

    public static class COD {
        public short scod;
        public short progressionOrder;
        public int numLayers;
        public short multiComponentTransform;
        public short numDecompLevels;
        public short xcb;
        public short ycb;
        public short cbStyle;
        public short transformation;
        public short precinctSizes[];

        public short getScod() { return scod; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            COD cod = (COD) o;
            return scod == cod.scod &&
                    progressionOrder == cod.progressionOrder &&
                    numLayers == cod.numLayers &&
                    multiComponentTransform == cod.multiComponentTransform &&
                    numDecompLevels == cod.numDecompLevels &&
                    xcb == cod.xcb &&
                    ycb == cod.ycb &&
                    cbStyle == cod.cbStyle &&
                    transformation == cod.transformation &&
                    Arrays.equals(precinctSizes, cod.precinctSizes);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(scod, progressionOrder, numLayers, multiComponentTransform, numDecompLevels, xcb, ycb, cbStyle, transformation);
            result = 31 * result + Arrays.hashCode(precinctSizes);
            return result;
        }
    }

    public static class QCD {
        public short sqcd;
        public int spqcd[];

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QCD qcd = (QCD) o;
            return sqcd == qcd.sqcd && Arrays.equals(spqcd, qcd.spqcd);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(sqcd);
            result = 31 * result + Arrays.hashCode(spqcd);
            return result;
        }
    }

    public Integer rsiz;
    public Long xsiz;
    public Long ysiz;
    public Long xosiz;
    public Long yosiz;
    public Long xtsiz;
    public Long ytsiz;
    public Long xtosiz;
    public Long ytosiz;
    public CSiz[] csiz;
    public COD cod;
    public QCD qcd;
    public CAP cap;

    public J2KHeaderParameters() {}

    // From CPL Descriptor to common J2KHeaderParameters
    public static J2KHeaderParameters fromDOMNode(DOMNodeObjectModel imageEssencedescriptorDOMNode) {
        J2KHeaderParameters p = new J2KHeaderParameters();

        DOMNodeObjectModel sdNode = imageEssencedescriptorDOMNode.getDOMNode("SubDescriptors");
        if (sdNode == null) {
            return null;
        }

        DOMNodeObjectModel j2kNode = sdNode.getDOMNode("JPEG2000SubDescriptor");
        if (j2kNode == null) {
            return null;
        }

        p.rsiz = j2kNode.getFieldAsInteger("Rsiz");
        if (p.rsiz == null) return null;

        p.xsiz = j2kNode.getFieldAsLong("Xsiz");
        if (p.xsiz == null) return null;

        p.ysiz = j2kNode.getFieldAsLong("Ysiz");
        if (p.ysiz == null) return null;

        p.xosiz = j2kNode.getFieldAsLong("XOsiz");
        if (p.xosiz == null) return null;

        p.yosiz = j2kNode.getFieldAsLong("YOsiz");
        if (p.yosiz == null) return null;

        p.xtsiz = j2kNode.getFieldAsLong("XTsiz");
        if (p.xtsiz == null) return null;

        p.ytsiz = j2kNode.getFieldAsLong("YTsiz");
        if (p.ytsiz == null) return null;

        p.xtosiz = j2kNode.getFieldAsLong("XTOsiz");
        if (p.xtosiz == null) return null;

        p.ytosiz = j2kNode.getFieldAsLong("YTOsiz");
        if (p.ytosiz == null) return null;

        // CSiz
        DOMNodeObjectModel csiziNode = j2kNode.getDOMNode("PictureComponentSizing");
        if (csiziNode == null) return null;

        List<DOMNodeObjectModel> csizi = csiziNode.getDOMNodes("J2KComponentSizing");
        p.csiz = new CSiz[csizi.size()];
        for (int i = 0; i < p.csiz.length; i++) {
            p.csiz[i] = new CSiz();

            Short ssiz = csizi.get(i).getFieldAsShort("Ssiz");
            if (ssiz == null) return null;
            p.csiz[i].ssiz = ssiz;

            Short xrsiz = csizi.get(i).getFieldAsShort("XRSiz");
            if (xrsiz == null) return null;
            p.csiz[i].xrsiz = xrsiz;

            Short yrsiz = csizi.get(i).getFieldAsShort("YRSiz");
            if (yrsiz == null) return null;
            p.csiz[i].yrsiz = yrsiz;
        }

        Integer csiz = j2kNode.getFieldAsInteger("Csiz");
        if (csiz != p.csiz.length) return null;

        // CAP
        DOMNodeObjectModel capNode = j2kNode.getDOMNode("J2KExtendedCapabilities");
        if (capNode != null) {
            Integer pcap = capNode.getFieldAsInteger("Pcap");
            if (pcap != null) {
                p.cap = new CAP();
                p.cap.pcap = pcap;

                DOMNodeObjectModel ccapiNode = capNode.getDOMNode("Ccapi");
                if (ccapiNode != null) {
                    List<Integer> values = ccapiNode.getFieldsAsInteger("UInt16");

                    p.cap.ccap = new int[values.size()];
                    for (int i = 0; i < p.cap.ccap.length; i++) {
                        if (values.get(i) == null) return null;
                        p.cap.ccap[i] = values.get(i);
                    }
                }

                int ccapLength = Long.bitCount(p.cap.pcap);
                if (ccapLength > 0 && (p.cap.ccap == null || p.cap.ccap.length != ccapLength))
                    return null;
                if (ccapLength == 0 && (p.cap.ccap != null && p.cap.ccap.length != 0))
                    return null;
            } else {
                return null;
            }
        }

        // COD
        String codString = j2kNode.getFieldAsString("CodingStyleDefault");
        if (codString != null && codString.length() >= 20 && (codString.length() % 2 == 0)) {
            p.cod = new COD();
            p.cod.scod = (short) Integer.parseInt(codString.substring(0, 2), 16);
            p.cod.progressionOrder = (short) Integer.parseInt(codString.substring(2, 4), 16);
            p.cod.numLayers = Integer.parseInt(codString.substring(4, 8), 16);
            p.cod.multiComponentTransform = (short) Integer.parseInt(codString.substring(8, 10), 16);
            p.cod.numDecompLevels = (short) Integer.parseInt(codString.substring(10, 12), 16);
            p.cod.xcb = (short) (Integer.parseInt(codString.substring(12, 14), 16) + 2);
            p.cod.ycb = (short) (Integer.parseInt(codString.substring(14, 16), 16) + 2);
            p.cod.cbStyle = (short) Integer.parseInt(codString.substring(16, 18), 16);
            p.cod.transformation = (short) Integer.parseInt(codString.substring(18, 20), 16);

            p.cod.precinctSizes = new short[(codString.length() - 20) / 2];
            for (int i = 0; i < p.cod.precinctSizes.length; i++) {
                p.cod.precinctSizes[i] = (short) Integer.parseInt(codString.substring(20 + 2 * i, 22 + 2 * i), 16);
            }
        }

        // QCD
        String qcdString = j2kNode.getFieldAsString("QuantizationDefault");
        if (qcdString != null && qcdString.length() >= 2 && (qcdString.length() % 2 == 0)) {
            p.qcd = new QCD();
            p.qcd.sqcd = (short) Integer.parseInt(qcdString.substring(0, 2), 16);

            int spqcdSize = (p.qcd.sqcd & 0b11111) == 0 ? 1 : 2;
            p.qcd.spqcd = new int[(qcdString.length() - 2) / (2 * spqcdSize)];
            for (int i = 0; i < p.qcd.spqcd.length; i++) {
                p.qcd.spqcd[i] = Integer.parseInt(qcdString.substring(2 + 2 * spqcdSize * i, 4 + 2 * spqcdSize * i), 16);
            }
        }

        return p;
    }

    // From MXF Descriptor to common J2KHeaderParameters
    public static J2KHeaderParameters fromJPEG2000PictureSubDescriptorBO(JPEG2000PictureSubDescriptor.JPEG2000PictureSubDescriptorBO jpeg2000PictureSubDescriptorBO) {
        J2KHeaderParameters p = new J2KHeaderParameters();

        p.rsiz = jpeg2000PictureSubDescriptorBO.getRSiz().intValue();
        p.xsiz = jpeg2000PictureSubDescriptorBO.getXSiz().longValue();
        p.ysiz = jpeg2000PictureSubDescriptorBO.getYSiz().longValue();
        p.xosiz = jpeg2000PictureSubDescriptorBO.getXoSiz().longValue();
        p.yosiz = jpeg2000PictureSubDescriptorBO.getYoSiz().longValue();
        p.xtsiz = jpeg2000PictureSubDescriptorBO.getXtSiz().longValue();
        p.ytsiz = jpeg2000PictureSubDescriptorBO.getYtSiz().longValue();
        p.xtosiz = jpeg2000PictureSubDescriptorBO.getXtoSiz().longValue();
        p.ytosiz = jpeg2000PictureSubDescriptorBO.getYtoSiz().longValue();

        // CSiz
        List<JPEG2000PictureComponent.JPEG2000PictureComponentBO> subDescriptorCsizi = jpeg2000PictureSubDescriptorBO.getPictureComponentSizing().getEntries();
        p.csiz = new CSiz[subDescriptorCsizi.size()];
        for (int i = 0; i < p.csiz.length; i++) {
            p.csiz[i] = new CSiz();
            p.csiz[i].ssiz = subDescriptorCsizi.get(i).getSSiz();
            p.csiz[i].xrsiz = subDescriptorCsizi.get(i).getXrSiz();
            p.csiz[i].yrsiz = subDescriptorCsizi.get(i).getYrSiz();
        }

        if (p.csiz.length != jpeg2000PictureSubDescriptorBO.getCSiz()) {
            return null;
        }

        // CAP
        J2KExtendedCapabilities j2KExtendedCapabilities = jpeg2000PictureSubDescriptorBO.getJ2kExtendedCapabilities();
        List<Short> subDescriptorcCap = j2KExtendedCapabilities.getcCap().getEntries();
        p.cap = new CAP();
        p.cap.pcap = j2KExtendedCapabilities.getpCap();
        p.cap.ccap = new int[subDescriptorcCap.size()];
        for (int i = 0; i < p.cap.ccap.length; i++) {
            p.cap.ccap[i] = subDescriptorcCap.get(i);
        }

        int cCapLength = Long.bitCount(p.cap.pcap);
        if (cCapLength > 0 && p.cap.ccap.length != cCapLength) {
            return null;
        }

        // COD
        String codString = jpeg2000PictureSubDescriptorBO.getCodingStyleDefaultString();
        if (codString != null && codString.length() >= 20 && (codString.length() % 2 == 0)) {
            p.cod = new COD();
            p.cod.scod = (short) Integer.parseInt(codString.substring(0, 2), 16);
            p.cod.progressionOrder = (short) Integer.parseInt(codString.substring(2, 4), 16);
            p.cod.numLayers = Integer.parseInt(codString.substring(4, 8), 16);
            p.cod.multiComponentTransform = (short) Integer.parseInt(codString.substring(8, 10), 16);
            p.cod.numDecompLevels = (short) Integer.parseInt(codString.substring(10, 12), 16);
            p.cod.xcb = (short) (Integer.parseInt(codString.substring(12, 14), 16) + 2);
            p.cod.ycb = (short) (Integer.parseInt(codString.substring(14, 16), 16) + 2);
            p.cod.cbStyle = (short) Integer.parseInt(codString.substring(16, 18), 16);
            p.cod.transformation = (short) Integer.parseInt(codString.substring(18, 20), 16);

            p.cod.precinctSizes = new short[(codString.length() - 20) / 2];
            for (int i = 0; i < p.cod.precinctSizes.length; i++) {
                p.cod.precinctSizes[i] = (short) Integer.parseInt(codString.substring(20 + 2 * i, 22 + 2 * i), 16);
            }
        } else {
            return null;
        }

        // QCD
        String qcdString = jpeg2000PictureSubDescriptorBO.getQuantisationDefaultString();
        if (qcdString != null && qcdString.length() >= 2 && (qcdString.length() % 2 == 0)) {
            p.qcd = new QCD();
            p.qcd.sqcd = (short) Integer.parseInt(qcdString.substring(0, 2), 16);

            int spqcdSize = (p.qcd.sqcd & 0b11111) == 0 ? 1 : 2;
            p.qcd.spqcd = new int[(qcdString.length() - 2) / (2 * spqcdSize)];
            for (int i = 0; i < p.qcd.spqcd.length; i++) {
                p.qcd.spqcd[i] = Integer.parseInt(qcdString.substring(2 + 2 * spqcdSize * i, 4 + 2 * spqcdSize * i), 16);
            }
        }

        return p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        J2KHeaderParameters that = (J2KHeaderParameters) o;
        return Objects.equals(rsiz, that.rsiz) &&
                Objects.equals(xsiz, that.xsiz) &&
                Objects.equals(ysiz, that.ysiz) &&
                Objects.equals(xosiz, that.xosiz) &&
                Objects.equals(yosiz, that.yosiz) &&
                Objects.equals(xtsiz, that.xtsiz) &&
                Objects.equals(ytsiz, that.ytsiz) &&
                Objects.equals(xtosiz, that.xtosiz) &&
                Objects.equals(ytosiz, that.ytosiz) &&
                Arrays.equals(csiz, that.csiz) &&
                cod.equals(that.cod) &&
                qcd.equals(that.qcd) &&
                cap.equals(that.cap);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rsiz, xsiz, ysiz, xosiz, yosiz, xtsiz, ytsiz, xtosiz, ytosiz, cod, qcd, cap);
        result = 31 * result + Arrays.hashCode(csiz);
        return result;
    }
}
