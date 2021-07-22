
## 区块链

+ 私钥生成流程    
输入助记词：original desert extra joy cycle install crystal ocean around wing dirt unlock    
    + 生成seed：    
[-121, 30, 66, -15, -57, 122, -84, 12, 97, -57, 90, 18, -26, -97, -36, -125, 3, 58, -87, -16, 40, -111, 1, -6, 5, 27, 110, 49, -36, 43, -57, -90, 60, -3, 79, -44, 117, 27, 19, 2, -12, -124, -44, -56, -47, -28, -73, -122, 16, -79, 20, 87, 98, 67, -125, 91, -56, 76, -111, -12, -119, 15, 2, 0]    
    + 编码seed： 以 "Bitcoin seed".getBytes()为key对seed进行HmacSHA512编码    
[69, -60, 25, 85, -17, 58, -126, 34, -54, 94, 28, -16, 72, 42, -108, 102, -56, 71, -92, -93, 89, 47, 25, -28, -1, -78, 116, 120, 7, -7, -108, -107, 11, -16, 98, -54, -65, -17, 100, 120, -113, -99, -67, -55, 82, 47, -119, -27, 122, 32, 54, -100, -102, 32, -15, 46, 8, 118, -94, 82, -32, -123, -13, -40]    
    + 生成ExtendedKey：使用前32位为priv，后32位为chainCode    
    + 生成RawPrivateKey：priv 32位，不足进行补位，多余取后面，对priv进行5层变化    
m/purpse/coin_type/account/change/address_index    
        + purpse | 0x80000000     以chainCode为key对priv+4位进行HmacSHA512编码，前32位结果加上原priv数据为新priv，后32位为新chainCode。    
        + coinType | 0x80000000  以chainCode为key对priv+4位进行HmacSHA512编码，前32位结果加上原priv数据为新priv，后32位为新chainCode。    
        + account | 0x80000000   以chainCode为key对priv+4位进行HmacSHA512编码，前32位结果加上原priv数据为新priv，后32位为新chainCode。    
        + change   以chainCode为key对priv+5位进行HmacSHA512编码，前32位结果加上原priv数据为新priv，后32位为新chainCode。    
        + address_index    以chainCode为key对priv+5位进行HmacSHA512编码，前32位结果加上原priv数据为新priv，后32位为新chainCode。    
[-117, 21, 40, -13, -12, 100, 13, 126, -108, 125, -69, -69, -50, 4, 64, -102, -72, 58, 23, -115, 117, -13, 99, -18, 83, 50, 105, 102, 63, -113, 2, -21]    
    + 生成PrivateKey：    
        + 添加版本标记0x80（主网）或0xef（测试网），不同的币种有不同的版本标记，如LTC是0xb0    
        + 添加0x01 + 4位校验码到尾部    
        + BASE58编码    
L1t4ygXx2x7KBg846aMddmr4KYgnpb2XXAhEmHGqAFTVGvoarBzK    

+ 公钥生成流程    
    + 对PrivateKey进行椭圆曲线算法生成pubComp数组    
[2, 35, -97, -111, 98, -102, 34, 85, -105, -11, 58, 17, -125, 33, 119, 98, -44, -13, -127, 121, -86, 105, -65, -95, -2, 63, -118, 86, -119, 37, -33, 113, -24]    
    + 对pub数组进行 Hex编码    
02239f91629a225597f53a1183217762d4f38179aa69bfa1fe3f8a568925df71e8    

+ 地址生成流程    
    + 对pub数组进行SHA256_RIPEMD160加密    
    + 添加版本前缀，不同的币种有不同的版本前缀   
    + SHA256签名两次    
    + 将双 Sha256 运算的结果前 4位 拼接到尾部    
    + BASE58编码    

+ LTC 地址格式       
  以下介绍 LTC 三种常见的地址格式。    
      
    + P2PKH 地址格式（imToken 支持）    
  P2PKH 格式（Pay to Public Key Hash）的地址以 L 开头，等同于 BTC 中以 1 开头的地址。P2PKH 地址格式示例：    
  LhyLNfBkoKshT7R8Pce6vkB9T2cP2o84hx    
      
    + P2SH-P2WPKH 地址格式（imToken 支持）    
  P2SH-P2WPKH 格式（Pay to Script Hash – Pay to Witness Public Key Hash）的地址以 M 开头，等同于 BTC 中以 3 开头的地址。P2SH-P2WPKH 地址格式：    
  MR5Hu9zXPX3o9QuYNJGft1VMpRP418QDfW    
  
    + Native SegWit 地址格式（imToken 暂不支持）    
  Native SegWit 格式的地址以 ltc 开头，等同于 BTC 中以 bc 开头的地址。Native SegWit 地址格式：    
  ltc1qum864wd9nwsc0u9ytkctz6wzrw6g7zdn08yddf    

+ BCH 地址格式           
    + Use CashAddr addresses for Bitcoin Cash (ie starting with 'q' instead of '1')    
    + Use BitPay-style addresses for Bitcoin Cash (ie starting with 'C' instead of '1')    
    + Use legacy addresses for Bitcoin Cash (ie starting with '1')              
              byte[] hashedPublicKey;    
              if (isCompressed()) {    
                  hashedPublicKey = RIPEMD160.hash160(pubComp);    
              } else {    
                  hashedPublicKey = RIPEMD160.hash160(pub);    
              }    
              return BitcoinCashAddressFormatter.toCashAddress(BitcoinCashAddressType.P2PKH,
                      hashedPublicKey, MoneyNetwork.MAIN);    
                      
                      

+ Filecoin 地址格式    
  Filecoin 有三种地址格式：普通地址（f1 开头）、矿工地址（f0 开头）和矿工地址（f3 开头。    
    + 普通地址格式（f1 开头）：f16tugakjlpyoomxy5uv2d6bdj7wcyr3ueofu7w7a    
    + 矿工地址格式（f0 开头）：f01782    
    + 矿工地址格式（f3 开头）：f3sg22lqqjewwczqcs2cjr3zp6htctbovwugzzut2nkvb366wzn5tp2zkfvu5xrfqhreowiryxump7l5e6jaaq       
 