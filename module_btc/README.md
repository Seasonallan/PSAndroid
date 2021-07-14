
## 币种
+ LTC 地址格式       
  以下介绍 LTC 三种常见的地址格式。imToken 当前支持前两种地址格式。
  
  P2PKH 地址格式（imToken 支持）
  P2PKH 格式（Pay to Public Key Hash）的地址以 L 开头，等同于 BTC 中以 1 开头的地址。P2PKH 地址格式示例：
  LhyLNfBkoKshT7R8Pce6vkB9T2cP2o84hx
  
  P2SH-P2WPKH 地址格式（imToken 支持）
  P2SH-P2WPKH 格式（Pay to Script Hash – Pay to Witness Public Key Hash）的地址以 M 开头，等同于 BTC 中以 3 开头的地址。P2SH-P2WPKH 地址格式：
  MR5Hu9zXPX3o9QuYNJGft1VMpRP418QDfW
  
  Native SegWit 地址格式（imToken 暂不支持）
  Native SegWit 格式的地址以 ltc 开头，等同于 BTC 中以 bc 开头的地址。Native SegWit 地址格式：
  ltc1qum864wd9nwsc0u9ytkctz6wzrw6g7zdn08yddf

+ BCH 地址格式       
  Use CashAddr addresses for Bitcoin Cash (ie starting with 'q' instead of '1')
  Use BitPay-style addresses for Bitcoin Cash (ie starting with 'C' instead of '1')
  Use legacy addresses for Bitcoin Cash (ie starting with '1')          
              byte[] hashedPublicKey;
              if (isCompressed()) {
                  hashedPublicKey = RIPEMD160.hash160(pubComp);
              } else {
                  hashedPublicKey = RIPEMD160.hash160(pub);
              }
              return BitcoinCashAddressFormatter.toCashAddress(BitcoinCashAddressType.P2PKH,
                      hashedPublicKey, MoneyNetwork.MAIN);
 