package com.android.vending.billing.test;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

import com.android.vending.billing.test.base64.Base64;

public class PurchaseDataSigner {
	/**
	 * @see test-key\android-test-billing.pkcs8
	 */
	private static final String PRIVATE_KEY = 
			"MIIEowIBAAKCAQEAzYcLsg66vpeSf29pXkYp9EslwkKTSctbbW/5UczAzMuZ4LHC" +
			"/Ll2mzUTNX0sxrxx4wG0/8c9Z+39/XcMCQc/yG6Ch62Smqyz/e5DPCoZMZ8QbNOM" +
			"AnqIqExCthf29VXviYvNWtACogOA3fro/1miQVg9BaGaXJycEiM9NI9GESIERPUX" +
			"MVRrvpFoUCHs7ktLPTRmqlQp6aJpiGwVO+IN4K3NGDNPMt5g3loD8C1+QzI51iOj" +
			"9oTfa+wTS8RtsEcHN9YsB50uZinXy/9NxR0ju7AKb4GOr97f4ryNY8viBAZ2jDcu" +
			"xf7ALOwKwtn5oYeetQ9bMNcKid1hPHiM3AqpXwIDAQABAoIBAE0KUBIAf3Ot8VYf" +
			"dwnFlzhhatf0iK1isvTEwlotPCAIObgGKib9hj4LmRW1xDECtlX3b9do4dWxWkHG" +
			"fBZuRzRkaD1fjNK55gZLZ5yH00XMcCW67oKHV+5uF5uWRBYWjmGqq+Fck4ATfBy5" +
			"TtlYyf9gig8wUmJgugf6eZjYE9WblEjLoh4qEYR0+Ldpa7i3A1frzoGaB99haVVk" +
			"1cVYV38rK4oYGMxn90nHVtDz36zVIlVutaxGxhtme5b8ChzcRXU1X7mQpl8F/6fc" +
			"r0j9uapJkEHF1mU7DfC4exLt03PZ9xM3rYVhJqIp+jrlAlDkLG8IZ+Bv1LSBiOLu" +
			"BmAxhEECgYEA9eLqRTGfn0vbdKtI21Uds22h/g3D+cM+wyY031QACGfd7l6ejd1G" +
			"bjOBqGAlodTOjxwWtKJ7d1wAyVIxgmkJT626ARGGp2Q/ZNWTNW5MTR2+d9lxG5SX" +
			"hVdWo14hOkgmu+ynHEYkC734MI9ujJ6S3cgGtROmdALtdTPmY959pVECgYEA1fsq" +
			"/ZTJKhW69l7GvKqiyTI5njkPDsR7XEhZZlxzMll1fhbPb7vAjQubbkve69NxXmCW" +
			"KqzsI/2yfBWMJ6XL4CplUBdzq4vtLBoUPCC92Di+a852EpO1XYxPjZLxo3Ge4WXI" +
			"C7GoxIfKJM4y6TrT706l6WdJFgindhsMUjvud68CgYBXLLsICx7feCJ3C58J+/RK" +
			"v+y1yKWZcEpZH7uRDhPw5vo835jxDOlCespWKrhM1sjg3gGCyzRgzeKNfbGF3b1q" +
			"XVpbtJ37zMPY+Qt23wL4ODpl/VZng9fwc1WJF/9JzXjGGZHpqUhwTUqn1MJJ4pKJ" +
			"RLh0DGJwMPWFHwBomeIL8QKBgAdwEZB66XsjAjqqFLJeJAZmToZnaVSUE82jXigm" +
			"soJfhav6Uv+JQSol6yMsU/VShjBHnzCgjhy6JsgVX6fJJYsHLjpYdlbSKE5OIcHt" +
			"SWwInOXWaVg/9h/oTqOXJfsOaf1fslJlvH4IUkhxOmfM+azpyO6R/oJbjqaWThlV" +
			"p/ZVAoGBAJbOLE373cfZ2KMehskCud3K2PivMutGeFK5QL0iScXAKVefpb9/CSVj" +
			"CRMiVFp5IS17WEF1u0YUXR1+RiLbIVxIzJ28GPHPPP2Ld29Lr/DO3mtAw7SLBI8J" +
			"VI96eFNpc/2A+8SJzf6Q7qLsfFbQCRjZCkY7xcobUVRRlHtCTm7+";
	
	public static String sign(String data) { 
		try {
	        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(PRIVATE_KEY))); 		
			
			Signature signer = Signature.getInstance("SHA1withRSA");
			signer.initSign(privateKey);
			// default charset is : com.ibm.icu4jni.charset.CharsetICU[UTF-8]
			signer.update(data.getBytes("UTF-8"));
			return Base64.encode(signer.sign());
		} catch (Exception ex) {
			return "PurchaseDataSignerFailed";
		}
		
	}
}
