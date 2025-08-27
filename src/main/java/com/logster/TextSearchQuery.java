package com.logster;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.regex.Pattern;

public class TextSearchQuery {

    private final  static  int USE_TRIE=1;
    private final  static  int USE_REGEXP=2;
    private final  static  int USE_INDEX=3;
    private final String query;
    private int use=3;

    Pattern pattern = null;
    Trie ahoTrie = null;
    public TextSearchQuery(String query){
        this.query = query;
        boolean multiKeyword = query.contains(",");
        boolean literal = isLiteral(query) && !multiKeyword;
        if (multiKeyword) {
            use=USE_TRIE;
            Trie.TrieBuilder builder = Trie.builder().ignoreCase();
            for (String keyword : query.split(",")) {
                builder.addKeyword(keyword.trim());
            }
            ahoTrie = builder.build();
        } else if (!literal) {
            use=USE_REGEXP;
            pattern = Pattern.compile(query);
        }
    }

    public boolean match(String line){
        if (use==USE_TRIE) {
            return  ahoTrie.containsMatch(line);
        } else if (use==USE_INDEX) {
            return line.contains(query);
        } else if (use==USE_REGEXP) { // regex
            return pattern.matcher(line).find();
        }
        return false;
    }
    private static boolean isLiteral(String query) {
        return !query.matches(".*[.^$*+?()\\[\\]{}|\\\\].*");
    }
}
