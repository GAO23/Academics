//
// Created by xgao on 2/12/20.
//

#include "../include/crypto.h"


static CIPHER_INFO cipher_info = {NULL, "abcdefgg", NULL, NULL, NULL, NULL};

static int init_flag = 0; // for making sure the CIPHER_INFO struct is properly initialize

// initialize the CIPHER_INFO struct to the given password
void crypto_init(unsigned char* password, size_t password_len){ // password len does not include null terminator
    if(init_flag){
        fprintf(stderr, "%s\n", "crypto has been init already, please reset first");
        exit(1);
    }

    cipher_info.password = malloc(password_len + 1);
    memcpy(cipher_info.password, password, password_len + 1);
    cipher_info.encrypt_ctx = EVP_CIPHER_CTX_new();
    cipher_info.decrypt_ctx = EVP_CIPHER_CTX_new();

    // 256 bit password and IV generation from password
    cipher_info.iv = malloc(32);
    cipher_info.key = malloc(32);
    if (EVP_BytesToKey(EVP_aes_256_cbc(), EVP_sha1(), cipher_info.salt, password, password_len, 5, cipher_info.key, cipher_info.iv) != 32) {
        fprintf(stderr, "Crypto Error: Key size is not 32 bits\n");
        exit(2);
    }

    EVP_EncryptInit_ex(cipher_info.encrypt_ctx, EVP_aes_256_cbc(), NULL, cipher_info.key, cipher_info.iv);
    EVP_DecryptInit_ex(cipher_info.decrypt_ctx, EVP_aes_256_cbc(), NULL, cipher_info.key, cipher_info.iv);
    init_flag = 1;
}

// reset the CIPHER_INFO struct
void crypto_reset(void){
    free(cipher_info.password);
    EVP_CIPHER_CTX_free(cipher_info.encrypt_ctx);
    EVP_CIPHER_CTX_free(cipher_info.decrypt_ctx);
    cipher_info.encrypt_ctx = NULL;
    cipher_info.decrypt_ctx = NULL;
    free(cipher_info.key);
    free(cipher_info.iv);
    cipher_info.key = NULL;
    cipher_info.iv = NULL;
    cipher_info.password = NULL;
    init_flag = 0;
}


/*
 * code snippets taken from https://wiki.openssl.org/index.php/EVP_Symmetric_Encryption_and_Decryption
 * function name, return variable, and parameters come from code snippets. Key and iv parameters were removed in favor of my own code
 * this is to encrypt a buffer and output to another buffer
 */
int encrypt(unsigned char *plaintext, int plaintext_len, unsigned char *ciphertext){

    if(!init_flag){
        fprintf(stderr, "%s\n", "please init the crypto first");
        exit(1);
    }

    int len; // this comes from the code snippet.

    int ciphertext_len; // this comes from the code snippet

   // now encrypts
   // this comes from the code snippet. I replaced abort with my own error handling
    if(EVP_EncryptUpdate(cipher_info.encrypt_ctx, ciphertext, &len, plaintext, plaintext_len) != 1){
        fprintf(stderr, "%s\n", "encryption failed");
        exit(2);
    }

    ciphertext_len = len; // this comes from the code snippet

    // finalize the encryption
    // this comes from the code snippet. I replaced abort with my own error handling
    if(EVP_EncryptFinal_ex(cipher_info.encrypt_ctx, ciphertext + len, &len) != 1){
        fprintf(stderr, "%s\n",  "failed to finalize the encryption");
        exit(2);
    }

    ciphertext_len += len; // this comes from the code snippet

    /* no need for clean up. Come read the memory if you dare.
     * this comes from the code snippet
     * this comes from the code snippet. Commented out because I would let the kernel get rid of the memory after program closes
    */

    //    EVP_CIPHER_CTX_free(cipher_info.encrypt_ctx);

    return ciphertext_len; // this comes from the code snippet
}

/*
 * code snippets taken from https://wiki.openssl.org/index.php/EVP_Symmetric_Encryption_and_Decryption
 * function name, return variable, and parameters come from code snippets. Key and iv parameters were removed in favor of my own code
 * this is used to decrypt a buffer and output to another buffer
 */
int decrypt(unsigned char *ciphertext, int ciphertext_len, unsigned char *plaintext){

    if(!init_flag){
        fprintf(stderr, "%s\n", "please init the crypto first");
        exit(1);
    }

    int len; // this comes from the code snippet

    int plaintext_len; // this comes from the code snippet

   // now decrypt
   // this comes from the code snippet. I replaced abort with my own error handling
    if(EVP_DecryptUpdate(cipher_info.decrypt_ctx, plaintext, &len, ciphertext, ciphertext_len) != 1){
        fprintf(stderr, "%s\n", "failed to decrypt");
        exit(2);
    }

    plaintext_len = len; // this comes from the code snippet

    // this comes from the code snippet. I replaced abort with my own error handling
    if(EVP_DecryptFinal_ex(cipher_info.decrypt_ctx, plaintext + len, &len) != 1){
        fprintf(stderr, "%s\n", "decrypt failed, is your password correct?");
        exit(2);
    }

    plaintext_len += len; // this comes from the code snippet

    /* no need for clean up. Come read the memory if you dare.
     * this comes from the code snippet
     * this comes from the code snippet. Commented out because I would let the kernel get rid of the memory after program closes
    */
     //    EVP_CIPHER_CTX_free(cipher_info.encrypt_ctx);

    return plaintext_len;
}