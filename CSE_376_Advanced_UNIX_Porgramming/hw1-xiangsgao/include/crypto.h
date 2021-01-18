//
// Created by xgao on 2/12/20.
//

#ifndef HW1_CRYPTO_H
#define HW1_CRYPTO_H

#include <openssl/evp.h>
#include <stdlib.h>
#include <string.h>
#include "../include/constants_and_prototypes.h"

void crypto_init(unsigned char* password, size_t password_len); // initialing the library
void crypto_reset(void); // resetting the library

typedef struct cipher_info_struct{
    unsigned char* password;
    const byte salt[8]; // randomly typing 8 bytes for the salt
    EVP_CIPHER_CTX* encrypt_ctx;
    EVP_CIPHER_CTX* decrypt_ctx;
    unsigned char* key;
    unsigned char* iv;
} CIPHER_INFO;

// this is for encrypting a buffer and write the encrypted bytes to another buffer
int encrypt(unsigned char *plaintext, int plaintext_len, unsigned char *ciphertext);

// this is for decrypting a buffer and writting the encrypted text to another buffer
int decrypt(unsigned char *ciphertext, int ciphertext_len,  unsigned char *plaintext);


#endif //HW1_CRYPTO_H
