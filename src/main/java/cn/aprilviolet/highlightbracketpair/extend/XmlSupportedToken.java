package cn.aprilviolet.highlightbracketpair.extend;

import cn.aprilviolet.highlightbracketpair.brace.BraceTokenTypes;
import cn.aprilviolet.highlightbracketpair.util.Pair;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Customer Xml support Bracket
 *
 * @author AprilViolet
 * @version v1.0.0
 * @date 2021.09.21 16:04
 * @since v1.0.0
 */
public class XmlSupportedToken extends CustomSupportedToken {
    public static String getLeftPart(int start, HighlighterIterator iterator) {
        int tagNameEnd = 0;
        Document document = iterator.getDocument();
        for (; !iterator.atEnd(); iterator.advance()) {
            IElementType tokenType = iterator.getTokenType();
            if (tokenType == XmlTokenType.XML_TAG_NAME && iterator.getEnd() > start) {
                tagNameEnd = iterator.getEnd();
                continue;
            }
            if (tokenType == XmlTokenType.XML_TAG_END && tagNameEnd != 0) {
                return document.getText(new TextRange(start,
                        iterator.getEnd()));
            }
            tagNameEnd = 0;
        }
        return "";
    }

    public static String getLeftPartOnlyName(int start, HighlighterIterator iterator) {
        Document document = iterator.getDocument();
        for (; !iterator.atEnd(); iterator.advance()) {
            IElementType iteratorTokenType = iterator.getTokenType();
            if (iteratorTokenType == XmlTokenType.XML_TAG_NAME && iterator.getEnd() > start) {
                String tagWithName = document.getText(new TextRange(start,
                        iterator.getEnd()));
                iterator.advance();
                if (iterator.getTokenType() == XmlTokenType.XML_TAG_END) {
                    return document.getText(new TextRange(start,
                            iterator.getEnd()));
                }
                return tagWithName;
            }
        }
        return "";
    }

    public static String getRightPart(int end, HighlighterIterator iterator, int leftIndex) {
        Document document = iterator.getDocument();
        Deque<Integer> xmlTagEnd = new ArrayDeque<>();
        for (; !iterator.atEnd(); iterator.retreat()) {
            IElementType tokenType = iterator.getTokenType();
            if (iterator.getStart() < leftIndex) {
                if (!xmlTagEnd.isEmpty()) {
                    return document.getText(new TextRange(xmlTagEnd.removeFirst(), end));
                }
                break;
            }
            if (tokenType == XmlTokenType.XML_END_TAG_START) {
                return document.getText(new TextRange(iterator.getStart(),
                        end));
            }
            if (tokenType == XmlTokenType.XML_TAG_END) {
                xmlTagEnd.addFirst(iterator.getStart());
            }
        }
        return "";
    }

    @Override
    public Map<Language, List<Pair<IElementType, IElementType>>> addSupported(Map<Language, List<Pair<IElementType,
            IElementType>>> languagePairsMap) {
        Language xml = Language.findLanguageByID("XML");
        if (xml == null) {
            return languagePairsMap;
        }

        List<Pair<IElementType, IElementType>> pairList = languagePairsMap.get(xml);
        if (pairList == null) {
            pairList = new ArrayList<>();
            pairList.add(new Pair<>(XmlTokenType.XML_START_TAG_START, XmlTokenType.XML_TAG_END));
            pairList.add(new Pair<>(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER,
                    XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER));
            pairList.add(new Pair<>(XmlTokenType.XML_START_TAG_START, XmlTokenType.XML_EMPTY_ELEMENT_END));
            pairList.add(new Pair<>(BraceTokenTypes.TEXT_TOKEN, BraceTokenTypes.TEXT_TOKEN));
            languagePairsMap.put(xml, pairList);
            for (Language subLanguage : xml.getDialects()) {
                List<Pair<IElementType, IElementType>> pairs = languagePairsMap.get(subLanguage);
                if (pairs != null) {
                    pairs.addAll(pairList);
                } else {
                    languagePairsMap.put(subLanguage, pairList);
                }
            }
        }
        return languagePairsMap;
    }

    public enum Singleton {
        /**
         * Enumeration singleton
         */
        INSTANCE;

        private final XmlSupportedToken xmlSupportedToken;

        Singleton() {
            xmlSupportedToken = new XmlSupportedToken();
        }

        public XmlSupportedToken getInstance() {
            return xmlSupportedToken;
        }

        public Map<Language, List<Pair<IElementType, IElementType>>> addSupported(Map<Language, List<Pair<IElementType,
                IElementType>>> map) {
            return xmlSupportedToken.addSupported(map);
        }
    }
}
