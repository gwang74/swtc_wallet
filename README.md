# swtc_wallet
[![](https://jitpack.io/v/gwang74/swtc_wallet.svg)](https://jitpack.io/#gwang74/swtc_wallet)

主要用于生成SWTC钱包的keyStore和二维码，其中keyStore是基于ETH的keyStore生成方式做成的。


## 使用说明
Step 1. Add the JitPack repository to your build file
```xml
allprojects {
repositories {
	...
	maven { url 'https://jitpack.io' }
	}
}
```

Step 2. Add the dependency
```xml
dependencies {
	implementation 'com.github.gwang74:swtc_wallet:1.0'
}
```


### keyStore生成
```android
JtKeyPair jtKeyPair = new JtKeyPair("jHY6aRcs7J6KnfgqD4FVwTQ247boj9nbDZ", "shExMjiMqza4DdMaSg3ra9vxWPZsQ");
try {
        KeyStoreFile keyStoreFile = KeyStore.createLight("Key123456", jtKeyPair);
        System.out.println(keyStoreFile.toString());
    } catch (CipherException e) {
        e.printStackTrace();
    }
```

```json
{
	"address": "jHY6aRcs7J6KnfgqD4FVwTQ247boj9nbDZ",
	"id": "1c1bf720-82fd-4ed3-bddf-72ebbc7b4262",
	"version": 3,
	"crypto": {
		"cipher": "aes-128-ctr",
		"ciphertext": "0bc63928ace81eb82869d5008372830191bad7706ef2101665d009a9e6",
		"cipherparams": {
			"iv": "2ae846f498bbb6ff6a7d572d51cdd74b"
		},
		"kdf": "scrypt",
		"kdfparams": {
			"dklen": 32,
			"n": 4096,
			"p": 6,
			"r": 8,
			"salt": "944611340b628e66850eff427ec0df006788d2aa7e3809b383dbe05282edd723"
		},
		"mac": "ad1343750c048c96b019dc09dd6a5b93d5664cfd5147dd052ec040546d53617f"
	}
}
```

### 解析keyStore
```java
String data = "{"address":"jHY6aRcs7J6KnfgqD4FVwTQ247boj9nbDZ","id":"1c1bf720-82fd-4ed3-bddf-72ebbc7b4262","version":3,"crypto":{"cipher":"aes-128-ctr","ciphertext":"0bc63928ace81eb82869d5008372830191bad7706ef2101665d009a9e6","cipherparams":{"iv":"2ae846f498bbb6ff6a7d572d51cdd74b"},"kdf":"scrypt","kdfparams":{"dklen":32,"n":4096,"p":6,"r":8,"salt":"944611340b628e66850eff427ec0df006788d2aa7e3809b383dbe05282edd723"},"mac":"ad1343750c048c96b019dc09dd6a5b93d5664cfd5147dd052ec040546d53617f"}}"
KeyStoreFile keyStoreFile = KeyStoreFile.parse(data);
JtKeyPair jtKeyPair = KeyStore.decrypt(pwd, keyStoreFile);
```

### 生成二维码
```java
Bitmap bitmap = QrCodeGenerator.getQrCodeImage(data, 800, Color.BLACK);
```

### 解析二维码
```java
String data = QrCodeGenerator.decodeQrImage(bitmap);
```
