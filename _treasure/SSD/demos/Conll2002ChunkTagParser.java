
import com.aliasi.corpus.TagHandler;

import com.aliasi.corpus.parsers.RegexLineTagParser;

/**
 * Here's a corpus sample from CoNLL 2002 test file <code>ned.testa</code>,
 * which is encoded in the character set ISO-8859-1 (aka Latin1):
 * 
 * <blockquote><pre>...
 * de Art O
 * orde N O
 * . Punc O
 * 
 * -DOCSTART- -DOCSTART- O
 * Met Prep O
 * tien Num O
 * miljoen Num O
 * komen V O
 * we Pron O
 * , Punc O
 * denk V O
 * ik Pron O
 * , Punc O
 * al Adv O
 * een Art O
 * heel Adj O
 * eind N O
 * . Punc O
 * 
 * Dirk N B-PER
 * ...
 * </pre></blockquote>
*/
public class Conll2002ChunkTagParser 
    extends RegexLineTagParser {
    
    static final String TOKEN_TAG_LINE_REGEX
        = "(\\S+)\\s(\\S+\\s)?(O|[B|I]-\\S+)"; // token ?posTag entityTag
    static final int TOKEN_GROUP = 1; // token
    static final int TAG_GROUP = 3;   // entityTag
    static final String IGNORE_LINE_REGEX
        = "-DOCSTART(.*)";  // lines that start with "-DOCSTART"
    static final String EOS_REGEX
        = "\\A\\Z";         // empty lines
    
    public Conll2002ChunkTagParser() { 
        this(null); 
    }
    
    public Conll2002ChunkTagParser(TagHandler handler) {
        super(handler,
              TOKEN_TAG_LINE_REGEX, TOKEN_GROUP, TAG_GROUP,
              IGNORE_LINE_REGEX, EOS_REGEX);
    }
}

