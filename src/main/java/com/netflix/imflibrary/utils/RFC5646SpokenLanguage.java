/*
 *
 * Copyright 2026 Netflix, Inc.
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

package com.netflix.imflibrary.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers for RFC-5646 language tags, as used for the primary spoken language of audio labels.
 */
public final class RFC5646SpokenLanguage {
    private RFC5646SpokenLanguage() {}

    /**
     * A method to verify if the spoken language indicated in the SoundFieldGroupLabelSubDescriptor of the WaveAudioPCMDescriptor
     * is RFC-5646 compliant or not
     * @param rfc5646SpokenLanguage the language tag that needs to be verified
     * @return a boolean indicating the result of the verification check
     */
    public static boolean isCompliant(String rfc5646SpokenLanguage){
        if(rfc5646SpokenLanguage != null){
            Matcher matcher = buildRegExpLangRFC5646().matcher(rfc5646SpokenLanguage);
            return matcher.find();
        }
        return false;
    }

    public static List<String> getPrimarySpokenLanguageUnicodeString(String rfc5646SpokenLanguage){

        Integer asciiStartRange = 0x21;
        Integer asciiEndRange = 0x7e;
        String inputString = rfc5646SpokenLanguage;
        char[] charArray = inputString.toCharArray();
        List<Integer> unicodeCodePoints = new ArrayList<>();
        for (int i=0; i<charArray.length;)
        {
            if (!Character.isHighSurrogate(charArray[i]))
            {
                int unicodeCodePoint = Character.codePointAt(charArray, i, (i+1));
                unicodeCodePoints.add(unicodeCodePoint);
                i++;
            }
            else
            {
                if ((i + 1) < charArray.length)
                {
                    int unicodeCodePoint = Character.codePointAt(charArray, i, (i+2));
                    unicodeCodePoints.add(unicodeCodePoint);
                    i += 2;
                }
                else
                {
                    throw new IllegalArgumentException(String.format("Invalid character in '%s'. Only high surrogate exists", new String(charArray)));
                }
            }
        }

        StringBuilder unicodeString = new StringBuilder();
        unicodeString.append("0x");
        StringBuilder stringBuilder = new StringBuilder();
        for (int unicodeCodePoint : unicodeCodePoints)
        {
            unicodeString.append(String.format("%02x", unicodeCodePoint));
            if(unicodeCodePoint < asciiStartRange
                    || unicodeCodePoint > asciiEndRange){
                stringBuilder.append(".");
            }
            else{
                stringBuilder.append(String.format("%s", String.copyValueOf(Character.toChars(unicodeCodePoint))));
            }
        }
        List<String> strings = new ArrayList<>();
        strings.add(unicodeString.toString());
        strings.add(stringBuilder.toString());
        return strings;
    }

    private static Pattern buildRegExpLangRFC5646()
    {
        String extLang = "([A-Za-z]{3}(-[A-Za-z]{3}){0,2})";
        String language =
                "(([a-zA-Z]{2,3}(-" + extLang + ")?)|([a-zA-Z]{5,8}))";
        String script = "([A-Za-z]{4})";
        String region = "([A-Za-z]{2}|\\d{3})";
        String variant = "([A-Za-z0-9]{5,8}|(\\d[A-Z-a-z0-9]{3}))";
        String singleton = "(\\d|[A-W]|[Y-Z]|[a-w]|[y-z])";
        String extension = "(" + singleton + "(-[A-Za-z0-9]{2,8})+)";
        String privateUse = "(x(-[A-Za-z0-9]{1,8})+)";

        String langTag =
                language + "(-" + script + ")?(-" + region + ")?(-" + variant
                        + ")*(-" + extension + ")*(-" + privateUse + ")?";

        String irregular =
                "((en-GB-oed)|(i-ami)|(i-bnn)|(i-default)|(i-enochian)|(i-hak)|(i-klingon)|(i-lux)|(i-mingo)|(i-navajo)|(i-pwn)|(i-tao)|(i-tay)|(i-tsu)|(sgn-BE-FR)|(sgn-BE-NL)|(sgn-CH-DE))";
        String regular =
                "((art-lojban)|(cel-gaulish)|(no-bok)|(no-nyn)|(zh-guoyu)|(zh-hakka)|(zh-min)|(zh-min-nan)|(zh-xiang))";
        String grandFathered = "(" + irregular + "|" + regular + ")";

        StringBuffer languageTag = new StringBuffer();
        languageTag.append("(^").append(privateUse).append("$)");
        languageTag.append('|');
        languageTag.append("(^").append(grandFathered).append("$)");
        languageTag.append('|');
        languageTag.append("(^").append(langTag).append("$)");

        return Pattern.compile(languageTag.toString());
    }
}
