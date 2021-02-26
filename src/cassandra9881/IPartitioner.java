package cassandra9881;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import cassandraIO.DecoratedKey;

public interface IPartitioner
{
    /**
     * Transform key to object representation of the on-disk format.
     *
     * @param key the raw, client-facing key
     * @return decorated version of key
     */
    public DecoratedKey decorateKey(ByteBuffer key);

//    /**
//     * Calculate a Token representing the approximate "middle" of the given
//     * range.
//     *
//     * @return The approximate midpoint between left and right.
//     */
//    public Token midpoint(Token left, Token right);
//
//    /**
//     * @return A Token smaller than all others in the range that is being partitioned.
//     * Not legal to assign to a node or key.  (But legal to use in range scans.)
//     */
//    public T getMinimumToken();
//
//    /**
//     * @return a Token that can be used to route a given key
//     * (This is NOT a method to create a Token from its string representation;
//     * for that, use TokenFactory.fromString.)
//     */
//    public T getToken(ByteBuffer key);
//
//    /**
//     * @return a randomly generated token
//     */
//    public T getRandomToken();
//
//    public Token.TokenFactory getTokenFactory();
//
//    /**
//     * @return True if the implementing class preserves key order in the Tokens
//     * it generates.
//     */
//    public boolean preservesOrder();
//
//    /**
//     * Calculate the deltas between tokens in the ring in order to compare
//     *  relative sizes.
//     *
//     * @param sortedTokens a sorted List of Tokens
//     * @return the mapping from 'token' to 'percentage of the ring owned by that token'.
//     */
//    public Map<Token, Float> describeOwnership(List<Token> sortedTokens);
//
//    public AbstractType<?> getTokenValidator();
//
//    public <R extends RingPosition> R minValue(Class<R> klass);
}
