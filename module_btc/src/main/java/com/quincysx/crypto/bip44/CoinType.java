package com.quincysx.crypto.bip44;


/**
 * @author QuincySx
 * @date 2018/3/5 下午4:26
 */
public class CoinType {
    private final Purpose purpose;
    private final CoinEnum coinType;
    private final String string;

    CoinType(final Purpose purpose, final CoinEnum coinType) {
        this.purpose = purpose;
        this.coinType = coinType;
        string = String.format("%s/%d'", purpose, coinType.coinType());
    }

    public CoinEnum getValue() {
        return coinType;
    }

    public Purpose getParent() {
        return purpose;
    }

    @Override
    public String toString() {
        return string;
    }

    /**
     * Create a {@link Account} for this purpose and coin type.
     *
     * @param account The account number
     * @return An {@link Account} instance for this purpose and coin type
     */
    public Account account(final int account) {
        return new Account(this, account);
    }
}
