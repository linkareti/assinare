async function doEncryptText(plaintext) {
    const encoder = new TextEncoder();
    const plaintextBuffer = encoder.encode(plaintext);

    return doEncrypt(plaintextBuffer);
}

async function doEncrypt(plaintextBuffer) {

    const pubJwk = {
        "kty": "RSA",
        "e": "AQAB",
        "kid": "2039e205-c86a-4fa0-8fc6-389c733d5ba7",
        "n": "lL3UqLUQHO2gi7ObFXIog23YPdZen-uHgsWp3uSd15KQz87yO3atHDhi7dfgek2KQEDWpKWDJxTdRkVQ40A0APtOIqm4fhSUJF3bnYlehgzEhzUA9XGsHCBFwVhPV320KSfUwI72Gl9GJJEGAboTHs6cre22Fy0CA3cnPyuSy82Ct1KEL5k2KfdKzNQJRkyyW1aH8wsfWiVFCeKHkYTgBknfnsNPQsyisplNbBdJbNFmDzWvdkmThDAli0LZVv-C3MvKa45U0HMTYMLPmRNFDewEkzbGGHEWTVJNXmPra5brI2UOh8waM_-MatSRj3ipFSJcoJ3dOTQbWUU7VJj2fw"
    };

    let pubKey = await crypto.subtle.importKey(
        "jwk",
        pubJwk,
        {
            name: "RSA-OAEP",
            hash: "SHA-256"
        },
        false,
        ["encrypt"]
    );

    let ciphertextBuffer = await crypto.subtle.encrypt(
        {
            name: "RSA-OAEP"
        },
        pubKey,
        plaintextBuffer
    );

    return ciphertextBuffer;

}